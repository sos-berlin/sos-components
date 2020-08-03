package com.sos.js7.history.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.JocClusterService;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.JocClusterServices;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.history.DBItemHistoryTempLog;
import com.sos.js7.event.controller.EventMeta.EventPath;
import com.sos.js7.event.controller.configuration.Configuration;
import com.sos.js7.event.controller.configuration.controller.ControllerConfiguration;
import com.sos.js7.event.controller.fatevent.bean.Entry;
import com.sos.js7.event.controller.handler.ILoopEventHandler;
import com.sos.js7.event.notifier.Mailer;
import com.sos.js7.history.controller.configuration.HistoryConfiguration;

public class HistoryMain extends JocClusterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryMain.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    private static final String IDENTIFIER = JocClusterServices.history.name();
    private static final String PROPERTIES_FILE = "history.properties";
    private static final long AWAIT_TERMINATION_TIMEOUT_EVENTHANDLER = 3;// in seconds

    private final Path logDir;

    private Configuration config;
    private JocClusterHibernateFactory factory;
    private ExecutorService threadPool;
    private boolean processingStarted;

    // private final List<HistoryControllerHandler> activeHandlers = Collections.synchronizedList(new ArrayList<HistoryControllerHandler>());
    private static List<HistoryControllerHandler> activeHandlers = new ArrayList<>();

    public HistoryMain(final JocConfiguration jocConf, ThreadGroup parentThreadGroup) {
        super(jocConf, parentThreadGroup, IDENTIFIER);
        MDC.put("clusterService", IDENTIFIER);
        setConfig();
        logDir = Paths.get(((HistoryConfiguration) config.getApp()).getLogDir());
    }

    @Override
    public JocClusterAnswer start(List<ControllerConfiguration> controllers) {
        try {
            MDC.put("clusterService", IDENTIFIER);
            LOGGER.info(String.format("[%s]start...", getIdentifier()));

            processingStarted = false;
            Mailer mailer = new Mailer(config.getMailer());
            config.setControllers(controllers);

            createFactory(getJocConfig().getHibernateConfiguration());
            handleTempLogsOnStart();
            threadPool = Executors.newFixedThreadPool(config.getControllers().size(), new JocClusterThreadFactory(getThreadGroup(), IDENTIFIER));

            for (ControllerConfiguration controllerConfig : config.getControllers()) {
                HistoryControllerHandler controllerHandler = new HistoryControllerHandler(factory, config, mailer, EventPath.fatEvent, Entry.class);
                controllerHandler.init(controllerConfig);
                activeHandlers.add(controllerHandler);

                Runnable task = new Runnable() {

                    @Override
                    public void run() {
                        controllerHandler.setIdentifier(null);
                        LOGGER.info(String.format("[%s][run]start ...", controllerHandler.getIdentifier()));
                        controllerHandler.run();
                        processingStarted = true;
                        LOGGER.info(String.format("[%s][run]end", controllerHandler.getIdentifier()));
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
    public JocClusterAnswer stop() {
        MDC.put("clusterService", IDENTIFIER);
        LOGGER.info(String.format("[%s]stop...", getIdentifier()));

        closeEventHandlers();
        handleTempLogsOnEnd();
        closeFactory();
        JocCluster.shutdownThreadPool(threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);

        LOGGER.info(String.format("[%s]stopped", getIdentifier()));

        return JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);
    }

    private void setConfig() {
        String method = "getConfig";

        config = new Configuration();
        try {
            Properties conf = JocConfiguration.readConfiguration(getJocConfig().getResourceDirectory().resolve(PROPERTIES_FILE).normalize());
            config.getMailer().load(conf);
            config.getHandler().load(conf);
            config.getHttpClient().load(conf);
            config.getWebservice().load(conf);

            HistoryConfiguration h = new HistoryConfiguration();
            h.load(conf);
            config.setApp(h);

            LOGGER.info(String.format("[%s]%s", method, SOSString.toString(config)));
        } catch (Exception ex) {
            LOGGER.error(ex.toString(), ex);
        }
    }

    private void handleTempLogsOnStart() {
        SOSHibernateSession session = null;
        try {
            session = factory.openStatelessSession(IDENTIFIER);
            session.beginTransaction();

            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_TEMP_LOG);
            hql.append(" where memberId <> :memberId");
            Query<DBItemHistoryTempLog> query = session.createQuery(hql.toString());
            query.setParameter("memberId", getJocConfig().getMemberId());
            List<DBItemHistoryTempLog> result = session.getResultList(query);
            session.commit();

            if (result != null && result.size() > 0) {
                List<Long> toDelete = new ArrayList<Long>();
                for (int i = 0; i < result.size(); i++) {
                    DBItemHistoryTempLog item = result.get(i);

                    Path dir = getOrderLogDirectory(logDir, item.getMainOrdertId());
                    try {
                        if (Files.exists(dir)) {
                            SOSPath.cleanupDirectory(dir);
                        } else {
                            Files.createDirectory(dir);
                        }
                        SOSPath.ungzipDirectory(item.getContent(), dir);
                        toDelete.add(item.getMainOrdertId());
                        LOGGER.info(String.format("[log directory restored from database]%s", dir));
                    } catch (Exception e) {
                        LOGGER.error(String.format("[%s]%s", dir, e.toString()), e);
                    }
                }

                session.beginTransaction();
                if (toDelete.size() == result.size()) {
                    session.getSQLExecutor().executeUpdate("truncate table " + DBLayer.TABLE_HISTORY_TEMP_LOGS);
                } else {
                    hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_HISTORY_TEMP_LOG);
                    hql.append(" where mainOrderId in (:mainOrderIds)");
                    query = session.createQuery(hql.toString());
                    query.setParameterList("mainOrderIds", toDelete);
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
        if (factory == null || !processingStarted) {
            return;
        }
        SOSHibernateSession session = null;
        try {
            session = factory.openStatelessSession();
            session.beginTransaction();
            List<Long> result = session.getResultList("select id from " + DBLayer.DBITEM_HISTORY_ORDER + " where parentId=0 and logId=0");
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

    private void importOrderLogs(SOSHibernateSession session, Long mainOrderId) {
        Path dir = getOrderLogDirectory(logDir, mainOrderId);
        try {
            if (Files.exists(dir)) {
                session.beginTransaction();

                StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_TEMP_LOG);
                hql.append(" where mainOrderId=:mainOrderId");
                Query<DBItemHistoryTempLog> query = session.createQuery(hql.toString());
                query.setParameter("mainOrderId", mainOrderId);

                DBItemHistoryTempLog item = session.getSingleResult(query);
                File f = SOSPath.getMostRecentFile(dir);
                Long mostRecentFile = f == null ? 0L : f.lastModified(); // TODO current time if null?
                boolean imported = false;
                if (item == null) {
                    item = new DBItemHistoryTempLog();
                    item.setMainOrderId(mainOrderId);
                    item.setMemberId(getJocConfig().getMemberId());
                    item.setContent(SOSPath.gzipDirectory(dir));
                    item.setMostRecentFile(mostRecentFile);
                    item.setCreated(new Date());
                    item.setModified(item.getCreated());
                    session.save(item);
                    imported = true;
                } else {
                    if (!item.getMostRecentFile().equals(mostRecentFile)) {
                        item.setMemberId(getJocConfig().getMemberId());
                        item.setContent(SOSPath.gzipDirectory(dir));
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

    public static Path getOrderLogDirectory(Path logDir, Long mainOrderId) {
        return logDir.resolve(String.valueOf(mainOrderId));
    }

    private void closeEventHandlers() {
        String method = "closeEventHandlers";

        int size = activeHandlers.size();
        if (size > 0) {
            // closes http client on all event handlers
            ExecutorService threadPool = Executors.newFixedThreadPool(size, new JocClusterThreadFactory(getThreadGroup(), IDENTIFIER + "-stop"));
            for (int i = 0; i < size; i++) {
                ILoopEventHandler eh = activeHandlers.get(i);
                Runnable thread = new Runnable() {

                    @Override
                    public void run() {
                        LOGGER.info(String.format("[%s][%s]start...", method, eh.getIdentifier()));
                        eh.close();
                        LOGGER.info(String.format("[%s][%s]end", method, eh.getIdentifier()));
                    }
                };
                threadPool.submit(thread);
            }
            JocCluster.shutdownThreadPool(threadPool, AWAIT_TERMINATION_TIMEOUT_EVENTHANDLER);
            activeHandlers = new ArrayList<>();
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][skip]already closed", method));
            }
        }
    }

    private void createFactory(Path configFile) throws Exception {
        factory = new JocClusterHibernateFactory(configFile, 1, config.getControllers().size());
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
