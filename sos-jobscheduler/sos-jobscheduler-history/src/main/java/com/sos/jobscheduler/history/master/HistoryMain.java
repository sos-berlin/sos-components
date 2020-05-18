package com.sos.jobscheduler.history.master;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSPath;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.db.history.DBItemTempLog;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.jobscheduler.event.master.EventMeta.EventPath;
import com.sos.jobscheduler.event.master.configuration.Configuration;
import com.sos.jobscheduler.event.master.configuration.master.MasterConfiguration;
import com.sos.jobscheduler.event.master.fatevent.bean.Entry;
import com.sos.jobscheduler.event.master.handler.ILoopEventHandler;
import com.sos.jobscheduler.event.notifier.Mailer;
import com.sos.jobscheduler.history.master.configuration.HistoryConfiguration;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.configuration.JocConfiguration;

public class HistoryMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryMain.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final String IDENTIFIER = "history";
    // in seconds
    private long AWAIT_TERMINATION_TIMEOUT_EVENTHANDLER = 3;
    private long AWAIT_TERMINATION_TIMEOUT_PLUGIN = 30;

    private final Configuration config;
    private final JocConfiguration jocConfig;
    private final Path logDir;

    private SOSHibernateFactory factory;
    private ExecutorService threadPool;
    // private final List<HistoryMasterHandler> activeHandlers = Collections.synchronizedList(new ArrayList<HistoryMasterHandler>());
    private static List<HistoryMasterHandler> activeHandlers = new ArrayList<>();

    public HistoryMain(final Configuration conf, final JocConfiguration jocConf) {
        config = conf;
        jocConfig = jocConf;
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));// TODO
        logDir = Paths.get(((HistoryConfiguration) config.getApp()).getLogDir());
    }

    public void start() throws Exception {
        createFactory(config.getHibernateConfiguration());
        Mailer mailer = new Mailer(config.getMailer());
        handleTempLogsOnStart();

        boolean run = true;
        while (run) {
            try {
                setMasters();
                if (config.getMasters() != null && config.getMasters().size() > 0) {
                    run = false;
                } else {
                    LOGGER.info("no masters found. sleep 1m and try again ...");
                    Thread.sleep(60 * 1_000);
                }
            } catch (Exception e) {
                LOGGER.error(String.format("[error occured][sleep 1m and try again ...]%s", e.toString()));
                Thread.sleep(60 * 1_000);
            }
        }

        threadPool = Executors.newFixedThreadPool(config.getMasters().size());

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
                    LOGGER.info(String.format("[start][%s][end]", masterHandler.getIdentifier()));
                }

            };
            threadPool.submit(task);
        }
    }

    private void handleTempLogsOnStart() {
        SOSHibernateSession session = null;
        try {
            session = factory.openStatelessSession("history");
            session.beginTransaction();

            StringBuilder hql = new StringBuilder("from ").append(DBLayer.HISTORY_DBITEM_TEMP_LOG);
            hql.append(" where memberId <> :memberId");
            Query<DBItemTempLog> query = session.createQuery(hql.toString());
            query.setParameter("memberId", jocConfig.getMemberId());
            List<DBItemTempLog> result = session.getResultList(query);
            session.commit();

            if (result != null && result.size() > 0) {
                List<Long> toDelete = new ArrayList<Long>();
                for (int i = 0; i < result.size(); i++) {
                    DBItemTempLog item = result.get(i);

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
                    session.getSQLExecutor().executeUpdate("truncate table " + DBLayer.HISTORY_TABLE_TEMP_LOGS);
                } else {
                    hql = new StringBuilder("delete from ").append(DBLayer.HISTORY_DBITEM_TEMP_LOG);
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
        SOSHibernateSession session = null;
        try {
            session = factory.openStatelessSession("history");
            session.beginTransaction();
            List<Long> result = session.getResultList("select id from " + DBLayer.HISTORY_DBITEM_ORDER + " where parentId=0 and logId=0");
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

                StringBuilder hql = new StringBuilder("from ").append(DBLayer.HISTORY_DBITEM_TEMP_LOG);
                hql.append(" where mainOrderId=:mainOrderId");
                Query<DBItemTempLog> query = session.createQuery(hql.toString());
                query.setParameter("mainOrderId", mainOrderId);

                DBItemTempLog item = session.getSingleResult(query);
                File f = SOSPath.getMostRecentFile(dir);
                Long mostRecentFile = f == null ? 0L : f.lastModified(); // TODO current time if null?
                boolean imported = false;
                if (item == null) {
                    item = new DBItemTempLog();
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

    private void setMasters() throws Exception {
        SOSHibernateSession session = null;
        try {
            session = factory.openStatelessSession("history");
            session.beginTransaction();
            List<DBItemInventoryInstance> result = session.getResultList("from " + DBLayer.DBITEM_INVENTORY_INSTANCES);
            session.commit();
            session.close();
            session = null;

            if (result != null && result.size() > 0) {
                Map<String, Properties> map = new HashMap<String, Properties>();
                for (int i = 0; i < result.size(); i++) {
                    DBItemInventoryInstance item = result.get(i);

                    Properties p = null;
                    if (map.containsKey(item.getSchedulerId())) {
                        p = map.get(item.getSchedulerId());
                    } else {
                        p = new Properties();
                    }
                    // TODO user, pass
                    p.setProperty("jobscheduler_id", item.getSchedulerId());
                    if (item.getIsPrimaryMaster()) {
                        p.setProperty("primary_master_uri", item.getUri());
                        if (item.getClusterUri() != null) {
                            p.setProperty("primary_cluster_uri", item.getClusterUri());
                        }
                        if (!config.isPublic()) {
                            p.setProperty("primary_master_user", "history");
                            p.setProperty("primary_master_user_password", "history");
                        }
                    } else {
                        p.setProperty("backup_master_uri", item.getUri());
                        if (item.getClusterUri() != null) {
                            p.setProperty("backup_cluster_uri", item.getClusterUri());
                        }
                        if (!config.isPublic()) {
                            p.setProperty("backup_master_user", "history");
                            p.setProperty("backup_master_user_password", "history");
                        }
                    }
                    map.put(item.getSchedulerId(), p);
                }

                for (Map.Entry<String, Properties> entry : map.entrySet()) {
                    LOGGER.info(String.format("[add][masterConfiguration]%s", entry));
                    MasterConfiguration mc = new MasterConfiguration();
                    mc.load(entry.getValue());
                    config.addMaster(mc);
                }
            }
        } catch (Exception e) {
            if (session != null) {
                try {
                    session.rollback();
                } catch (SOSHibernateException e1) {
                }
            }
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public void exit() {
        String method = "exit";

        closeEventHandlers();
        handleTempLogsOnEnd();
        closeFactory();
        JocCluster.shutdownThreadPool(method, threadPool, AWAIT_TERMINATION_TIMEOUT_PLUGIN);
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
        factory = new SOSHibernateFactory(configFile);
        factory.setIdentifier(IDENTIFIER);
        factory.setAutoCommit(false);
        factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        factory.addClassMapping(DBLayer.getHistoryClassMapping());
        factory.addClassMapping(DBItemInventoryInstance.class);
        factory.build();
    }

    private void closeFactory() {
        if (factory != null) {
            factory.close();
            factory = null;
        }
        LOGGER.info(String.format("database factory closed"));
    }
}
