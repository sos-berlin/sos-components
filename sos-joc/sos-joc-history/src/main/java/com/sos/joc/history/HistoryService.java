package com.sos.joc.history;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSGzip;
import com.sos.commons.util.SOSGzip.SOSGzipResult;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSPath.SOSPathResult;
import com.sos.joc.Globals;
import com.sos.joc.classes.proxy.ProxyUser;
import com.sos.joc.cluster.AJocClusterService;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.bean.answer.JocServiceAnswer;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration.Action;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.cluster.notifier.Mailer;
import com.sos.joc.cluster.notifier.MailerConfiguration;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.db.joc.DBItemJocVariable;
import com.sos.joc.history.controller.configuration.HistoryConfiguration;
import com.sos.joc.history.db.DBLayerHistory;
import com.sos.joc.model.cluster.common.ClusterServices;

public class HistoryService extends AJocClusterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryService.class);

    private static final String IDENTIFIER = ClusterServices.history.name();
    private static final long AWAIT_TERMINATION_TIMEOUT_EVENTHANDLER = 3;// in seconds

    private final Path logDir;

    private HistoryConfiguration config;
    private MailerConfiguration mailerConfig;
    private JocClusterHibernateFactory factory;
    private ExecutorService threadPool;
    private AtomicBoolean processingStarted = new AtomicBoolean(false);
    private AtomicBoolean stop = new AtomicBoolean(false);

    // private final List<HistoryControllerHandler> activeHandlers = Collections.synchronizedList(new ArrayList<HistoryControllerHandler>());
    private static CopyOnWriteArrayList<HistoryControllerHandler> activeHandlers = new CopyOnWriteArrayList<>();

    public HistoryService(final JocConfiguration jocConf, ThreadGroup parentThreadGroup) {
        super(jocConf, parentThreadGroup, IDENTIFIER);
        setConfig();
        logDir = Paths.get(config.getLogDir());
    }

    @Override
    public String getControllerApiUser() {
        return ProxyUser.HISTORY.getUser();
    }

    @Override
    public String getControllerApiUserPassword() {
        return ProxyUser.HISTORY.getPwd();
    }

    @Override
    public JocClusterAnswer start(List<ControllerConfiguration> controllers, AConfigurationSection configuration, StartupMode mode) {
        try {
            AJocClusterService.setLogger(IDENTIFIER);
            LOGGER.info(String.format("[%s][%s]start...", getIdentifier(), mode));

            processingStarted.set(true);
            Mailer mailer = new Mailer(mailerConfig);

            checkLogDirectory();
            createFactory(getJocConfig().getHibernateConfiguration(), controllers.size());
            handleLogsOnStart(mode);
            threadPool = Executors.newFixedThreadPool((controllers.size() + 1), new JocClusterThreadFactory(getThreadGroup(), IDENTIFIER));
            // AJocClusterService.clearAllLoggers();

            for (ControllerConfiguration controllerConfig : controllers) {
                HistoryControllerHandler controllerHandler = new HistoryControllerHandler(factory, config, controllerConfig, mailer, IDENTIFIER);
                activeHandlers.add(controllerHandler);

                Runnable task = new Runnable() {

                    @Override
                    public void run() {
                        AJocClusterService.setLogger(IDENTIFIER);

                        LOGGER.info(String.format("[%s][run]start...", controllerHandler.getIdentifier()));
                        controllerHandler.start();
                        LOGGER.info(String.format("[%s][run]end", controllerHandler.getIdentifier()));

                        // AJocClusterService.clearAllLoggers();
                    }

                };
                threadPool.submit(task);
            }
            return JocCluster.getOKAnswer(JocClusterAnswerState.STARTED);
        } catch (Exception e) {
            return JocCluster.getErrorAnswer(e);
        }
    }

    @Override
    public JocClusterAnswer stop(StartupMode mode) {
        stop.set(true);
        try {
            AJocClusterService.setLogger(IDENTIFIER);
            LOGGER.info(String.format("[%s][%s]stop...", getIdentifier(), mode));
            // AJocClusterService.clearAllLoggers();

            int size = closeEventHandlers(mode);

            AJocClusterService.setLogger(IDENTIFIER);
            if (size > 0) {
                handleLogsOnStop(mode);
            }
            closeFactory();
            JocCluster.shutdownThreadPool(mode, threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);
        } finally {
            stop.set(false);
            processingStarted.set(false);
        }
        LOGGER.info(String.format("[%s][%s]stopped", getIdentifier(), mode));

        AJocClusterService.removeLogger(IDENTIFIER);
        return JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);
    }

    @Override
    public JocServiceAnswer getInfo() {
        long start = 0;
        long end = 0;
        if (activeHandlers.size() > 0) {
            for (HistoryControllerHandler h : activeHandlers) {
                if (h.getLastActivityStart().get() > start) {
                    start = h.getLastActivityStart().get();
                }
                if (h.getLastActivityEnd().get() > end) {
                    end = h.getLastActivityEnd().get();
                }
            }
        }
        return new JocServiceAnswer(start == 0 ? null : Instant.ofEpochMilli(start), end == 0 ? null : Instant.ofEpochMilli(end));
    }

    @Override
    public void update(List<ControllerConfiguration> controllers, String controllerId, Action action) {
        Runnable task = new Runnable() {

            @Override
            public void run() {
                AJocClusterService.setLogger(IDENTIFIER);
                LOGGER.info(String.format("[%s][%s]start...", controllerId, action));
                Optional<HistoryControllerHandler> oh = activeHandlers.stream().filter(c -> c.getControllerId().equals(controllerId)).findAny();
                if (oh.isPresent()) {
                    HistoryControllerHandler h = oh.get();
                    h.close();
                    activeHandlers.remove(h);
                }
                HistoryControllerHandler h = null;
                if (!action.equals(Action.REMOVED)) {
                    Optional<ControllerConfiguration> occ = controllers.stream().filter(c -> c.getCurrent().getId().equals(controllerId)).findAny();
                    if (occ.isPresent()) {
                        h = new HistoryControllerHandler(factory, config, occ.get(), new Mailer(mailerConfig), IDENTIFIER);
                        activeHandlers.add(h);
                    } else {
                        LOGGER.error(String.format("[%s]counfiguration not found", controllerId));
                    }
                }
                int poolSize = getThreadPoolSize();
                int newPoolSize = activeHandlers.size() + 1;
                if (newPoolSize != poolSize) {
                    adjustThreadPoolSize(newPoolSize > 0 ? newPoolSize : 1);
                }
                if (h != null) {
                    h.start();
                }
                LOGGER.info(String.format("[%s][%s]end", controllerId, action));
                // AJocClusterService.clearAllLoggers();
            }
        };
        threadPool.submit(task);
    }

    @Override
    public void update(StartupMode mode, AConfigurationSection configuration) {
        AJocClusterService.setLogger(IDENTIFIER);
        updateHistoryConfiguration();
    }

    private void setConfig() {
        AJocClusterService.setLogger(IDENTIFIER);
        try {
            Properties conf = Globals.sosCockpitProperties == null ? new Properties() : Globals.sosCockpitProperties.getProperties();
            config = new HistoryConfiguration();
            config.load(conf);
            updateHistoryConfigLogSize(true);

            mailerConfig = new MailerConfiguration();// TODO
            mailerConfig.load(conf);
        } catch (Exception ex) {
            LOGGER.error(ex.toString(), ex);
        } finally {
            // AJocClusterService.clearAllLoggers();
        }
    }

    private boolean updateHistoryConfigLogSize(boolean onStart) {
        int oldLogApplicableMBSize = config.getLogApplicableMBSize();
        int oldLogMaximumMBSize = config.getLogMaximumMBSize();
        ConfigurationGlobals cg = Globals.configurationGlobals;

        boolean result = false;
        if (cg != null) {
            ConfigurationGlobalsJoc joc = (ConfigurationGlobalsJoc) cg.getConfigurationSection(DefaultSections.joc);
            if (joc != null) {
                try {
                    config.setLogApplicableMBSize(Integer.parseInt(joc.getLogApplicableSize().getValue()));
                } catch (Throwable e) {
                    LOGGER.warn(String.format("[%s][%s=%s][use default %s][error]%s", StartupMode.settings_changed.name(), joc.getLogApplicableSize()
                            .getName(), joc.getLogApplicableSize().getValue(), config.getLogApplicableMBSize(), e.toString()));
                }
                try {
                    config.setLogMaximumMBSize(Integer.parseInt(joc.getLogMaxSize().getValue()));
                } catch (Throwable e) {
                    LOGGER.warn(String.format("[%s][%s=%s][use default %s][error]%s", StartupMode.settings_changed.name(), joc.getLogMaxSize()
                            .getName(), joc.getLogMaxSize().getValue(), config.getLogMaximumMBSize(), e.toString()));
                }

                result = (oldLogApplicableMBSize != config.getLogApplicableMBSize()) || (oldLogMaximumMBSize != config.getLogMaximumMBSize());
                if (result && !onStart) {
                    LOGGER.info(String.format("[%s][old %s=%s,%s=%s][new %s=%s,%s=%s]", StartupMode.settings_changed.name(), joc
                            .getLogApplicableSize().getName(), oldLogApplicableMBSize, joc.getLogMaxSize().getName(), oldLogMaximumMBSize, joc
                                    .getLogApplicableSize().getName(), config.getLogApplicableMBSize(), joc.getLogMaxSize().getName(), config
                                            .getLogMaximumMBSize()));
                }
            }
        }
        return result;
    }

    // Another thread
    public void updateHistoryConfiguration() {
        if (updateHistoryConfigLogSize(false)) {
            if (activeHandlers.size() > 0) {
                for (HistoryControllerHandler h : activeHandlers) {
                    h.updateHistoryConfiguration(config);
                }
            }
        }
    }

    private void checkLogDirectory() throws Exception {
        if (!Files.exists(logDir)) {
            try {
                Files.createDirectory(logDir);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[history_log_dir=%s]created", logDir));
                }
            } catch (Throwable e) {
                throw new Exception(String.format("[%s][can't create directory]%s", logDir.toAbsolutePath(), e.toString()), e);
            }
        }
    }

    private void handleLogsOnStart(StartupMode mode) {
        DBLayerHistory dbLayer = null;
        try {
            if (getJocConfig().getClusterMode()) {
                String method = "handleLogsOnStart";
                dbLayer = new DBLayerHistory(factory.openStatelessSession(IDENTIFIER + "_" + method));
                Long jocInstances = dbLayer.getCountJocInstances();
                if (jocInstances > 1) {
                    switch (mode) {
                    case manual_restart:// restart cluster or history service
                    case settings_changed:
                        LOGGER.info(String.format("[%s][skip]because start=%s", method, mode));
                        // delete BLOB ???
                        break;
                    default:
                        //
                        DBItemJocVariable item = dbLayer.getLogsVariable();
                        if (item == null) {
                            LOGGER.info(String.format("[%s][skip]because compressed data not found", method));
                            return;
                        }
                        byte[] compressed = item.getBinaryValue();
                        if (compressed == null) {
                            LOGGER.info(String.format("[%s][skip][remove empty entry]because compressed data not found", method));
                            dbLayer.beginTransaction();
                            dbLayer.handleLogsVariable(getJocConfig().getMemberId(), null);
                            dbLayer.commit();
                            return;
                        }
                        if (stop.get()) {
                            LOGGER.info(String.format("[%s][skip]because stop called", method));
                            return;
                        }
                        dbLayer.close();

                        // decompress
                        LOGGER.info(String.format("[%s][%s]start..", method, logDir));
                        SOSGzipResult gr = SOSGzip.decompress(compressed, logDir, true);
                        LOGGER.info(String.format("[%s][end]%s", method, gr));
                        gr.getDirectories().forEach(d -> {
                            LOGGER.info(String.format("    [decompressed]%s", d));
                        });

                        // remove db entry
                        dbLayer.setSession(factory.openStatelessSession(IDENTIFIER + "_" + method));
                        dbLayer.beginTransaction();
                        dbLayer.handleLogsVariable(getJocConfig().getMemberId(), null);
                        dbLayer.commit();

                        break;
                    }
                }
                dbLayer.close();
                dbLayer = null;
            }
        } catch (Exception e) {
            if (dbLayer != null) {
                dbLayer.rollback();
            }
            LOGGER.error(e.toString(), e);
        } finally {
            if (dbLayer != null) {
                dbLayer.close();
            }
        }
    }

    private void handleLogsOnStop(StartupMode mode) {
        if (factory == null) {
            return;
        }
        String method = "handleLogsOnStop";
        DBLayerHistory dbLayer = null;
        try {
            dbLayer = new DBLayerHistory(factory.openStatelessSession(IDENTIFIER + "_" + method));
            boolean hasOnlyFinished = false;
            boolean skipCheckOrders = false;
            if (getJocConfig().getClusterMode()) {
                Long jocInstances = dbLayer.getCountJocInstances();
                if (jocInstances > 1) {
                    switch (mode) {
                    case manual_restart:// restart cluster or history service
                    case settings_changed:
                        LOGGER.info(String.format("[%s][skip]because stop=%s", method, mode));
                        // delete BLOB ???
                        break;
                    default:
                        // TODO select List<Long> (convert to Set) and remove "old" folders before compress
                        Long orderLogs = dbLayer.getCountNotFinishedOrderLogs();
                        long subfolders = SOSPath.getCountSubfolders(logDir, 1);
                        LOGGER.info(String.format("[%s][db: not finished order logs=%s][log directory: subfolders=%s]", method, orderLogs,
                                subfolders));

                        hasOnlyFinished = orderLogs.equals(0L);
                        if (hasOnlyFinished) {
                            dbLayer.beginTransaction();
                            dbLayer.handleLogsVariable(getJocConfig().getMemberId(), null);
                            dbLayer.commit();
                        } else {
                            if (subfolders > 0 && subfolders != orderLogs.longValue()) {
                                cleanupNotReferencedLogs(dbLayer, method);
                            }
                            dbLayer.close();

                            if (subfolders == 0 || SOSPath.isDirectoryEmpty(logDir)) {
                                LOGGER.info(String.format("[%s][compress][skip][%s]is empty", method, logDir));
                            } else {
                                // compress
                                LOGGER.info(String.format("[%s][compress][%s]start..", method, logDir));
                                SOSGzipResult gr = SOSGzip.compress(logDir, false);

                                // write compressed to database
                                dbLayer.setSession(factory.openStatelessSession(IDENTIFIER + "_" + method));
                                compress(method, dbLayer, gr);
                                dbLayer.close();

                                // log compressed results
                                gr.getDirectories().forEach(d -> {
                                    LOGGER.info(String.format("    [compressed]%s", d));
                                });

                                // cleanup whole history log directory
                                cleanupAllLogs(method);
                            }
                        }
                        break;
                    }
                    skipCheckOrders = true;
                }
            }
            if (!skipCheckOrders) {
                if (dbLayer.getSession() == null) {
                    dbLayer.setSession(factory.openStatelessSession(IDENTIFIER + "_" + method));
                }

                Long orderLogs = dbLayer.getCountNotFinishedOrderLogs();
                hasOnlyFinished = orderLogs.equals(0L);
                LOGGER.info(String.format("[%s][not finished order logs]%s", method, orderLogs));
            }
            dbLayer.close();
            dbLayer = null;

            if (hasOnlyFinished) {
                // cleanup
                LOGGER.info(String.format("[%s][cleanup][%s]start..", method, logDir));
                SOSPathResult pr = SOSPath.cleanupDirectory(logDir);
                LOGGER.info(String.format("[%s][cleanup][end]%s", method, pr));
                pr.getDirectories().forEach(d -> {
                    LOGGER.info(String.format("    [deleted]%s", d));
                });
            }
        } catch (Exception e) {
            if (dbLayer != null) {
                dbLayer.rollback();
            }
            LOGGER.error(e.toString(), e);
        } finally {
            if (dbLayer != null) {
                dbLayer.close();
            }
        }
    }

    private void compress(String caller, DBLayerHistory dbLayer, SOSGzipResult gr) throws Exception {
        String method = "compress";
        Instant start = Instant.now();
        dbLayer.beginTransaction();
        if (gr.getDirectories().size() == 0) {
            dbLayer.handleLogsVariable(getJocConfig().getMemberId(), null);
        } else {
            dbLayer.handleLogsVariable(getJocConfig().getMemberId(), gr.getCompressed());
        }
        dbLayer.commit();
        Instant end = Instant.now();
        LOGGER.info(String.format("[%s][%s][end]%s,db update=%s", caller, method, gr, SOSDate.getDuration(start, end)));
    }

    private void cleanupAllLogs(String caller) throws IOException {
        String method = "cleanupAllLogs";
        LOGGER.info(String.format("[%s][%s][%s]start..", caller, method, logDir));
        SOSPathResult r = SOSPath.cleanupDirectory(logDir);
        LOGGER.info(String.format("[%s][%s][end]%s", caller, method, r));
    }

    // TODO duplicate method (some changes) - see com.sos.joc.cleanup.model.CleanupTaskHistory
    private void cleanupNotReferencedLogs(DBLayerHistory dbLayer, String caller) {
        Path dir = logDir.toAbsolutePath();
        if (Files.exists(dir)) {
            String method = "cleanupNotReferencedLogs";
            LOGGER.info(String.format("[%s][%s]%s", caller, method, dir));

            try {
                int i = 0;
                try (Stream<Path> stream = Files.walk(dir)) {
                    for (Path p : stream.filter(f -> !f.equals(dir)).collect(Collectors.toList())) {
                        File f = p.toFile();
                        if (f.isDirectory()) {
                            try {
                                Long id = Long.parseLong(f.getName());
                                if (!dbLayer.mainOrderLogNotFinished(id)) {
                                    try {
                                        if (SOSPath.deleteIfExists(p)) {
                                            LOGGER.info(String.format("    [deleted]%s", p));
                                            i++;
                                        }
                                    } catch (Throwable e) {// in the same moment deleted by history
                                    }
                                }
                            } catch (Throwable e) {
                                LOGGER.info(String.format("    [skip][non numeric]%s", p));
                            }
                        }
                    }
                }
                LOGGER.info(String.format("[%s][%s][deleted][total]%s", caller, method, i));
            } catch (Throwable e) {
                LOGGER.warn(String.format("[%s][%s]%s", caller, method, e.toString()), e);
            }
        }
    }

    private int closeEventHandlers(StartupMode mode) {
        String method = "closeEventHandlers";

        int size = activeHandlers.size();
        AJocClusterService.setLogger(IDENTIFIER);
        LOGGER.info(String.format("[%s]found %s active handlers", method, size));
        // AJocClusterService.clearAllLoggers();
        if (size > 0) {
            // close all event handlers
            ExecutorService threadPool = Executors.newFixedThreadPool(size, new JocClusterThreadFactory(getThreadGroup(), IDENTIFIER + "-stop"));
            for (int i = 0; i < size; i++) {
                HistoryControllerHandler h = activeHandlers.get(i);
                Runnable thread = new Runnable() {

                    @Override
                    public void run() {
                        AJocClusterService.setLogger(IDENTIFIER);
                        LOGGER.info(String.format("[%s][%s]start...", method, h.getIdentifier()));
                        h.close();
                        LOGGER.info(String.format("[%s][%s]end", method, h.getIdentifier()));
                        // AJocClusterService.clearAllLoggers();
                    }
                };
                threadPool.submit(thread);
            }
            AJocClusterService.setLogger(IDENTIFIER);
            JocCluster.shutdownThreadPool(mode, threadPool, AWAIT_TERMINATION_TIMEOUT_EVENTHANDLER);
            // AJocClusterService.clearAllLoggers();
            activeHandlers = new CopyOnWriteArrayList<>();
        } else {
            if (LOGGER.isDebugEnabled()) {
                AJocClusterService.setLogger(IDENTIFIER);
                LOGGER.debug(String.format("[%s][skip]already closed", method));
                // AJocClusterService.clearAllLoggers();
            }
        }
        return size;
    }

    private void adjustThreadPoolSize(int size) {
        ThreadPoolExecutor tp = (ThreadPoolExecutor) threadPool;
        tp.setCorePoolSize(size);
        tp.setMaximumPoolSize(size);
    }

    private int getThreadPoolSize() {
        return ((ThreadPoolExecutor) threadPool).getPoolSize();
    }

    private void createFactory(Path configFile, int maxPoolSize) throws Exception {
        factory = new JocClusterHibernateFactory(configFile, 1, maxPoolSize);
        factory.setIdentifier(IDENTIFIER);
        factory.setAutoCommit(false);
        factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        factory.addClassMapping(DBLayer.getHistoryClassMapping());
        factory.addClassMapping(DBItemJocInstance.class);
        factory.build();
    }

    private void closeFactory() {
        if (factory != null) {
            factory.close();
            factory = null;
        }
        LOGGER.info(String.format("[%s]database factory closed", getIdentifier()));
    }
}
