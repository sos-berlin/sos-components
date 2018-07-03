package com.sos.jobscheduler.history.master;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.event.master.EventMeta.EventPath;
import com.sos.jobscheduler.event.master.fatevent.bean.Entry;
import com.sos.jobscheduler.event.master.handler.EventHandlerMasterSettings;
import com.sos.jobscheduler.event.master.handler.EventHandlerSettings;

public class HistoryEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryEventHandler.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final String IDENTIFIER = "history";

    private EventHandlerSettings settings;
    private SOSHibernateFactory factory;
    private final ExecutorService threadPool;
    private final List<HistoryEventHandlerMaster> activeHandlers = Collections.synchronizedList(new ArrayList<HistoryEventHandlerMaster>());

    public HistoryEventHandler(final EventHandlerSettings historySettings) {
        settings = historySettings;
        threadPool = Executors.newFixedThreadPool(settings.getMasters().size());
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public void start() throws Exception {
        createFactory(settings.getHibernateConfiguration());
        HistoryMailer hm = new HistoryMailer(settings);

        for (EventHandlerMasterSettings masterSettings : settings.getMasters()) {
            Runnable task = new Runnable() {

                @Override
                public void run() {
                    HistoryEventHandlerMaster masterHandler = new HistoryEventHandlerMaster(factory, hm, EventPath.fatEvent, Entry.class);
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

        for (HistoryEventHandlerMaster hm : activeHandlers) {
            if (isDebugEnabled) {
                LOGGER.info(String.format("[%s][%s] close...", method, hm.getIdentifier()));
            }
            hm.close();
            LOGGER.info(String.format("[%s][%s] closed", method, hm.getIdentifier()));
        }

        for (HistoryEventHandlerMaster hm : activeHandlers) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s] awaitEnd ...", method, hm.getIdentifier()));
            }
            hm.awaitEnd();
            LOGGER.info(String.format("[%s][%s] awaitEnd executed", method, hm.getIdentifier()));
        }
        closeFactory();
        LOGGER.info(String.format("[%s] factory closed", method));

        try {
            threadPool.shutdownNow();
            boolean shutdown = threadPool.awaitTermination(1L, TimeUnit.SECONDS);
            if (isDebugEnabled) {
                if (shutdown) {
                    LOGGER.debug(String.format("[%s] thread has been shut down correctly", method));
                } else {
                    LOGGER.debug(String.format("[%s] thread has ended due to timeout on shutdown. doesn�t wait for answer from thread", method));
                }
            }
        } catch (InterruptedException e) {
            LOGGER.error(String.format("[%s] %s", method, e.toString()), e);
        }
    }

    private void createFactory(Path configFile) throws Exception {
        factory = new SOSHibernateFactory(configFile);
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
    }
}
