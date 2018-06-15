package com.sos.jobscheduler.history.master;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.event.master.bean.Event;
import com.sos.jobscheduler.event.master.bean.IEntry;
import com.sos.jobscheduler.event.master.fatevent.bean.Entry;
import com.sos.jobscheduler.event.master.handler.EventHandlerMasterSettings;

public class HistoryModelTest {

    public SOSHibernateFactory createFactory(String schedulerId, Path configFile) throws Exception {
        SOSHibernateFactory factory = new SOSHibernateFactory(configFile);
        factory.setIdentifier("history");
        factory.setAutoCommit(false);
        factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        factory.addClassMapping(DBLayer.getHistoryClassMapping());
        factory.build();
        return factory;
    }

    public void closeFactory(SOSHibernateFactory factory) {
        if (factory != null) {
            factory.close();
            factory = null;
        }
    }

    public EventHandlerMasterSettings createMasterSettings(String schedulerId, String host, String port) {
        EventHandlerMasterSettings ms = new EventHandlerMasterSettings();
        ms.setSchedulerId(schedulerId);
        ms.setHttpHost(host);
        ms.setHttpPort(port);
        return ms;
    }

    public Event createEvent(String response) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        SimpleModule sm = new SimpleModule();
        sm.addAbstractTypeMapping(IEntry.class, Entry.class);
        objectMapper.registerModule(sm);

        return objectMapper.readValue(response, Event.class);
    }

    public static void main(String[] args) throws Exception {
        HistoryModelTest mt = new HistoryModelTest();

        String schedulerId = "jobscheduler2";
        String schedulerHost = "localhost";
        String schedulerPort = "4444";
        Path hibernateConfigFile = Paths.get("src/test/resources/hibernate.cfg.xml");
        String fatEventResponse = new String(Files.readAllBytes(Paths.get("src/test/resources/history.json")));

        SOSHibernateFactory factory = null;
        try {
            factory = mt.createFactory(schedulerId, hibernateConfigFile);
            String identifier = "[" + schedulerId + "]";
            HistoryModel m = new HistoryModel(factory, mt.createMasterSettings(schedulerId, schedulerHost, schedulerPort), identifier);

            m.setStoredEventId(m.getEventId());
            m.process(mt.createEvent(fatEventResponse));
        } catch (Throwable t) {
            throw t;
        } finally {
            mt.closeFactory(factory);
        }
    }

}
