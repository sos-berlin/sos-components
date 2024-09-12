package com.sos.joc.history;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.time.Instant;
import java.util.ArrayList;
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
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.common.JocClusterServiceActivity;
import com.sos.joc.cluster.common.JocClusterUtil;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.JocHistoryConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration.Action;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc.LogExtType;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.cluster.service.JocClusterServiceLogger;
import com.sos.joc.cluster.service.active.AJocActiveMemberService;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.db.joc.DBItemJocVariable;
import com.sos.joc.history.db.DBLayerHistory;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.joc.model.cluster.common.state.JocClusterState;

public class HistoryService extends AJocActiveMemberService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryService.class);

    private static final String IDENTIFIER = ClusterServices.history.name();
    private static final long AWAIT_TERMINATION_TIMEOUT_EVENTHANDLER = 3;// in seconds

    private JocHistoryConfiguration config;
    private JocClusterHibernateFactory factory;
    private ExecutorService threadPool;
    private AtomicBoolean processingStarted = new AtomicBoolean(false);
    private AtomicBoolean stop = new AtomicBoolean(false);

    // private final List<HistoryControllerHandler> activeHandlers = Collections.synchronizedList(new ArrayList<HistoryControllerHandler>());
    private static CopyOnWriteArrayList<HistoryControllerHandler> activeHandlers = new CopyOnWriteArrayList<>();

    public HistoryService(final JocConfiguration jocConf, ThreadGroup parentThreadGroup) {
        super(jocConf, parentThreadGroup, IDENTIFIER);
        setConfig();
    }

    @Override
    public String getControllerApiUser() {
        return ProxyUser.HISTORY.getUser();
    }

    @Override
    public String getControllerApiUserPassword() {
        return ProxyUser.HISTORY.getPwd();
    }

    public static void setLogger() {
        JocClusterServiceLogger.setLogger(IDENTIFIER);
    }

    @Override
    public JocClusterAnswer start(StartupMode mode, List<ControllerConfiguration> controllers, AConfigurationSection configuration) {
        try {
            setLogger();
            LOGGER.info(String.format("[%s][%s]start...", getIdentifier(), mode));

            processingStarted.set(true);

            checkLogDirectory();
            createFactory(getJocConfig().getHibernateConfiguration(), controllers.size());
            handleLogsOnStart(mode);

            ThreadGroup tg = getThreadGroup();
            threadPool = Executors.newFixedThreadPool((controllers.size() + 1), new JocClusterThreadFactory(tg, IDENTIFIER));
            // JocClusterServiceLogger.clearAllLoggers();

            for (ControllerConfiguration controllerConfig : controllers) {
                HistoryControllerHandler controllerHandler = new HistoryControllerHandler(factory, config, controllerConfig, IDENTIFIER);
                activeHandlers.add(controllerHandler);

                Runnable task = new Runnable() {

                    @Override
                    public void run() {
                        JocClusterServiceLogger.setLogger(IDENTIFIER);

                        LOGGER.info(String.format("[%s][%s][run]start...", IDENTIFIER, controllerHandler.getIdentifier()));
                        controllerHandler.start(mode, tg);
                        LOGGER.info(String.format("[%s][%s][run]end", IDENTIFIER, controllerHandler.getIdentifier()));

                        // JocClusterServiceLogger.clearAllLoggers();
                    }

                };
                threadPool.submit(task);
            }
            return JocCluster.getOKAnswer(JocClusterState.STARTED);
        } catch (Exception e) {
            return JocCluster.getErrorAnswer(e);
        }
    }

    @Override
    public JocClusterAnswer stop(StartupMode mode) {
        stop.set(true);
        try {
            setLogger();
            LOGGER.info(String.format("[%s][%s]stop...", getIdentifier(), mode));
            // JocClusterServiceLogger.clearAllLoggers();

            int size = closeEventHandlers(mode);

            JocClusterServiceLogger.setLogger(IDENTIFIER);
            if (size > 0) {
                handleLogsOnStop(mode);
            }
            closeFactory();
            JocCluster.shutdownThreadPool("[" + getIdentifier() + "][" + mode + "]", threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);
        } finally {
            stop.set(false);
            processingStarted.set(false);
        }
        LOGGER.info(String.format("[%s][%s]stopped", getIdentifier(), mode));

        JocClusterServiceLogger.removeLogger(IDENTIFIER);
        return JocCluster.getOKAnswer(JocClusterState.STOPPED);
    }

    @Override
    public void runNow(StartupMode mode, List<ControllerConfiguration> controllers, AConfigurationSection configuration) {

    }

    @Override
    public JocClusterServiceActivity getActivity() {
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
        return new JocClusterServiceActivity(start == 0 ? null : Instant.ofEpochMilli(start), end == 0 ? null : Instant.ofEpochMilli(end));
    }

    @Override
    public boolean startPause() {
        return true;
    }

    @Override
    public boolean stopPause() {
        return true;
    }

    @Override
    public void update(StartupMode mode, List<ControllerConfiguration> controllers, String controllerId, Action action) {
        Runnable task = new Runnable() {

            @Override
            public void run() {
                setLogger();
                LOGGER.info(String.format("[%s][%s][%s]start...", getIdentifier(), controllerId, action));
                Optional<HistoryControllerHandler> oh = activeHandlers.stream().filter(c -> c.getControllerId().equals(controllerId)).findAny();
                if (oh.isPresent()) {
                    HistoryControllerHandler h = oh.get();
                    h.close(mode);
                    activeHandlers.remove(h);
                }
                HistoryControllerHandler h = null;
                if (!action.equals(Action.REMOVED)) {
                    Optional<ControllerConfiguration> occ = controllers.stream().filter(c -> c.getCurrent().getId().equals(controllerId)).findAny();
                    if (occ.isPresent()) {
                        h = new HistoryControllerHandler(factory, config, occ.get(), IDENTIFIER);
                        activeHandlers.add(h);
                    } else {
                        LOGGER.error(String.format("[%s][%s]counfiguration not found", getIdentifier(), controllerId));
                    }
                }
                int poolSize = getThreadPoolSize();
                int newPoolSize = activeHandlers.size() + 1;
                if (newPoolSize != poolSize) {
                    adjustThreadPoolSize(newPoolSize > 0 ? newPoolSize : 1);
                }
                if (h != null) {
                    h.start(mode, getThreadGroup());
                }
                LOGGER.info(String.format("[%s][%s][%s]end", getIdentifier(), controllerId, action));
                // JocClusterServiceLogger.clearAllLoggers();
            }
        };
        threadPool.submit(task);
    }

    @Override
    public void update(StartupMode mode, AConfigurationSection configuration) {
        setLogger();
        updateHistoryConfiguration();
    }

    private void setConfig() {
        setLogger();
        try {
            Properties conf = Globals.sosCockpitProperties == null ? new Properties() : Globals.sosCockpitProperties.getProperties();
            config = new JocHistoryConfiguration();
            config.load(conf);
            updateHistoryConfig(true);
        } catch (Exception ex) {
            LOGGER.error(ex.toString(), ex);
        } finally {
            // JocClusterServiceLogger.clearAllLoggers();
        }
    }

    private boolean updateHistoryConfig(boolean onStart) {
        ConfigurationGlobals cg = Globals.configurationGlobals;
        boolean result = false;
        if (cg != null) {
            ConfigurationGlobalsJoc joc = (ConfigurationGlobalsJoc) cg.getConfigurationSection(DefaultSections.joc);
            String logTransferChanges = updateHistoryConfigLogTransfer(joc, onStart);
            String logSizeChanges = updateHistoryConfigLogSize(joc, onStart);
            result = logTransferChanges != null || logSizeChanges != null;
            if (result && !onStart) {
                String ltc = logTransferChanges == null ? "" : "[" + logTransferChanges + "]";
                String lsc = logSizeChanges == null ? "" : "[" + logSizeChanges + "]";
                LOGGER.info(String.format("[%s][%s]%s%s", getIdentifier(), StartupMode.settings_changed.name(), ltc, lsc));
            }
        }
        return result;
    }

    private String updateHistoryConfigLogTransfer(ConfigurationGlobalsJoc joc, boolean onStart) {
        if (joc == null) {
            return null;
        }
        Path oldDir = config.getLogExtDir();
        LogExtType oldOrderHistory = config.getLogExtOrderHistory();
        LogExtType oldOrder = config.getLogExtOrder();
        LogExtType oldTask = config.getLogExtTask();

        StringBuilder warn = config.setLogExt(joc);
        if (warn != null) {
            LOGGER.warn(String.format("[%s]%s", StartupMode.settings_changed.name(), warn));
        }
        boolean result = !config.isLogExtDirEquals(oldDir) || !config.isLogExtOrderHistoryEquals(oldOrderHistory) || !config.isLogExtOrderEquals(
                oldOrder) || !config.isLogExtTaskEquals(oldTask);
        if (result && !onStart) {
            return String.format("[%s][%s][old %s=%s,%s=%s,%s=%s,%s=%s][new %s=%s,%s=%s,%s=%s,%s=%s]", getIdentifier(), StartupMode.settings_changed
                    .name(), joc.getLogExtDirectory().getName(), oldDir, joc.getLogExtOrderHistory().getName(), oldOrderHistory, joc.getLogExtOrder()
                            .getName(), oldOrder, joc.getLogExtTask().getName(), oldTask, joc.getLogExtDirectory().getName(), config.getLogExtDir(),
                    joc.getLogExtOrderHistory().getName(), config.getLogExtOrderHistory(), joc.getLogExtOrder().getName(), config.getLogExtOrder(),
                    joc.getLogExtTask().getName(), config.getLogExtTask());
        }
        return null;
    }

    private String updateHistoryConfigLogSize(ConfigurationGlobalsJoc joc, boolean onStart) {
        if (joc == null) {
            return null;
        }
        int oldLogApplicableMBSize = config.getLogApplicableMBSize();
        int oldLogMaximumMBSize = config.getLogMaximumMBSize();
        int oldLogMaximumDisplayMBSize = config.getLogMaximumDisplayMBSize();

        boolean result = false;
        try {
            config.setLogApplicableMBSize(Integer.parseInt(joc.getLogApplicableSize().getValue()));
        } catch (Throwable e) {
            LOGGER.warn(String.format("[%s][%s][%s=%s][use default %s][error]%s", getIdentifier(), StartupMode.settings_changed.name(), joc
                    .getLogApplicableSize().getName(), joc.getLogApplicableSize().getValue(), config.getLogApplicableMBSize(), e.toString()));
        }
        try {
            config.setLogMaximumMBSize(Integer.parseInt(joc.getLogMaxSize().getValue()));
        } catch (Throwable e) {
            LOGGER.warn(String.format("[%s][%s][%s=%s][use default %s][error]%s", getIdentifier(), StartupMode.settings_changed.name(), joc
                    .getLogMaxSize().getName(), joc.getLogMaxSize().getValue(), config.getLogMaximumMBSize(), e.toString()));
        }
        try {
            config.setLogMaximumDisplayMBSize(Integer.parseInt(joc.getMaxDisplaySize().getValue()));
        } catch (Throwable e) {
            LOGGER.warn(String.format("[%s][%s][%s=%s][use default %s][error]%s", getIdentifier(), StartupMode.settings_changed.name(), joc
                    .getLogMaxSize().getName(), joc.getLogMaxSize().getValue(), config.getLogMaximumMBSize(), e.toString()));
        }
        result = (oldLogApplicableMBSize != config.getLogApplicableMBSize()) || (oldLogMaximumMBSize != config.getLogMaximumMBSize()
                || oldLogMaximumDisplayMBSize != config.getLogMaximumDisplayMBSize());
        if (result && !onStart) {
            return String.format("[%s][old %s=%s,%s=%s,%s=%s][new %s=%s,%s=%s,%s=%s]", getIdentifier(), joc.getLogApplicableSize().getName(),
                    oldLogApplicableMBSize, joc.getLogMaxSize().getName(), oldLogMaximumMBSize, joc.getMaxDisplaySize().getName(),
                    oldLogMaximumDisplayMBSize, joc.getLogApplicableSize().getName(), config.getLogApplicableMBSize(), joc.getLogMaxSize().getName(),
                    config.getLogMaximumMBSize(), joc.getMaxDisplaySize().getName(), config.getLogMaximumDisplayMBSize());
        }
        return null;
    }

    // Another thread
    public void updateHistoryConfiguration() {
        if (updateHistoryConfig(false)) {
            if (activeHandlers.size() > 0) {
                for (HistoryControllerHandler h : activeHandlers) {
                    h.updateHistoryConfiguration(config);
                }
            }
        }
    }

    private void checkLogDirectory() throws Exception {
        if (!Files.exists(config.getLogDir())) {
            try {
                Files.createDirectory(config.getLogDir());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[%s][history_log_dir=%s]created", getIdentifier(), config.getLogDir()));
                }
            } catch (Throwable e) {
                throw new Exception(String.format("[%s][%s][can't create directory]%s", getIdentifier(), config.getLogDir(), e.toString()), e);
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
                        LOGGER.info(String.format("[%s][%s][skip]because start=%s", getIdentifier(), method, mode));
                        // delete BLOB ???
                        break;
                    default:
                        //
                        DBItemJocVariable item = dbLayer.getLogsVariable();
                        if (item == null) {
                            LOGGER.info(String.format("[%s][%s][skip]because compressed data not found", getIdentifier(), method));
                            return;
                        }
                        byte[] compressed = item.getBinaryValue();
                        if (compressed == null) {
                            LOGGER.info(String.format("[%s][%s][skip][remove empty entry]because compressed data not found", getIdentifier(),
                                    method));
                            dbLayer.beginTransaction();
                            dbLayer.handleLogsVariable(getJocConfig().getMemberId(), null);
                            dbLayer.commit();
                            return;
                        }
                        if (stop.get()) {
                            LOGGER.info(String.format("[%s][%s][skip]because stop called", getIdentifier(), method));
                            return;
                        }
                        dbLayer.close();

                        // decompress
                        LOGGER.info(String.format("[%s][%s][%s]start..", getIdentifier(), method, config.getLogDir()));
                        SOSGzipResult gr = SOSGzip.decompress(compressed, config.getLogDir(), true);
                        LOGGER.info(String.format("[%s][%s][end]%s", getIdentifier(), method, gr));
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
                        LOGGER.info(String.format("[%s][%s][skip]because stop=%s", getIdentifier(), method, mode));
                        // delete BLOB ???
                        break;
                    default:
                        // TODO select List<Long> (convert to Set) and remove "old" folders before compress
                        Long orderLogs = dbLayer.getCountNotFinishedOrderLogs();
                        long subfolders = SOSPath.getCountSubfolders(config.getLogDir(), 1);
                        LOGGER.info(String.format("[%s][%s][db: not finished order logs=%s][log directory: subfolders=%s]", getIdentifier(), method,
                                orderLogs, subfolders));

                        hasOnlyFinished = orderLogs.equals(0L);
                        if (hasOnlyFinished) {
                            dbLayer.beginTransaction();
                            dbLayer.handleLogsVariable(getJocConfig().getMemberId(), null);
                            dbLayer.commit();
                        } else {
                            if (subfolders > 0 && subfolders != orderLogs.longValue()) {
                                deleteNotReferencedLogs(dbLayer, method);
                            }
                            dbLayer.close();

                            if (subfolders == 0 || SOSPath.isDirectoryEmpty(config.getLogDir())) {
                                LOGGER.info(String.format("[%s][%s][compress][skip][%s]is empty", getIdentifier(), method, config.getLogDir()));
                            } else {
                                // truncate the logs that exceed the log applicable size
                                truncateLogs(method, config.getLogDir());

                                // compress
                                LOGGER.info(String.format("[%s][%s][compress][%s]start..", getIdentifier(), method, config.getLogDir()));
                                SOSGzipResult gr = SOSGzip.compress(config.getLogDir(), false);

                                // write compressed to database
                                dbLayer.setSession(factory.openStatelessSession(IDENTIFIER + "_" + method));
                                storeCompress(method, dbLayer, gr);
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
                LOGGER.info(String.format("[%s][%s][not finished order logs]%s", getIdentifier(), method, orderLogs));
            }
            dbLayer.close();
            dbLayer = null;

            if (hasOnlyFinished) {
                // cleanup
                LOGGER.info(String.format("[%s][%s][cleanup][%s]start..", getIdentifier(), method, config.getLogDir()));
                SOSPathResult pr = SOSPath.cleanupDirectory(config.getLogDir());
                LOGGER.info(String.format("[%s][%s][cleanup][end]%s", getIdentifier(), method, pr));
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

    private void truncateLogs(String caller, Path logDir) {
        ConfigurationGlobals cg = Globals.configurationGlobals;
        if (cg != null) {
            ConfigurationGlobalsJoc joc = (ConfigurationGlobalsJoc) cg.getConfigurationSection(DefaultSections.joc);
            if (joc != null) {
                try {
                    int logMaximumMBSize = Integer.parseInt(joc.getLogMaxSize().getValue());
                    int logApplicableMBSize = Integer.parseInt(joc.getLogApplicableSize().getValue());
                    if (logMaximumMBSize > 0 || logApplicableMBSize > 0) {

                        int logMaximumBytes = JocClusterUtil.mb2bytes(logMaximumMBSize);
                        int logApplicableBytes = JocClusterUtil.mb2bytes(logApplicableMBSize);
                        List<Path> bigFiles = new ArrayList<>();
                        try (Stream<Path> stream = Files.walk(logDir)) {
                            bigFiles = stream.filter(f -> {
                                try {
                                    long size = Files.size(f);
                                    return Files.isRegularFile(f) && (size > logApplicableBytes || size > logMaximumBytes);
                                } catch (IOException e) {
                                    return false;
                                }
                            }).map(Path::normalize).collect(Collectors.toList());
                        }
                        for (Path p : bigFiles) {
                            long size = Files.size(p);

                            boolean truncateIsMaximum = false;
                            int truncateExeededMBSize = 0;
                            if (size > logMaximumBytes) {
                                truncateIsMaximum = true;
                                truncateExeededMBSize = logMaximumMBSize;
                            } else {
                                truncateExeededMBSize = logApplicableMBSize;
                            }
                            JocClusterUtil.truncateHistoryOriginalLogFile(caller, p, size, truncateExeededMBSize, truncateIsMaximum);
                        }
                    }
                } catch (Throwable e) {
                    LOGGER.warn(String.format("[%s][%s][truncateLogs][%s=%s][%s=%s][error]%s", getIdentifier(), caller, joc.getLogMaxSize().getName(),
                            joc.getLogMaxSize().getValue(), joc.getLogApplicableSize().getName(), joc.getLogApplicableSize().getValue(), e
                                    .toString()));
                }
            }
        }
    }

    private void storeCompress(String caller, DBLayerHistory dbLayer, SOSGzipResult gr) throws Exception {
        String method = "storeCompress";
        Instant start = Instant.now();
        dbLayer.beginTransaction();
        if (gr.getDirectories().size() == 0) {
            dbLayer.handleLogsVariable(getJocConfig().getMemberId(), null);
        } else {
            dbLayer.handleLogsVariable(getJocConfig().getMemberId(), gr.getCompressed());
        }
        dbLayer.commit();
        Instant end = Instant.now();
        LOGGER.info(String.format("[%s][%s][%s][end]%s,db update=%s", getIdentifier(), caller, method, gr, SOSDate.getDuration(start, end)));
    }

    private void cleanupAllLogs(String caller) throws IOException {
        String method = "cleanupAllLogs";
        LOGGER.info(String.format("[%s][%s][%s][%s]start..", getIdentifier(), caller, method, config.getLogDir()));
        SOSPathResult r = SOSPath.cleanupDirectory(config.getLogDir());
        LOGGER.info(String.format("[%s][%s][%s][end]%s", getIdentifier(), caller, method, r));
    }

    // TODO duplicate method (some changes) - see com.sos.joc.cleanup.model.CleanupTaskHistory
    private void deleteNotReferencedLogs(DBLayerHistory dbLayer, String caller) {
        Path dir = config.getLogDir().toAbsolutePath();
        if (Files.exists(dir)) {
            String method = "deleteNotReferencedLogs";
            LOGGER.info(String.format("[%s][%s][%s]%s", getIdentifier(), caller, method, dir));

            try {
                int i = 0;
                try (Stream<Path> stream = Files.walk(dir)) {
                    for (Path p : stream.filter(f -> !f.equals(dir)).collect(Collectors.toList())) {
                        File f = p.toFile();
                        if (f.isDirectory()) {
                            try {
                                Long id = Long.parseLong(f.getName());
                                if (id > JocHistoryConfiguration.ID_NOT_STARTED_ORDER) {// id=0 is a temporary folder for not started orders - see
                                                                                        // config.getLogDirTmpOrders()
                                    if (!dbLayer.mainOrderLogNotFinished(id)) {
                                        try {
                                            if (SOSPath.deleteIfExists(p)) {
                                                LOGGER.info(String.format("    [deleted]%s", p));
                                                i++;
                                            }
                                        } catch (Throwable e) {// in the same moment deleted by history
                                        }
                                    }
                                }
                            } catch (Throwable e) {
                                LOGGER.info(String.format("    [skip][non numeric]%s", p));
                            }
                        }
                    }
                }
                LOGGER.info(String.format("[%s][%s][%s][deleted][total]%s", getIdentifier(), caller, method, i));
            } catch (Throwable e) {
                LOGGER.warn(String.format("[%s][%s][%s]%s", getIdentifier(), caller, method, e.toString()), e);
            }
        }
    }

    private int closeEventHandlers(StartupMode mode) {
        String method = "closeEventHandlers";

        int size = activeHandlers.size();
        JocClusterServiceLogger.setLogger(IDENTIFIER);
        LOGGER.info(String.format("[%s][%s]found %s active handlers", getIdentifier(), method, size));
        // JocClusterServiceLogger.clearAllLoggers();
        if (size > 0) {
            // close all event handlers
            ExecutorService threadPool = Executors.newFixedThreadPool(size, new JocClusterThreadFactory(getThreadGroup(), IDENTIFIER + "-stop"));
            for (int i = 0; i < size; i++) {
                HistoryControllerHandler h = activeHandlers.get(i);
                Runnable thread = new Runnable() {

                    @Override
                    public void run() {
                        JocClusterServiceLogger.setLogger(IDENTIFIER);
                        LOGGER.info(String.format("[%s][%s][%s]start...", getIdentifier(), method, h.getIdentifier()));
                        h.close(mode);
                        LOGGER.info(String.format("[%s][%s][%s]end", getIdentifier(), method, h.getIdentifier()));
                        // JocClusterServiceLogger.clearAllLoggers();
                    }
                };
                threadPool.submit(thread);
            }
            JocClusterServiceLogger.setLogger(IDENTIFIER);
            JocCluster.shutdownThreadPool("[" + getIdentifier() + "][" + mode + "]", threadPool, AWAIT_TERMINATION_TIMEOUT_EVENTHANDLER);
            // JocClusterServiceLogger.clearAllLoggers();
            activeHandlers = new CopyOnWriteArrayList<>();
        } else {
            if (LOGGER.isDebugEnabled()) {
                JocClusterServiceLogger.setLogger(IDENTIFIER);
                LOGGER.debug(String.format("[%s][%s][skip]already closed", getIdentifier(), method));
                // JocClusterServiceLogger.clearAllLoggers();
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
