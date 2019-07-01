package com.sos.jobscheduler.history.master.model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.TimeZone;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.event.master.bean.Event;
import com.sos.jobscheduler.event.master.bean.IEntry;
import com.sos.jobscheduler.event.master.fatevent.bean.Entry;
import com.sos.jobscheduler.event.master.handler.configuration.Master;
import com.sos.jobscheduler.history.master.configuration.HistoryMasterConfiguration;
import com.sos.jobscheduler.history.master.model.HistoryModel;

public class HistoryModelTest {

    public SOSHibernateFactory createFactory(String masterId, Path configFile, boolean autoCommit) throws Exception {
        SOSHibernateFactory factory = new SOSHibernateFactory(configFile);
        factory.setIdentifier("history");
        factory.setAutoCommit(autoCommit);
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

    public Event createEvent(String response) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        SimpleModule sm = new SimpleModule();
        sm.addAbstractTypeMapping(IEntry.class, Entry.class);
        objectMapper.registerModule(sm);

        return objectMapper.readValue(response, Event.class);
    }

    public static void main(String[] args) throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        HistoryModelTest mt = new HistoryModelTest();

        String masterId = "jobscheduler2";
        String masterUri = "http://localhost:4444";
        Path hibernateConfigFile = Paths.get("src/test/resources/hibernate.cfg.xml");
        String fatEventResponse = new String(Files.readAllBytes(Paths.get("src/test/resources/history.json")));

        SOSHibernateFactory factory = null;
        boolean autoCommit = false;
        try {
            factory = mt.createFactory(masterId, hibernateConfigFile, autoCommit);
            String identifier = "[" + masterId + "]";

            Master primary = new Master(masterId, masterUri);
            Master backup = null;

            HistoryModel m = new HistoryModel(factory, new HistoryMasterConfiguration(primary, backup), identifier);

            m.setMaxTransactions(100);

            m.setStoredEventId(m.getEventId());
            m.process(mt.createEvent(fatEventResponse), null);
        } catch (Throwable t) {
            throw t;
        } finally {
            mt.closeFactory(factory);
        }
    }

}
