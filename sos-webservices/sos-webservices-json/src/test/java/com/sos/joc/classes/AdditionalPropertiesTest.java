package com.sos.joc.classes;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.jobscheduler.model.common.Variables;
import com.sos.jobscheduler.model.instruction.IfElse;
import com.sos.jobscheduler.model.instruction.NamedJob;
import com.sos.jobscheduler.model.instruction.Retry;
import com.sos.jobscheduler.model.order.FreshOrder;

public class AdditionalPropertiesTest {
	
	private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void freshOrderTest() throws Exception {
        Variables vars = new Variables();
        FreshOrder order = new FreshOrder();
        order.setWorkflowPath("/test");
        order.setArguments(vars);
        vars.setAdditionalProperty("hallo", "welt");
        vars.setAdditionalProperty("hello", "world");
//        System.out.println(new ObjectMapper().writeValueAsString(order));
        String expected = "{\"workflowPath\":\"/test\",\"variables\":{\"hallo\":\"welt\",\"hello\":\"world\"}}";
        assertEquals("freshOrderTest", expected, objectMapper.writeValueAsString(order));
    }
    
    @Test
    public void ifElseTest() throws Exception {
        IfElse ifElse = new IfElse();
        ifElse.setPredicate("true");
//        System.out.println(new ObjectMapper().writeValueAsString(ifElse));
        String expected = "{\"TYPE\":\"If\",\"predicate\":\"true\"}";
        assertEquals("ifElseTest", expected, objectMapper.writeValueAsString(ifElse));
    }
    
    @Test
    public void retryTest() throws Exception {
        Retry retry = new Retry();
        NamedJob job = new NamedJob();
        job.setJobName("TEST");	
        retry.setTry(new ArrayList<>(Arrays.asList(job)));
//        System.out.println(new ObjectMapper().writeValueAsString(retry));
        String expected = "{\"TYPE\":\"Try\",\"try\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"TEST\"}],\"catch\":[{\"TYPE\":\"Retry\"}]}";
        assertEquals("retryTest", expected, objectMapper.writeValueAsString(retry));
    }

}
