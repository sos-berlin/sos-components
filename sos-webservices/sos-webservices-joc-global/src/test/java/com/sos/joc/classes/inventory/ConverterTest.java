package com.sos.joc.classes.inventory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.sign.model.job.ExecutableScript;
import com.sos.sign.model.job.JobReturnCode;
import com.sos.sign.model.workflow.OrderPreparation;


public class ConverterTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConverterTest.class);
    private static final String jsonTemplate =
            "{\"TYPE\": \"Workflow\",\"jobs\":{\"job1\":{\"executable\":{\"TYPE\": \"ShellScriptExecutable\", \"returnCodeMeaning\": %s}}}}";
    private static final String invOrderPreparation =
            "{\"parameters\":{\"myOptionalNumberVar\":{\"type\":\"Number\",\"default\":\"0\"},\"myFinalVar\":{\"final\":\"'hello'\"},\"myRequiredStringVar\":{\"type\":\"String\"},\"myListVar\":{\"type\":\"List\",\"listParameters\":{\"myListVar2\":{\"type\":\"String\"},\"myListVar1\":{\"type\":\"Number\"}}}},\"allowUndeclared\":false}";
    private static final String signOrderPreparation =
            "{\"parameters\":{\"myOptionalNumberVar\":{\"type\":\"Number\",\"default\":\"0\"},\"myFinalVar\":{\"final\":\"'hello'\"},\"myRequiredStringVar\":{\"type\":\"String\"},\"myListVar\":{\"type\":{\"TYPE\":\"List\",\"elementType\":{\"TYPE\":\"Object\",\"myListVar2\":\"String\",\"myListVar1\":\"Number\"}}}},\"allowUndeclared\":false}";

    private static final List<String> jsons = Arrays.asList(
            String.format(jsonTemplate, "{\"warning\": [1,2]}"),
            String.format(jsonTemplate, "{\"success\": [1,3],\"warning\": [1,2]}"),
            String.format(jsonTemplate, "{\"success\": \"6,0..10, 42\",\"warning\": [1,2]}"),
            String.format(jsonTemplate, "{\"success\": \"6,0..10, 42\",\"warning\": \"7..12\"}"),
            String.format(jsonTemplate, "{\"failure\": [1,3],\"warning\": [1,2]}"),
            String.format(jsonTemplate, "{\"failure\": \"1..10, 42\",\"warning\": [1,3]}"),
            String.format(jsonTemplate, "{\"failure\": \"1..100, 42\",\"warning\": \"7..9, 70..85, 80..90, 97..120\"}"),
            String.format(jsonTemplate, "{\"failure\": \"1..10, 42\",\"warning\": \"7..9, 40..100\"}"));

    private static final List<JobReturnCode> expectations = Arrays.asList(
            new JobReturnCode(Arrays.asList(0, 1, 2), null), 
            new JobReturnCode(Arrays.asList(1, 2, 3), null), 
            new JobReturnCode("0..10,42", null), 
            new JobReturnCode("0..12,42", null), 
            new JobReturnCode(null, "3"), 
            new JobReturnCode(null, "2,4..10,42"), 
            new JobReturnCode(null, "1..6,10..69,91..96"), 
            new JobReturnCode(null, "1..6,10"));
    
    private void testTemplate(int i) {
        try {
            com.sos.sign.model.workflow.Workflow signWorkflow = Globals.objectMapper.readValue(jsons.get(i), com.sos.sign.model.workflow.Workflow.class);
            Workflow invWorkflow = Globals.objectMapper.readValue(jsons.get(i), Workflow.class);
            JsonConverter.considerReturnCodeWarningsAndSubagentClusterId(invWorkflow.getJobs(), signWorkflow.getJobs());
            // System.out.println(Globals.prettyPrintObjectMapper.writeValueAsString(signWorkflow));
            ExecutableScript es = signWorkflow.getJobs().getAdditionalProperties().get("job1").getExecutable().cast();
            LOGGER.trace(es.getReturnCodeMeaning() + "==" + expectations.get(i) + "?");
            assertTrue(es.getReturnCodeMeaning().equals(expectations.get(i)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testReturnCodeWarnings1() {
        testTemplate(0);
    }
    
    @Test
    public void testReturnCodeWarnings2() {
        testTemplate(1);
    }
    
    @Test
    public void testReturnCodeWarnings3() {
        testTemplate(2);
    }
    
    @Test
    public void testReturnCodeWarnings4() {
        testTemplate(3);
    }
    
    @Test
    public void testReturnCodeWarnings5() {
        testTemplate(4);
    }
    
    @Test
    public void testReturnCodeWarnings6() {
        testTemplate(5);
    }
    
    @Test
    public void testReturnCodeWarnings7() {
        testTemplate(6);
    }
    
    @Test
    public void testReturnCodeWarnings8() {
        testTemplate(7);
    }
    
    @Test
    public void signOrderPrepToInvOrderPrep() throws JsonParseException, JsonMappingException, IOException {
        OrderPreparation conf = Globals.objectMapper.readValue(signOrderPreparation, OrderPreparation.class);
        Requirements r = JsonConverter.signOrderPreparationToInvOrderPreparation(conf); 
        String result = Globals.objectMapper.writeValueAsString(r);
//        System.out.println(invOrderPreparation);
//        System.out.println(result);
        assertEquals("signOrderPrepToInvOrderPrep", result, invOrderPreparation);
    }
    
    @Test
    public void invOrderPrepToSignOrderPrep() throws JsonParseException, JsonMappingException, IOException {
        Requirements conf = Globals.objectMapper.readValue(invOrderPreparation, Requirements.class);
        OrderPreparation op = JsonConverter.invOrderPreparationToSignOrderPreparation(conf, null);
        String result = Globals.objectMapper.writeValueAsString(op);
//        System.out.println(result);
        assertEquals("invOrderPrepToSignOrderPrep", result, signOrderPreparation);
    }

}
