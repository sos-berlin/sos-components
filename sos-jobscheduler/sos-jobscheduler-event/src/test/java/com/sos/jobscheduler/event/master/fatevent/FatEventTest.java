package com.sos.jobscheduler.event.master.fatevent;

import java.util.List;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FatEventTest {

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        //mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
        // mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        String emptyEvent = "{\"TYPE\":\"Empty\",\"lastEventId\":1526370922642000}";
        Event feE = mapper.readValue(emptyEvent, Event.class);

        System.out.println(feE.getType());
        System.out.println(feE.getLastEventId());
        System.out.println("-----");

        String notEmptyEvent =
                "{\"TYPE\":\"NonEmpty\",\"stampeds\":[{\"eventId\":1526377800096000,\"timestamp\":1526377800023,\"key\":\"/re_test@2018-05-15T09:50:00Z\",\"TYPE\":\"OrderProcessingStartedFat\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[0]},\"agentUri\":\"http://localhost:4445\",\"jobPath\":\"/test_1\",\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526377800214000,\"timestamp\":1526377800168,\"key\":\"/re_test@2018-05-15T09:50:00Z\",\"TYPE\":\"OrderStdoutWrittenFat\",\"chunk\":\"test_1 - 11:50:00,16\\r\\n\"},{\"eventId\":1526377800397000,\"timestamp\":1526377800195,\"key\":\"/re_test@2018-05-15T09:50:00Z\",\"TYPE\":\"OrderProcessedFat\",\"outcome\":{\"TYPE\":\"Succeeded\",\"returnCode\":0},\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526377800399001,\"timestamp\":1526377800296,\"key\":\"/re_test@2018-05-15T09:50:00Z/fork_1\",\"TYPE\":\"OrderAddedFat\",\"parent\":\"/re_test@2018-05-15T09:50:00Z\",\"cause\":\"Forked\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[1,\"fork_1\",0]},\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526377800399001,\"timestamp\":1526377800296,\"key\":\"/re_test@2018-05-15T09:50:00Z/fork_2\",\"TYPE\":\"OrderAddedFat\",\"parent\":\"/re_test@2018-05-15T09:50:00Z\",\"cause\":\"Forked\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[1,\"fork_2\",0]},\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526377800516000,\"timestamp\":1526377800344,\"key\":\"/re_test@2018-05-15T09:50:00Z/fork_2\",\"TYPE\":\"OrderProcessingStartedFat\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[1,\"fork_2\",0]},\"agentUri\":\"http://localhost:4445\",\"jobPath\":\"/test_4\",\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526377800517001,\"timestamp\":1526377800364,\"key\":\"/re_test@2018-05-15T09:50:00Z/fork_1\",\"TYPE\":\"OrderProcessingStartedFat\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[1,\"fork_1\",0]},\"agentUri\":\"http://localhost:4445\",\"jobPath\":\"/test_2\",\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526377800517002,\"timestamp\":1526377800410,\"key\":\"/re_test@2018-05-15T09:50:00Z/fork_2\",\"TYPE\":\"OrderStdoutWrittenFat\",\"chunk\":\"test_4 - 11:50:00,41\\r\\n\"},{\"eventId\":1526377800517003,\"timestamp\":1526377800441,\"key\":\"/re_test@2018-05-15T09:50:00Z/fork_1\",\"TYPE\":\"OrderStdoutWrittenFat\",\"chunk\":\"test_2 - 11:50:00,42\\r\\n\"},{\"eventId\":1526377800517004,\"timestamp\":1526377800464,\"key\":\"/re_test@2018-05-15T09:50:00Z/fork_2\",\"TYPE\":\"OrderProcessedFat\",\"outcome\":{\"TYPE\":\"Succeeded\",\"returnCode\":0},\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526377800518000,\"timestamp\":1526377800464,\"key\":\"/re_test@2018-05-15T09:50:00Z/fork_1\",\"TYPE\":\"OrderProcessedFat\",\"outcome\":{\"TYPE\":\"Succeeded\",\"returnCode\":0},\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}}]}";
        notEmptyEvent =
                "{\"TYPE\":\"NonEmpty\",\"stampeds\":[{\"eventId\":1526377800645002,\"timestamp\":1526377800510,\"key\":\"/re_test@2018-05-15T09:50:00Z/fork_1\",\"TYPE\":\"OrderProcessingStartedFat\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[1,\"fork_1\",1]},\"agentUri\":\"http://localhost:4445\",\"jobPath\":\"/test_3\",\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526377800646000,\"timestamp\":1526377800510,\"key\":\"/re_test@2018-05-15T09:50:00Z/fork_2\",\"TYPE\":\"OrderProcessingStartedFat\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[1,\"fork_2\",1]},\"agentUri\":\"http://localhost:4445\",\"jobPath\":\"/test_5\",\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526377800646001,\"timestamp\":1526377800586,\"key\":\"/re_test@2018-05-15T09:50:00Z/fork_1\",\"TYPE\":\"OrderStdoutWrittenFat\",\"chunk\":\"test_3 - 11:50:00,58\\r\\n\"}]}";
        notEmptyEvent =
                "{\"TYPE\":\"NonEmpty\",\"stampeds\":[{\"eventId\":1526377800767000,\"timestamp\":1526377800608,\"key\":\"/re_test@2018-05-15T09:50:00Z/fork_2\",\"TYPE\":\"OrderStdoutWrittenFat\",\"chunk\":\"test_5 - 11:50:00,59\\r\\n\"},{\"eventId\":1526377800767001,\"timestamp\":1526377800609,\"key\":\"/re_test@2018-05-15T09:50:00Z/fork_1\",\"TYPE\":\"OrderProcessedFat\",\"outcome\":{\"TYPE\":\"Succeeded\",\"returnCode\":0},\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526377800767002,\"timestamp\":1526377800653,\"key\":\"/re_test@2018-05-15T09:50:00Z/fork_2\",\"TYPE\":\"OrderProcessedFat\",\"outcome\":{\"TYPE\":\"Succeeded\",\"returnCode\":0},\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526377801174000,\"timestamp\":1526377801042,\"key\":\"/re_test@2018-05-15T09:50:00Z\",\"TYPE\":\"OrderProcessingStartedFat\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[2]},\"agentUri\":\"http://localhost:4445\",\"jobPath\":\"/test_6\",\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526377801174001,\"timestamp\":1526377801119,\"key\":\"/re_test@2018-05-15T09:50:00Z\",\"TYPE\":\"OrderStdoutWrittenFat\",\"chunk\":\"test_6 - 11:50:01,11\\r\\n\"},{\"eventId\":1526377801315000,\"timestamp\":1526377801143,\"key\":\"/re_test@2018-05-15T09:50:00Z\",\"TYPE\":\"OrderProcessedFat\",\"outcome\":{\"TYPE\":\"Succeeded\",\"returnCode\":0},\"variables\":{\"TEST-VARIABLE_1\":\"TEST-VALUE_1\",\"TEST-VARIABLE_2\":\"TEST-VALUE_2\"}},{\"eventId\":1526377801406000,\"key\":\"/re_test@2018-05-15T09:50:00Z\",\"TYPE\":\"OrderFinishedFat\",\"workflowPosition\":{\"workflowId\":{\"path\":\"/re_test\",\"versionId\":\"(initial)\"},\"position\":[3]}}]}";

        Event feNe = mapper.readValue(notEmptyEvent, Event.class);

        System.out.println("Type: " + feNe.getType());
        System.out.println("LastEventId: " + feNe.getLastEventId());

        List<Entry> ses = feNe.getStampeds();
        System.out.println("Stampeds size: " + ses.size());
        int i = 1;
        for (Entry entry : ses) {
            System.out.println("    " + i + ") Type: " + entry.getType());
            System.out.println("            eventId: " + entry.getEventId());
            System.out.println("            timestamp: " + entry.getTimestamp());
            System.out.println("            key: " + entry.getKey());
            System.out.println("            parent: " + entry.getParent());
            System.out.println("            agentUri: " + entry.getAgentUri());
            System.out.println("            jobPath: " + entry.getJobPath());
            System.out.println("            chunk: " + entry.getChunk());

            if (entry.getWorkflowPosition() != null) {
                System.out.println("            WorkflowPosition");
                System.out.println("                   path: " + entry.getWorkflowPosition().getWorkflowId().getPath());
                System.out.println("                   versionId: " + entry.getWorkflowPosition().getWorkflowId().getVersionId());
                System.out.println("                   position: " + entry.getWorkflowPosition().getPosition());
            }
            if (entry.getOutcome() != null) {
                System.out.println("            Outcome");
                System.out.println("                   type: " + entry.getOutcome().getType());
                System.out.println("                   returnCode: " + entry.getOutcome().getReturnCode());
            }
            if (entry.getVariables() != null) {
                System.out.println("            Variables");
                System.out.println("                   vars: " + entry.getVariables());
            }
            i++;
        }
        System.out.println("-----");

    }

}
