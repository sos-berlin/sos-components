package com.sos.js7.history.controller;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.exception.SOSHibernateException;
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
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.cluster.notifier.Mailer;
import com.sos.joc.cluster.notifier.MailerConfiguration;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.db.joc.DBItemJocVariable;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.js7.history.controller.configuration.HistoryConfiguration;
import com.sos.js7.history.db.DBLayerHistory;

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
            AJocClusterService.clearLogger();

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

                        AJocClusterService.clearLogger();
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
        AJocClusterService.setLogger(IDENTIFIER);
        LOGGER.info(String.format("[%s][%s]stop...", getIdentifier(), mode));
        AJocClusterService.clearLogger();

        int size = closeEventHandlers(mode);

        AJocClusterService.setLogger(IDENTIFIER);
        if (size > 0) {
            handleLogsOnStop(mode);
        }
        closeFactory();
        JocCluster.shutdownThreadPool(mode, threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);

        processingStarted.set(false);
        LOGGER.info(String.format("[%s][%s]stopped", getIdentifier(), mode));

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
                AJocClusterService.clearLogger();
            }
        };
        threadPool.submit(task);
    }

    private void setConfig() {
        AJocClusterService.setLogger(IDENTIFIER);
        try {
            Properties conf = Globals.sosCockpitProperties == null ? new Properties() : Globals.sosCockpitProperties.getProperties();
            config = new HistoryConfiguration();
            config.load(conf);

            mailerConfig = new MailerConfiguration();// TODO
            mailerConfig.load(conf);
        } catch (Exception ex) {
            LOGGER.error(ex.toString(), ex);
        } finally {
            AJocClusterService.clearLogger();
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
                        decompress(method, dbLayer);
                        break;
                    }
                }
                dbLayer.close();
                dbLayer = null;
            }
        } catch (Exception e) {
            if (dbLayer != null) {
                try {
                    dbLayer.getSession().rollback();
                } catch (SOSHibernateException e1) {
                }
            }
            LOGGER.error(e.toString(), e);
        } finally {
            if (dbLayer != null) {
                dbLayer.close();
            }
        }
    }

    private void decompress(String caller, DBLayerHistory dbLayer) throws Exception {
        String method = "decompress";
        DBItemJocVariable item = dbLayer.getLogsVariable();
        if (item == null) {
            LOGGER.info(String.format("[%s][%s][skip]because compressed data not found", caller, method));
            return;
        }
        byte[] compressed = item.getBinaryValue();
        if (compressed == null) {
            LOGGER.info(String.format("[%s][%s][skip][remove empty entry]because compressed data not found", caller, method));
        } else {
            // current member - getJocConfig().getMemberId(), stored member - item.getTextValue

            // cleanup logs
            // LOGGER.info(String.format("[%s][cleanup][%s]start..", method, logDir));
            // SOSPathResult pr = SOSPath.cleanupDirectory(logDir);
            // LOGGER.info(String.format("[%s][cleanup][end]%s", method, pr));

            // decompress
            LOGGER.info(String.format("[%s][%s][%s]start..", caller, method, logDir));
            SOSGzipResult gr = SOSGzip.decompress(compressed, logDir, true);
            LOGGER.info(String.format("[%s][%s][end]%s", caller, method, gr));
            gr.getDirectories().forEach(d -> {
                LOGGER.info(String.format("    [decompressed]%s", d));
            });
        }

        // remove db entry
        dbLayer.getSession().beginTransaction();
        dbLayer.handleLogsVariable(getJocConfig().getMemberId(), null);
        dbLayer.getSession().commit();
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
                        hasOnlyFinished = orderLogs.equals(0L);
                        LOGGER.info(String.format("[%s][not finished order logs]%s", method, orderLogs));

                        if (hasOnlyFinished) {
                            dbLayer.getSession().beginTransaction();
                            dbLayer.handleLogsVariable(getJocConfig().getMemberId(), null);
                            dbLayer.getSession().commit();
                        } else {
                            compress(method, dbLayer);
                        }
                        break;
                    }
                    skipCheckOrders = true;
                }
            }
            if (!skipCheckOrders) {
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
                    LOGGER.info(String.format("    [removed]%s", d));
                });
            }
        } catch (Exception e) {
            if (dbLayer != null) {
                try {
                    dbLayer.getSession().rollback();
                } catch (SOSHibernateException e1) {
                }
            }
            LOGGER.error(e.toString(), e);
        } finally {
            if (dbLayer != null) {
                dbLayer.close();
            }
        }
    }

    private void compress(String caller, DBLayerHistory dbLayer) throws Exception {
        String method = "compress";
        // compress logs
        LOGGER.info(String.format("[%s][%s][%s]start..", caller, method, logDir));
        SOSGzipResult gr = SOSGzip.compress(logDir, false);

        Instant start = Instant.now();
        dbLayer.getSession().beginTransaction();
        if (gr.getDirectories().size() == 0) {
            dbLayer.handleLogsVariable(getJocConfig().getMemberId(), null);
        } else {
            dbLayer.handleLogsVariable(getJocConfig().getMemberId(), gr.getCompressed());
        }
        dbLayer.getSession().commit();
        Instant end = Instant.now();

        LOGGER.info(String.format("[%s][%s][end]%s,db update=%s", caller, method, gr, SOSDate.getDuration(start, end)));
        gr.getDirectories().forEach(d -> {
            LOGGER.info(String.format("    [compressed]%s", d));
        });

        // cleanup logs
        LOGGER.info(String.format("[%s][%s][cleanup][%s]start..", caller, method, logDir));
        SOSPathResult r = SOSPath.cleanupDirectory(logDir);
        LOGGER.info(String.format("[%s][%s][cleanup][end]%s", caller, method, r));
    }

    public static Path getOrderLogDirectory(Path logDir, Long historyOrderMainParentId) {
        return logDir.resolve(String.valueOf(historyOrderMainParentId));
    }

    private int closeEventHandlers(StartupMode mode) {
        String method = "closeEventHandlers";

        int size = activeHandlers.size();
        AJocClusterService.setLogger(IDENTIFIER);
        LOGGER.info(String.format("[%s]found %s active handlers", method, size));
        AJocClusterService.clearLogger();
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
                        AJocClusterService.clearLogger();
                    }
                };
                threadPool.submit(thread);
            }
            AJocClusterService.setLogger(IDENTIFIER);
            JocCluster.shutdownThreadPool(mode, threadPool, AWAIT_TERMINATION_TIMEOUT_EVENTHANDLER);
            AJocClusterService.clearLogger();
            activeHandlers = new CopyOnWriteArrayList<>();
        } else {
            if (LOGGER.isDebugEnabled()) {
                AJocClusterService.setLogger(IDENTIFIER);
                LOGGER.debug(String.format("[%s][skip]already closed", method));
                AJocClusterService.clearLogger();
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
