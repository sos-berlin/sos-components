package com.sos.joc.classes.inventory;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.controller.model.order.OrderItem;
import com.sos.controller.model.workflow.HistoricOutcome;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.OrdersHelper;
import com.sos.joc.model.order.OrderV;


public class OrderMappingTest {

    @Test
    public void test() throws JsonParseException, JsonMappingException, IOException {
        Path order = Paths.get("src/test/resources/failedOrder.json");
        OrderItem oItem = Globals.objectMapper.readValue(Files.readAllBytes(order), OrderItem.class);
        OrderV o = new OrderV();
        o.setArguments(oItem.getArguments());
        o.setAttachedState(oItem.getAttachedState());
        o.setOrderId(oItem.getId());
        List<HistoricOutcome> outcomes = oItem.getHistoricOutcomes();
        if (outcomes != null && !outcomes.isEmpty()) {
            o.setLastOutcome(outcomes.get(outcomes.size() - 1).getOutcome());
        }
        o.setHistoricOutcome(outcomes);
        o.setPosition(oItem.getWorkflowPosition().getPosition());
        Long scheduledFor = oItem.getScheduledFor();
        o.setState(OrdersHelper.getState(oItem.getState().getTYPE(), oItem.getIsSuspended()));
        o.setScheduledFor(scheduledFor);
        o.setWorkflowId(oItem.getWorkflowPosition().getWorkflowId());
        System.out.println(Globals.objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true).writeValueAsString(o));
    }
    
    @Test
    public void SerializerTest() throws IOException {
        String json = "{\"TYPE\": \"Workflow\", \"title\":\"\", \"jobResourceNames\": [\"\"], \"jobs\": {\"job1\": {\"agentName\": \"secondaryAgent\", \"taskLimit\": 100, \"executable\": {\"TYPE\": \"ScriptExecutable\", \"script\": \"echo hallo\", \"v1Compatible\": false}, \"failOnErrWritten\": false}, \"transferFile\": {\"agentName\": \"primaryAgent\", \"taskLimit\": 100, \"executable\": {\"env\": {}, \"TYPE\": \"ScriptExecutable\", \"ReturnCodeMeaning\": {}, \"script\": \"echo hello\", \"v1Compatible\": false}, \"failOnErrWritten\": false}}, \"versionId\": \"38752807-de25-4eb6-b132-1189a9da8cd0\", \"instructions\": [{\"TYPE\": \"Execute.Named\", \"label\": \"job1\", \"jobName\": \"job1\"}, {\"TYPE\": \"Execute.Named\", \"label\": \"transferFile\", \"jobName\": \"transferFile\"}], \"orderRequirements\": {\"parameters\": {\"yade_bin\": {\"type\": \"String\", \"default\": \"/var/sos-berlin.com/yade/bin/jade.sh\"}, \"yade_profile\": {\"type\": \"String\", \"default\": \"product_demo_from_galadriel_sftp\"}, \"yade_settings\": {\"type\": \"String\", \"default\": \"./config/yade.xml\"}, \"yade_java_options\": {\"type\": \"String\", \"default\": \"-Xmx32m\"}}}}";
        Workflow w = (Globals.objectMapper.readValue(json, Workflow.class));
        String result = JsonSerializer.writeValueAsString(w);
        System.out.println(result);
        String expected = "{\"TYPE\":\"Workflow\",\"versionId\":\"38752807-de25-4eb6-b132-1189a9da8cd0\",\"orderRequirements\":{\"parameters\":{\"yade_bin\":{\"type\":\"String\",\"default\":\"/var/sos-berlin.com/yade/bin/jade.sh\"},\"yade_profile\":{\"type\":\"String\",\"default\":\"product_demo_from_galadriel_sftp\"},\"yade_settings\":{\"type\":\"String\",\"default\":\"./config/yade.xml\"},\"yade_java_options\":{\"type\":\"String\",\"default\":\"-Xmx32m\"}}},\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"job1\",\"label\":\"job1\"},{\"TYPE\":\"Execute.Named\",\"jobName\":\"transferFile\",\"label\":\"transferFile\"}],\"jobs\":{\"job1\":{\"agentName\":\"secondaryAgent\",\"executable\":{\"TYPE\":\"ScriptExecutable\",\"script\":\"echo hallo\"},\"taskLimit\":100},\"transferFile\":{\"agentName\":\"primaryAgent\",\"executable\":{\"TYPE\":\"ScriptExecutable\",\"script\":\"echo hello\"},\"taskLimit\":100}}}";
        assertEquals("SerializerTest", expected, result);
    }

}
