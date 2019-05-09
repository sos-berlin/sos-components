package com.sos.joc.classes;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.jobscheduler.model.command.CancelOrder;
import com.sos.jobscheduler.model.command.ICommandable;
import com.sos.jobscheduler.model.command.JSBatchCommands;
import com.sos.jobscheduler.model.common.Variables;
import com.sos.jobscheduler.model.instruction.IfElse;
import com.sos.jobscheduler.model.instruction.NamedJob;
import com.sos.jobscheduler.model.instruction.Retry;
import com.sos.jobscheduler.model.order.FreshOrder;
import com.sos.jobscheduler.model.order.OrderMode;
import com.sos.jobscheduler.model.order.OrderModeType;

public class PojosTest {
	
	private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void freshOrderTest() throws Exception {
        Variables vars = new Variables();
        FreshOrder order = new FreshOrder();
        order.setWorkflowPath("/test");
        order.setScheduledFor(1488888000000L);
        order.setArguments(vars);
        vars.setAdditionalProperty("hallo", "welt");
        vars.setAdditionalProperty("hello", "world");
//        System.out.println(objectMapper.writeValueAsString(order));
        String expected = "{\"workflowPath\":\"/test\",\"scheduledFor\":1488888000000,\"arguments\":{\"hallo\":\"welt\",\"hello\":\"world\"}}";
        assertEquals("freshOrderTest", expected, objectMapper.writeValueAsString(order));
    }
    
    @Test
    public void ifElseTest() throws Exception {
        IfElse ifElse = new IfElse();
        ifElse.setPredicate("true");
//        System.out.println(objectMapper.writeValueAsString(ifElse));
        String expected = "{\"TYPE\":\"If\",\"predicate\":\"true\"}";
        assertEquals("ifElseTest", expected, objectMapper.writeValueAsString(ifElse));
    }
    
    @Test
    public void retryTest() throws Exception {
		Retry retry = new Retry();
        NamedJob job = new NamedJob();
        job.setJobName("TEST");	
        retry.setTry(Arrays.asList(job));
        retry.setRetryDelays(Arrays.asList(30, 150));
        retry.setMaxTries(3);
        System.out.println(objectMapper.writeValueAsString(retry));
        String expected = "{\"TYPE\":\"Try\",\"try\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"TEST\"}],\"catch\":[{\"TYPE\":\"Retry\"}],\"maxTries\":3,\"retryDelays\":[30,150]}";
        assertEquals("retryTest", expected, objectMapper.writeValueAsString(retry));
    }
    
	@Test
	public void batchCommandTest() throws Exception {
		JSBatchCommands batch = new JSBatchCommands();
		batch.setCommands(new ArrayList<ICommandable>());

		CancelOrder cancelnotStartedOrder = new CancelOrder();
		cancelnotStartedOrder.setOrderId("TEST-NOT_STARTED-ORDER");
		//"mode":{"TYPE":"NotStarted"} is default
		CancelOrder cancelFreshOrStartedOrder = new CancelOrder();
		OrderMode orderMode = cancelFreshOrStartedOrder.getMode();
		orderMode.setTYPE(OrderModeType.FRESH_OR_STARTED);
		cancelFreshOrStartedOrder.setMode(orderMode);
		cancelFreshOrStartedOrder.setOrderId("TEST-FRESH_OR_STARTED-ORDER");
		batch.setCommands(Arrays.asList(cancelnotStartedOrder, cancelFreshOrStartedOrder));
//		System.out.println(objectMapper.writeValueAsString(batch));
		String expected = "{\"TYPE\":\"Batch\",\"commands\":[{\"TYPE\":\"CancelOrder\",\"orderId\":\"TEST-NOT_STARTED-ORDER\",\"mode\":{\"TYPE\":\"NotStarted\"}},{\"TYPE\":\"CancelOrder\",\"orderId\":\"TEST-FRESH_OR_STARTED-ORDER\",\"mode\":{\"TYPE\":\"FreshOrStarted\"}}]}";
		assertEquals("batchCommandTest", expected, objectMapper.writeValueAsString(batch));
	}

}
