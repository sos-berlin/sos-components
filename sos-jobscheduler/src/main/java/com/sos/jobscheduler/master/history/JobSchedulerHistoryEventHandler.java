package com.sos.jobscheduler.master.history;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.jobscheduler.master.event.EventHandlerMasterSettings;
import com.sos.jobscheduler.master.event.EventHandlerSettings;

public class JobSchedulerHistoryEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerHistoryEventHandler.class);

    private EventHandlerSettings settings;
    private SOSHibernateFactory factory;
    private final ExecutorService threadPool;
    private final Set<JobSchedulerMasterHistoryEventHandler> activeHandlers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public JobSchedulerHistoryEventHandler(final EventHandlerSettings historySettings) {
        settings = historySettings;
        threadPool = Executors.newFixedThreadPool(settings.getMasters().size());
    }

    public void start() throws Exception {
        createFactory(settings.getHibernateConfiguration());

        for (EventHandlerMasterSettings masterSettings : settings.getMasters()) {
            Runnable task = new Runnable() {

                @Override
                public void run() {
                    JobSchedulerMasterHistoryEventHandler masterHandler = new JobSchedulerMasterHistoryEventHandler(factory);
                    masterHandler.init(masterSettings);
                    activeHandlers.add(masterHandler);

                    masterHandler.run();
                }

            };
            threadPool.submit(task);
        }
    }

    public void exit() {
        String method = "exit";

        for (JobSchedulerMasterHistoryEventHandler hm : activeHandlers) {
            LOGGER.info(String.format("[%s][%s] close",method, hm.getIdentifier()));
            hm.close();
        }

        for (JobSchedulerMasterHistoryEventHandler hm : activeHandlers) {
            LOGGER.info(String.format("[%s][%s] awaitEnd ...",method, hm.getIdentifier()));
            hm.awaitEnd();
        }

        closeFactory();

        try {
            threadPool.shutdownNow();
            boolean shutdown = threadPool.awaitTermination(1L, TimeUnit.SECONDS);
            if (shutdown) {
                LOGGER.debug(String.format("[%s] thread has been shut down correctly", method));
            } else {
                LOGGER.debug(String.format("[%s] thread has ended due to timeout on shutdown. doesn�t wait for answer from thread", method));
            }
        } catch (InterruptedException e) {
            LOGGER.error(String.format("[%s] %s", method, e.toString()), e);
        }
    }

    private void createFactory(Path configFile) throws Exception {
        factory = new SOSHibernateFactory(configFile);
        factory.setIdentifier("history");
        factory.setAutoCommit(false);
        factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        // factory.addClassMapping(DBLayer.getReportingClassMapping());
        // factory.addClassMapping(DBLayer.getInventoryClassMapping());
        // factory.addClassMapping(com.sos.jitl.notification.db.DBLayer.getNotificationClassMapping());
        factory.build();
    }

    private void closeFactory() {
        if (factory != null) {
            factory.close();
            factory = null;
        }
    }
}
