package com.sos.jobscheduler.history.master;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.jobscheduler.event.master.EventMeta.EventPath;
import com.sos.jobscheduler.event.master.configuration.Configuration;
import com.sos.jobscheduler.event.master.configuration.master.MasterConfiguration;
import com.sos.jobscheduler.event.master.fatevent.bean.Entry;
import com.sos.jobscheduler.event.master.handler.ILoopEventHandler;
import com.sos.jobscheduler.event.master.handler.notifier.Mailer;

public class HistoryMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryMain.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final String IDENTIFIER = "history";
    // in seconds
    private long AWAIT_TERMINATION_TIMEOUT_EVENTHANDLER = 3;
    private long AWAIT_TERMINATION_TIMEOUT_PLUGIN = 30;

    private Configuration config;
    private SOSHibernateFactory factory;
    private ExecutorService threadPool;
    private final String timezone;
    // private final List<HistoryMasterHandler> activeHandlers = Collections.synchronizedList(new ArrayList<HistoryMasterHandler>());
    private static List<HistoryMasterHandler> activeHandlers = new ArrayList<>();

    public HistoryMain(final Configuration conf) {
        config = conf;
        timezone = TimeZone.getDefault().getID();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));// TODO
    }

    public void start() throws Exception {
        createFactory(config.getHibernateConfiguration());
        Mailer mailer = new Mailer(config.getMailer());

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
        closeFactory();
        shutdownThreadPool(method, threadPool, AWAIT_TERMINATION_TIMEOUT_PLUGIN);
    }

    public String getTimezone() {
        return timezone;
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
            shutdownThreadPool(method, threadPool, AWAIT_TERMINATION_TIMEOUT_EVENTHANDLER);
            activeHandlers = new ArrayList<>();
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][skip]already closed", method));
            }
        }
    }

    private void shutdownThreadPool(String callerMethod, ExecutorService threadPool, long awaitTerminationTimeout) {
        try {
            threadPool.shutdown();
            // threadPool.shutdownNow();
            boolean shutdown = threadPool.awaitTermination(awaitTerminationTimeout, TimeUnit.SECONDS);
            if (isDebugEnabled) {
                if (shutdown) {
                    LOGGER.debug(String.format("%sthread has been shut down correctly", callerMethod));
                } else {
                    LOGGER.debug(String.format("%sthread has ended due to timeout of %ss on shutdown", callerMethod, awaitTerminationTimeout));
                }
            }
        } catch (InterruptedException e) {
            LOGGER.error(String.format("%s[exception]%s", callerMethod, e.toString()), e);
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
