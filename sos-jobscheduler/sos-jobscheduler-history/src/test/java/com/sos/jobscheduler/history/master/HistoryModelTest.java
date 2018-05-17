package com.sos.jobscheduler.history.master;

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

    public SOSHibernateFactory createFactory(Path configFile) throws Exception {
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
        ms.setHost(host);
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
        String fatEventResponse =
                "{\"TYPE\":\"NonEmpty\",\"stampeds\":[{\"eventId\":1526556248013000,\"key\":\"/re_test@2018-05-17T11:25:00Z\",\"TYPE\":\"OrderAddedFat\",\"cause\":\"UNKNOWN\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[0]},\"scheduledAt\":1526556300000,\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556300072000,\"timestamp\":1526556300016,\"key\":\"/re_test@2018-05-17T11:25:00Z\",\"TYPE\":\"OrderProcessingStartedFat\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[0]},\"agentUri\":\"http://localhost:4445\",\"jobPath\":\"/test_1\",\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556300193000,\"timestamp\":1526556300143,\"key\":\"/re_test@2018-05-17T11:25:00Z\",\"TYPE\":\"OrderStdoutWrittenFat\",\"chunk\":\"test_1 line 1 - 13:25:00,13\\r\\ntest_1 line 3 - 13:25:00,13\\r\\n\"},{\"eventId\":1526556300359000,\"timestamp\":1526556300171,\"key\":\"/re_test@2018-05-17T11:25:00Z\",\"TYPE\":\"OrderProcessedFat\",\"outcome\":{\"TYPE\":\"Succeeded\",\"returnCode\":0},\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556300361001,\"timestamp\":1526556300237,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_1\",\"TYPE\":\"OrderAddedFat\",\"parent\":\"/re_test@2018-05-17T11:25:00Z\",\"cause\":\"Forked\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[1,\"fork_1\",0]},\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556300361001,\"timestamp\":1526556300237,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_2\",\"TYPE\":\"OrderAddedFat\",\"parent\":\"/re_test@2018-05-17T11:25:00Z\",\"cause\":\"Forked\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[1,\"fork_2\",0]},\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556300363000,\"timestamp\":1526556300265,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_1\",\"TYPE\":\"OrderProcessingStartedFat\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[1,\"fork_1\",0]},\"agentUri\":\"http://localhost:4445\",\"jobPath\":\"/test_2\",\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556300363001,\"timestamp\":1526556300266,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_2\",\"TYPE\":\"OrderProcessingStartedFat\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[1,\"fork_2\",0]},\"agentUri\":\"http://localhost:4445\",\"jobPath\":\"/test_4\",\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556300502000,\"timestamp\":1526556300312,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_1\",\"TYPE\":\"OrderStdoutWrittenFat\",\"chunk\":\"test_2 line 1 - 13:25:00,31\\r\\ntest_2 line 2 - 13:25:00,31\\r\\n\"},{\"eventId\":1526556300503000,\"timestamp\":1526556300337,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_2\",\"TYPE\":\"OrderStdoutWrittenFat\",\"chunk\":\"test_4 - 13:25:00,32\\r\\n\"},{\"eventId\":1526556300503001,\"timestamp\":1526556300337,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_1\",\"TYPE\":\"OrderStdoutWrittenFat\",\"chunk\":\"test_2 line 3 - 13:25:00,31\\r\\n\"},{\"eventId\":1526556300503002,\"timestamp\":1526556300356,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_2\",\"TYPE\":\"OrderProcessedFat\",\"outcome\":{\"TYPE\":\"Succeeded\",\"returnCode\":0},\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556300504000,\"timestamp\":1526556300357,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_1\",\"TYPE\":\"OrderProcessedFat\",\"outcome\":{\"TYPE\":\"Succeeded\",\"returnCode\":0},\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556300504003,\"timestamp\":1526556300412,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_2\",\"TYPE\":\"OrderProcessingStartedFat\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[1,\"fork_2\",1]},\"agentUri\":\"http://localhost:4445\",\"jobPath\":\"/test_5\",\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556300505000,\"timestamp\":1526556300413,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_1\",\"TYPE\":\"OrderProcessingStartedFat\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[1,\"fork_1\",1]},\"agentUri\":\"http://localhost:4445\",\"jobPath\":\"/test_3\",\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556300632000,\"timestamp\":1526556300485,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_2\",\"TYPE\":\"OrderStdoutWrittenFat\",\"chunk\":\"test_5 - 13:25:00,47\\r\\n\"},{\"eventId\":1526556300632001,\"timestamp\":1526556300500,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_2\",\"TYPE\":\"OrderProcessedFat\",\"outcome\":{\"TYPE\":\"Succeeded\",\"returnCode\":0},\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556300633000,\"timestamp\":1526556300523,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_1\",\"TYPE\":\"OrderStdoutWrittenFat\",\"chunk\":\"test_3 - 13:25:00,49\\r\\n\"},{\"eventId\":1526556300635000,\"timestamp\":1526556300567,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_1\",\"TYPE\":\"OrderProcessedFat\",\"outcome\":{\"TYPE\":\"Succeeded\",\"returnCode\":0},\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556301009000,\"timestamp\":1526556300845,\"key\":\"/re_test@2018-05-17T11:25:00Z\",\"TYPE\":\"OrderProcessingStartedFat\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[2]},\"agentUri\":\"http://localhost:4445\",\"jobPath\":\"/test_6\",\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556301009001,\"timestamp\":1526556300890,\"key\":\"/re_test@2018-05-17T11:25:00Z\",\"TYPE\":\"OrderStdoutWrittenFat\",\"chunk\":\"test_6 - 13:25:00,89\\r\\n\"},{\"eventId\":1526556301010000,\"timestamp\":1526556300911,\"key\":\"/re_test@2018-05-17T11:25:00Z\",\"TYPE\":\"OrderProcessedFat\",\"outcome\":{\"TYPE\":\"Succeeded\",\"returnCode\":0},\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556301118000,\"key\":\"/re_test@2018-05-17T11:25:00Z\",\"TYPE\":\"OrderFinishedFat\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[3]}}]}";
        
        SOSHibernateFactory factory = null;
        try {
            factory = mt.createFactory(hibernateConfigFile);
            HistoryModel m = new HistoryModel(factory, mt.createMasterSettings(schedulerId, schedulerHost, schedulerPort));

            m.getEventId();
            m.process(mt.createEvent(fatEventResponse));

        } catch (Throwable t) {
            throw t;
        } finally {
            mt.closeFactory(factory);
        }
    }

}
