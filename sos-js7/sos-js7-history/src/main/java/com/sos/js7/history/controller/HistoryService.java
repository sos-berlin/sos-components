package com.sos.js7.history.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSGzip;
import com.sos.commons.util.SOSPath;
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
import com.sos.joc.db.history.DBItemHistoryTempLog;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.js7.history.controller.configuration.HistoryConfiguration;

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
            handleTempLogsOnStart();
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
            handleTempLogsOnEnd();
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

    private void handleTempLogsOnStart() {
        SOSHibernateSession session = null;
        try {
            session = factory.openStatelessSession(IDENTIFIER);
            session.beginTransaction();

            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_TEMP_LOGS);
            hql.append(" where memberId <> :memberId");
            Query<DBItemHistoryTempLog> query = session.createQuery(hql.toString());
            query.setParameter("memberId", getJocConfig().getMemberId());
            List<DBItemHistoryTempLog> result = session.getResultList(query);
            session.commit();

            if (result != null && result.size() > 0) {
                List<Long> toDelete = new ArrayList<Long>();
                for (int i = 0; i < result.size(); i++) {
                    DBItemHistoryTempLog item = result.get(i);

                    Path dir = getOrderLogDirectory(logDir, item.getHistoryOrderMainParentId());
                    try {
                        if (Files.exists(dir)) {
                            SOSPath.cleanupDirectory(dir);
                        } else {
                            Files.createDirectory(dir);
                        }
                        SOSGzip.decompress(item.getContent(), dir, true);
                        toDelete.add(item.getHistoryOrderMainParentId());
                        LOGGER.info(String.format("[log directory restored from database]%s", dir));
                    } catch (Exception e) {
                        LOGGER.error(String.format("[%s]%s", dir, e.toString()), e);
                    }
                }

                session.beginTransaction();
                if (toDelete.size() == result.size()) {
                    session.getSQLExecutor().executeUpdate("truncate table " + DBLayer.TABLE_HISTORY_TEMP_LOGS);
                } else {
                    hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_HISTORY_TEMP_LOGS);
                    hql.append(" where historyOrderMainParentId in (:historyOrderMainParentIds)");
                    query = session.createQuery(hql.toString());
                    query.setParameterList("historyOrderMainParentIds", toDelete);
                    session.executeUpdate(query);
                }
                session.commit();
            } else {
                LOGGER.info("[handleTempLogsOnStart]0 log directories to restore");
            }

            session.close();
            session = null;
        } catch (Exception e) {
            if (session != null) {
                try {
                    session.rollback();
                } catch (SOSHibernateException e1) {
                }
            }
            LOGGER.error(e.toString(), e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private void handleTempLogsOnEnd() {
        if (factory == null) {
            return;
        }
        SOSHibernateSession session = null;
        try {
            session = factory.openStatelessSession();
            session.beginTransaction();
            List<Long> result = session.getResultList("select id from " + DBLayer.DBITEM_HISTORY_ORDERS + " where parentId=0 and logId=0");
            session.commit();

            if (result != null && result.size() > 0) {
                for (int i = 0; i < result.size(); i++) {
                    importOrderLogs(session, result.get(i));
                }
            } else {
                LOGGER.info("[handleTempLogsOnEnd]0 log directories imported into database");
            }
            session.close();
            session = null;
        } catch (Exception e) {
            if (session != null) {
                try {
                    session.rollback();
                } catch (SOSHibernateException e1) {
                }
            }
            LOGGER.error(e.toString(), e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private void importOrderLogs(SOSHibernateSession session, Long historyOrderMainParentId) {
        Path dir = getOrderLogDirectory(logDir, historyOrderMainParentId);
        try {
            if (Files.exists(dir)) {
                session.beginTransaction();

                StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_TEMP_LOGS);
                hql.append(" where historyOrderMainParentId=:historyOrderMainParentId");
                Query<DBItemHistoryTempLog> query = session.createQuery(hql.toString());
                query.setParameter("historyOrderMainParentId", historyOrderMainParentId);

                DBItemHistoryTempLog item = session.getSingleResult(query);
                File f = SOSPath.getMostRecentFile(dir);
                Long mostRecentFile = f == null ? 0L : f.lastModified(); // TODO current time if null?
                boolean imported = false;
                if (item == null) {
                    item = new DBItemHistoryTempLog();
                    item.setHistoryOrderMainParentId(historyOrderMainParentId);
                    item.setMemberId(getJocConfig().getMemberId());
                    item.setContent(SOSGzip.compress(dir, false).getCompressed());
                    item.setMostRecentFile(mostRecentFile);
                    item.setCreated(new Date());
                    item.setModified(item.getCreated());
                    session.save(item);
                    imported = true;
                } else {
                    if (!item.getMostRecentFile().equals(mostRecentFile)) {
                        item.setMemberId(getJocConfig().getMemberId());
                        item.setContent(SOSGzip.compress(dir, false).getCompressed());
                        item.setMostRecentFile(mostRecentFile);
                        item.setModified(new Date());
                        session.update(item);
                        imported = true;
                    }
                }
                session.commit();
                if (imported) {
                    LOGGER.info(String.format("[log directory imported into database]%s", dir));
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(String.format("[log directory not imported into database][%s][mostRecentFile=%s][item.mostRecentFile=%s]", dir,
                                mostRecentFile, item.getMostRecentFile()));
                    }
                }
            }

        } catch (Exception e) {
            if (session != null) {
                try {
                    session.rollback();
                } catch (SOSHibernateException e1) {
                }
            }
            LOGGER.error(String.format("[%s]%s", dir, e.toString()), e);
        }
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
