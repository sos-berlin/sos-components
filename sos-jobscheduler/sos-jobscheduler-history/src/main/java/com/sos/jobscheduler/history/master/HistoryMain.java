package com.sos.jobscheduler.history.master;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.db.history.DBItemHistoryTempLog;
import com.sos.jobscheduler.event.master.EventMeta.EventPath;
import com.sos.jobscheduler.event.master.configuration.Configuration;
import com.sos.jobscheduler.event.master.configuration.master.MasterConfiguration;
import com.sos.jobscheduler.event.master.fatevent.bean.Entry;
import com.sos.jobscheduler.event.master.handler.ILoopEventHandler;
import com.sos.jobscheduler.event.notifier.Mailer;
import com.sos.jobscheduler.history.master.configuration.HistoryConfiguration;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.api.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.api.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.handler.IJocClusterHandler;

public class HistoryMain implements IJocClusterHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryMain.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    private static final String IDENTIFIER = "history";
    private static final String PROPERTIES_FILE = "history.properties";
    // private static final String LOG4J_FILE = "history.log4j2.xml";
    // in seconds
    private long AWAIT_TERMINATION_TIMEOUT_EVENTHANDLER = 3;

    private Configuration config;
    private final JocConfiguration jocConfig;
    private final Path logDir;

    private JocClusterHibernateFactory factory;
    private ExecutorService threadPool;
    private boolean masterProcessingStarted;

    // private final List<HistoryMasterHandler> activeHandlers = Collections.synchronizedList(new ArrayList<HistoryMasterHandler>());
    private static List<HistoryMasterHandler> activeHandlers = new ArrayList<>();

    public HistoryMain(final JocConfiguration jocConf) {
        jocConfig = jocConf;
        // setLogger(LOG4J_FILE);
        setConfiguration();
        logDir = Paths.get(((HistoryConfiguration) config.getApp()).getLogDir());
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public String getMasterApiUser() {
        return IDENTIFIER;
    }

    @Override
    public String getMasterApiUserPassword() {
        return IDENTIFIER;
    }

    @Override
    public JocClusterAnswer start(List<MasterConfiguration> masters) {
        try {
            LOGGER.info(String.format("[%s]start", getIdentifier()));

            masterProcessingStarted = false;
            Mailer mailer = new Mailer(config.getMailer());
            tmpMoveLogFiles(config);
            config.setMasters(masters);

            createFactory(jocConfig.getHibernateConfiguration());
            handleTempLogsOnStart();
            threadPool = Executors.newFixedThreadPool(config.getMasters().size(), new JocClusterThreadFactory(IDENTIFIER));

            for (MasterConfiguration masterConfig : config.getMasters()) {
                HistoryMasterHandler masterHandler = new HistoryMasterHandler(factory, config, mailer, EventPath.fatEvent, Entry.class);
                masterHandler.init(masterConfig);
                activeHandlers.add(masterHandler);

                Runnable task = new Runnable() {

                    @Override
                    public void run() {
                        masterHandler.setIdentifier(null);
                        LOGGER.info(String.format("[start][%s][run]...", masterHandler.getIdentifier()));
                        masterHandler.run();
                        masterProcessingStarted = true;
                        LOGGER.info(String.format("[start][%s][end]", masterHandler.getIdentifier()));
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
        String method = "stop";
        LOGGER.info(String.format("[%s]stop", getIdentifier()));

        closeEventHandlers();
        handleTempLogsOnEnd();
        closeFactory();
        JocCluster.shutdownThreadPool(method, threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);

        return JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);
    }

    private void setConfiguration() {
        String method = "getConfiguration";

        config = new Configuration();
        try {
            Properties conf = JocConfiguration.readConfiguration(jocConfig.getResourceDirectory().resolve(PROPERTIES_FILE).normalize());
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

    private void tmpMoveLogFiles(Configuration conf) {// to be delete
        try {
            Path logDir = Paths.get(((HistoryConfiguration) conf.getApp()).getLogDir());
            List<Path> l = SOSPath.getFileList(logDir, "^[1-9]*[_]?[1-9]*\\.log$", 0);
            l.stream().forEach(p -> {
                Path dir = logDir.resolve(p.getFileName().toString().replace(".log", "").split("_")[0]);
                try {
                    if (!Files.exists(dir)) {
                        Files.createDirectory(dir);
                    }
                    Files.move(p, dir.resolve(p.getFileName()), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    LOGGER.error(e.toString(), e);
                }
            });
            LOGGER.info(String.format("[tmpMoveLogFiles][moved]%s", l.size()));
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
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
            query.setParameter("memberId", jocConfig.getMemberId());
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
        if (factory == null || !masterProcessingStarted) {
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
                    item.setMemberId(jocConfig.getMemberId());
                    item.setContent(SOSPath.gzipDirectory(dir));
                    item.setMostRecentFile(mostRecentFile);
                    item.setCreated(new Date());
                    item.setModified(item.getCreated());
                    session.save(item);
                    imported = true;
                } else {
                    if (!item.getMostRecentFile().equals(mostRecentFile)) {
                        item.setMemberId(jocConfig.getMemberId());
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
            ExecutorService threadPool = Executors.newFixedThreadPool(size);
            for (int i = 0; i < size; i++) {
                ILoopEventHandler eh = activeHandlers.get(i);
                Runnable thread = new Runnable() {

                    @Override
                    public void run() {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][start]...", method, eh.getIdentifier()));
                        }
                        eh.close();
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][end]", method, eh.getIdentifier()));
                        }
                    }
                };
                threadPool.submit(thread);
            }
            JocCluster.shutdownThreadPool(method, threadPool, AWAIT_TERMINATION_TIMEOUT_EVENTHANDLER);
            activeHandlers = new ArrayList<>();
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][skip]already closed", method));
            }
        }
    }

    private void createFactory(Path configFile) throws Exception {
        factory = new JocClusterHibernateFactory(configFile, 1, config.getMasters().size());
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
        LOGGER.info(String.format("database factory closed"));
    }

    @SuppressWarnings("unused")
    private void setLogger(String logConfigurationFile) {
        Path p = jocConfig.getResourceDirectory().resolve(logConfigurationFile).normalize();
        if (Files.exists(p)) {
            try {
                LoggerContext context = (LoggerContext) LogManager.getContext(false);
                context.setConfigLocation(p.toUri());
                context.updateLoggers();
                LOGGER.info(String.format("[setLogger]%s", p));
            } catch (Exception e) {
                LOGGER.warn(e.toString(), e);
            }
        } else {
            LOGGER.info("[setLogger]use default logger configuration");
        }
    }
}
