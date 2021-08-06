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
        System.out.println(Globals.prettyPrintObjectMapper.writeValueAsString(o));
    }
    
    @Test
    public void SerializerTest() throws IOException {
        String json = "{\"TYPE\": \"Workflow\", \"title\":\"\", \"jobResourceNames\": [\"\"], \"jobs\": {\"job1\": {\"agentName\": \"secondaryAgent\", \"parallelism\": 100, \"executable\": {\"TYPE\": \"ShellScriptExecutable\", \"script\": \"echo hallo\", \"v1Compatible\": false}, \"failOnErrWritten\": false}, \"transferFile\": {\"agentName\": \"primaryAgent\", \"parallelism\": 100, \"executable\": {\"env\": {}, \"TYPE\": \"ShellScriptExecutable\", \"ReturnCodeMeaning\": {}, \"script\": \"echo hello\", \"v1Compatible\": false}, \"failOnErrWritten\": false}}, \"versionId\": \"38752807-de25-4eb6-b132-1189a9da8cd0\", \"instructions\": [{\"TYPE\": \"Execute.Named\", \"label\": \"job1\", \"jobName\": \"job1\"}, {\"TYPE\": \"Execute.Named\", \"label\": \"transferFile\", \"jobName\": \"transferFile\"}], \"orderRequirements\": {\"parameters\": {\"yade_bin\": {\"type\": \"String\", \"default\": \"/var/sos-berlin.com/yade/bin/jade.sh\"}, \"yade_profile\": {\"type\": \"String\", \"default\": \"product_demo_from_galadriel_sftp\"}, \"yade_settings\": {\"type\": \"String\", \"default\": \"./config/yade.xml\"}, \"yade_java_options\": {\"type\": \"String\", \"default\": \"-Xmx32m\"}}}}";
        Workflow w = Globals.objectMapper.readValue(json, Workflow.class);
        String result = JsonSerializer.serializeAsString(w);
        System.out.println(result);
        String expected = "{\"TYPE\":\"Workflow\",\"version\":\"1.0.0\",\"versionId\":\"38752807-de25-4eb6-b132-1189a9da8cd0\",\"orderPreparation\":{\"parameters\":{\"yade_bin\":{\"type\":\"String\",\"default\":\"'/var/sos-berlin.com/yade/bin/jade.sh'\"},\"yade_profile\":{\"type\":\"String\",\"default\":\"'product_demo_from_galadriel_sftp'\"},\"yade_settings\":{\"type\":\"String\",\"default\":\"'./config/yade.xml'\"},\"yade_java_options\":{\"type\":\"String\",\"default\":\"'-Xmx32m'\"}}},\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"job1\",\"label\":\"job1\"},{\"TYPE\":\"Execute.Named\",\"jobName\":\"transferFile\",\"label\":\"transferFile\"}],\"jobs\":{\"job1\":{\"agentName\":\"secondaryAgent\",\"executable\":{\"TYPE\":\"ShellScriptExecutable\",\"script\":\"echo hallo\"},\"parallelism\":100},\"transferFile\":{\"agentName\":\"primaryAgent\",\"executable\":{\"TYPE\":\"ShellScriptExecutable\",\"script\":\"echo hello\"},\"parallelism\":100}}}";
        System.out.println(expected);
        assertEquals("SerializerTest", expected, result);
    }
    
//    @Test
//    public void mapVariables() throws JsonParseException, JsonMappingException, IOException {
//        String json = "{\"myString\": \"stringValue\",\"myNumber\": 3.14,\"myBoolean\": true,\"myList\": [{\"a\":\"var1\"},{\"a\":\"var2\"}]}\"";
//        Variables vars = Globals.objectMapper.readValue(json, Variables.class);
//        Map<String, Value> controllerVars = variablesToScalaValuedArguments(vars);
//        System.out.println(controllerVars.toString());
//    }
//    
//    
//    public static Map<String, Value> variablesToScalaValuedArguments(Variables vars) {
//        Map<String, Value> arguments = new HashMap<>();
//        if (vars != null) {
//            arguments = variablesToScalaValuedArguments(vars.getAdditionalProperties());
//        }
//        return arguments;
//    }
//    
//    public static Map<String, Value> variablesToScalaValuedArguments(Map<String, Object> vars) {
//        Map<String, Value> arguments = new HashMap<>();
//        if (vars != null) {
//            vars.forEach((key, val) -> {
//                if (val instanceof String) {
//                    arguments.put(key, StringValue.of((String) val));
//                } else if (val instanceof Boolean) {
//                    arguments.put(key, BooleanValue.of((Boolean) val));
//                } else if (val instanceof Integer) {
//                    arguments.put(key, NumberValue.of((Integer) val));
//                } else if (val instanceof Long) {
//                    arguments.put(key, NumberValue.of((Long) val));
//                } else if (val instanceof Double) {
//                    arguments.put(key, NumberValue.of(BigDecimal.valueOf((Double) val)));
//                } else if (val instanceof BigDecimal) {
//                    arguments.put(key, NumberValue.of(((BigDecimal) val)));
//                } else if (val instanceof List) {
//                    @SuppressWarnings("unchecked")
//                    List<Map<String, Object>> valList = (List<Map<String, Object>>) val;
//                    List<Value> map = new ArrayList<>();
//                    valList.forEach(m -> {
//                        Map<String, Value> listArguments = variablesToScalaValuedArguments(m);
//                        // ObjectValue.of(...map...) missing
//                        map.add(ObjectValue.apply(toScalaMap(listArguments)));
//                    });
//                    arguments.put(key, ListValue.of(map));
//                }
//            });
//        }
//        return arguments;
//    }
//    
//    public static scala.collection.immutable.Map<String, Value> toScalaMap(Map<String, Value> jmap) {
//        return scala.collection.immutable.Map.from(scala.jdk.CollectionConverters.MapHasAsScala(jmap).asScala());
//    }

}
