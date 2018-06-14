package com.sos.jobscheduler.event.master.bean.fatevent;

import java.util.List;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.sos.jobscheduler.event.master.bean.Event;
import com.sos.jobscheduler.event.master.bean.IEntry;
import com.sos.jobscheduler.event.master.fatevent.bean.Entry;

public class EventTest {

    public ObjectMapper getObjectMapper(Class<? extends IEntry> clazz) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

        SimpleModule sm = new SimpleModule();
        sm.addAbstractTypeMapping(IEntry.class, clazz);
        mapper.registerModule(sm);

        // mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
        // mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    public void readEmptyEvent(ObjectMapper mapper) throws Exception {
        String emptyEvent = "{\"TYPE\":\"Empty\",\"lastEventId\":1526370922642000}";
        Event feE = mapper.readValue(emptyEvent, Event.class);

        System.out.println(feE.getType());
        System.out.println(feE.getLastEventId());
        System.out.println("-----");
    }

    public void readNotEmptyEvent(ObjectMapper mapper) throws Exception {
        String notEmptyEvent =
                "{\"TYPE\":\"NonEmpty\",\"stampeds\":[{\"eventId\":1526556248013000,\"key\":\"/re_test@2018-05-17T11:25:00Z\",\"TYPE\":\"OrderAddedFat\",\"cause\":\"UNKNOWN\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[0]},\"scheduledAt\":1526556300000,\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556300072000,\"timestamp\":1526556300016,\"key\":\"/re_test@2018-05-17T11:25:00Z\",\"TYPE\":\"OrderProcessingStartedFat\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[0]},\"agentUri\":\"http://localhost:4445\",\"jobPath\":\"/test_1\",\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556300193000,\"timestamp\":1526556300143,\"key\":\"/re_test@2018-05-17T11:25:00Z\",\"TYPE\":\"OrderStdoutWrittenFat\",\"chunk\":\"test_1 line 1 - 13:25:00,13\\r\\ntest_1 line 3 - 13:25:00,13\\r\\n\"},{\"eventId\":1526556300359000,\"timestamp\":1526556300171,\"key\":\"/re_test@2018-05-17T11:25:00Z\",\"TYPE\":\"OrderProcessedFat\",\"outcome\":{\"TYPE\":\"Succeeded\",\"returnCode\":0},\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556300361001,\"timestamp\":1526556300237,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_1\",\"TYPE\":\"OrderAddedFat\",\"parent\":\"/re_test@2018-05-17T11:25:00Z\",\"cause\":\"Forked\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[1,\"fork_1\",0]},\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556300361001,\"timestamp\":1526556300237,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_2\",\"TYPE\":\"OrderAddedFat\",\"parent\":\"/re_test@2018-05-17T11:25:00Z\",\"cause\":\"Forked\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[1,\"fork_2\",0]},\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556300363000,\"timestamp\":1526556300265,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_1\",\"TYPE\":\"OrderProcessingStartedFat\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[1,\"fork_1\",0]},\"agentUri\":\"http://localhost:4445\",\"jobPath\":\"/test_2\",\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556300363001,\"timestamp\":1526556300266,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_2\",\"TYPE\":\"OrderProcessingStartedFat\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[1,\"fork_2\",0]},\"agentUri\":\"http://localhost:4445\",\"jobPath\":\"/test_4\",\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556300502000,\"timestamp\":1526556300312,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_1\",\"TYPE\":\"OrderStdoutWrittenFat\",\"chunk\":\"test_2 line 1 - 13:25:00,31\\r\\ntest_2 line 2 - 13:25:00,31\\r\\n\"},{\"eventId\":1526556300503000,\"timestamp\":1526556300337,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_2\",\"TYPE\":\"OrderStdoutWrittenFat\",\"chunk\":\"test_4 - 13:25:00,32\\r\\n\"},{\"eventId\":1526556300503001,\"timestamp\":1526556300337,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_1\",\"TYPE\":\"OrderStdoutWrittenFat\",\"chunk\":\"test_2 line 3 - 13:25:00,31\\r\\n\"},{\"eventId\":1526556300503002,\"timestamp\":1526556300356,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_2\",\"TYPE\":\"OrderProcessedFat\",\"outcome\":{\"TYPE\":\"Succeeded\",\"returnCode\":0},\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556300504000,\"timestamp\":1526556300357,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_1\",\"TYPE\":\"OrderProcessedFat\",\"outcome\":{\"TYPE\":\"Succeeded\",\"returnCode\":0},\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556300504003,\"timestamp\":1526556300412,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_2\",\"TYPE\":\"OrderProcessingStartedFat\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[1,\"fork_2\",1]},\"agentUri\":\"http://localhost:4445\",\"jobPath\":\"/test_5\",\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556300505000,\"timestamp\":1526556300413,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_1\",\"TYPE\":\"OrderProcessingStartedFat\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[1,\"fork_1\",1]},\"agentUri\":\"http://localhost:4445\",\"jobPath\":\"/test_3\",\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556300632000,\"timestamp\":1526556300485,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_2\",\"TYPE\":\"OrderStdoutWrittenFat\",\"chunk\":\"test_5 - 13:25:00,47\\r\\n\"},{\"eventId\":1526556300632001,\"timestamp\":1526556300500,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_2\",\"TYPE\":\"OrderProcessedFat\",\"outcome\":{\"TYPE\":\"Succeeded\",\"returnCode\":0},\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556300633000,\"timestamp\":1526556300523,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_1\",\"TYPE\":\"OrderStdoutWrittenFat\",\"chunk\":\"test_3 - 13:25:00,49\\r\\n\"},{\"eventId\":1526556300635000,\"timestamp\":1526556300567,\"key\":\"/re_test@2018-05-17T11:25:00Z/fork_1\",\"TYPE\":\"OrderProcessedFat\",\"outcome\":{\"TYPE\":\"Succeeded\",\"returnCode\":0},\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556301009000,\"timestamp\":1526556300845,\"key\":\"/re_test@2018-05-17T11:25:00Z\",\"TYPE\":\"OrderProcessingStartedFat\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[2]},\"agentUri\":\"http://localhost:4445\",\"jobPath\":\"/test_6\",\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556301009001,\"timestamp\":1526556300890,\"key\":\"/re_test@2018-05-17T11:25:00Z\",\"TYPE\":\"OrderStdoutWrittenFat\",\"chunk\":\"test_6 - 13:25:00,89\\r\\n\"},{\"eventId\":1526556301010000,\"timestamp\":1526556300911,\"key\":\"/re_test@2018-05-17T11:25:00Z\",\"TYPE\":\"OrderProcessedFat\",\"outcome\":{\"TYPE\":\"Succeeded\",\"returnCode\":0},\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526556301118000,\"key\":\"/re_test@2018-05-17T11:25:00Z\",\"TYPE\":\"OrderFinishedFat\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[3]}}]}";
        Event feNe = mapper.readValue(notEmptyEvent, Event.class);

        System.out.println("Type: " + feNe.getType());
        System.out.println("LastEventId: " + feNe.getLastEventId());

        List<IEntry> ses = (List<IEntry>) feNe.getStamped();
        System.out.println("Stampeds size: " + ses.size());
        int i = 1;
        for (IEntry en : ses) {
            Entry entry = (Entry) en;

            System.out.println(i + ") Entry: " + entry);
            System.out.println("      Type: " + entry.getType());
            System.out.println("      eventId: " + entry.getEventId());
            System.out.println("      eventIdAsDate: " + entry.getEventIdAsDate());
            System.out.println("      timestamp: " + entry.getTimestamp());
            System.out.println("      timestampAsDate: " + entry.getTimestampAsDate());
            System.out.println("      key: " + entry.getKey());
            System.out.println("      parent: " + entry.getParent());
            System.out.println("      agentUri: " + entry.getAgentUri());
            System.out.println("      jobPath: " + entry.getJobPath());
            System.out.println("      chunk: " + entry.getChunk());

            if (entry.getWorkflowPosition() != null) {
                System.out.println("      WorkflowPosition: " + entry.getWorkflowPosition());
                System.out.println("            WorkflowId: " + entry.getWorkflowPosition().getWorkflowId());
                System.out.println("                  path: " + entry.getWorkflowPosition().getWorkflowId().getPath());
                System.out.println("                  versionId: " + entry.getWorkflowPosition().getWorkflowId().getVersionId());
                System.out.println("            position: " + entry.getWorkflowPosition().getPosition());
                System.out.println("            positionAsString: " + entry.getWorkflowPosition().getPositionAsString());
            }
            if (entry.getOutcome() != null) {
                System.out.println("      Outcome: " + entry.getOutcome());
                System.out.println("            type: " + entry.getOutcome().getType());
                System.out.println("            returnCode: " + entry.getOutcome().getReturnCode());
            }
            if (entry.getVariables() != null) {
                System.out.println("      Variables: " + entry.getVariables());
            }
            i++;
        }
        System.out.println("-----");

    }

    public static void main(String[] args) throws Exception {
        EventTest t = new EventTest();

        ObjectMapper mapper = t.getObjectMapper(Entry.class);
        t.readEmptyEvent(mapper);
        t.readNotEmptyEvent(mapper);

    }

}
