package com.sos.joc.classes;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.jobscheduler.model.command.CancelOrder;
import com.sos.jobscheduler.model.command.JSBatchCommands;
import com.sos.jobscheduler.model.common.Variables;
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.jobscheduler.model.instruction.IfElse;
import com.sos.jobscheduler.model.instruction.InstructionType;
import com.sos.jobscheduler.model.instruction.Instructions;
import com.sos.jobscheduler.model.instruction.NamedJob;
import com.sos.jobscheduler.model.instruction.RetryCatch;
import com.sos.jobscheduler.model.instruction.TryCatch;
import com.sos.jobscheduler.model.order.FreshOrder;
import com.sos.jobscheduler.model.order.OrderMode;
import com.sos.jobscheduler.model.order.OrderModeType;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.model.inventory.JSObject;
import com.sos.schema.JsonValidator;

public class PojosTest {
	
	private ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

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
        IfElse ifElse = new IfElse("true", new Instructions(Collections.emptyList()), null);
        System.out.println(objectMapper.writeValueAsString(ifElse));
        String expected = "{\"TYPE\":\"If\",\"predicate\":\"true\",\"then\":{\"instructions\":[]}}";
        assertEquals("ifElseTest", expected, objectMapper.writeValueAsString(ifElse));
    }
    
    @Test
    public void retryTest() throws Exception {
        RetryCatch retry = new RetryCatch(3, Arrays.asList(30, 150), new Instructions(Arrays.asList(new NamedJob("TEST"))));
        System.out.println(objectMapper.writeValueAsString(retry));
        String expected = "{\"TYPE\":\"Try\",\"maxTries\":3,\"retryDelays\":[30,150],\"try\":{\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"TEST\"}]},\"catch\":{\"instructions\":[{\"TYPE\":\"Retry\"}]}}";
        assertEquals("retryTest", expected, objectMapper.writeValueAsString(retry));
    }
    
    @Test
    public void tryTest() throws Exception {
		NamedJob job = new NamedJob("TEST", null, null);
		TryCatch _try = new TryCatch(new Instructions(Arrays.asList(job)));
        System.out.println(objectMapper.writeValueAsString(_try));
        String expected = "{\"TYPE\":\"Try\",\"try\":{\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"TEST\"}]},\"catch\":{\"instructions\":[]}}";
        assertEquals("retryTest", expected, objectMapper.writeValueAsString(_try));
    }
    
    @Test
	public void batchCommandTest() throws Exception {
		JSBatchCommands batch = new JSBatchCommands();
		//batch.setCommands(new ArrayList<Command>());

		CancelOrder cancelnotStartedOrder = new CancelOrder("TEST-NOT_STARTED-ORDER", new OrderMode(OrderModeType.NOT_STARTED, null));
		CancelOrder cancelFreshOrStartedOrder = new CancelOrder("TEST-FRESH_OR_STARTED-ORDER", new OrderMode(OrderModeType.FRESH_OR_STARTED, null));
		batch.setCommands(Arrays.asList(cancelnotStartedOrder, cancelFreshOrStartedOrder));
//		System.out.println(objectMapper.writeValueAsString(batch));
		String expected = "{\"TYPE\":\"Batch\",\"commands\":[{\"TYPE\":\"CancelOrder\",\"orderId\":\"TEST-NOT_STARTED-ORDER\",\"mode\":{\"TYPE\":\"NotStarted\"}},{\"TYPE\":\"CancelOrder\",\"orderId\":\"TEST-FRESH_OR_STARTED-ORDER\",\"mode\":{\"TYPE\":\"FreshOrStarted\"}}]}";
		assertEquals("batchCommandTest", expected, objectMapper.writeValueAsString(batch));
	}
	
	@Test
	public void readRetryInWorkflowTest() throws Exception {
		String json = "{\"TYPE\":\"Workflow\",\"path\":\"/test/RetryWorkflow\",\"versionId\":\"2.0.0-SNAPSHOT\",\"instructions\":[{\"TYPE\":\"Try\",\"try\":{\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"TEST\"}]},\"catch\":{\"instructions\":[{\"TYPE\":\"Retry\"}]},\"maxTries\":3,\"retryDelays\":[30,150]}]}";
		Workflow workflow = objectMapper.readValue(json, Workflow.class);
		final List<Integer> maxTries = new ArrayList<Integer>();
		workflow.getInstructions().stream().filter(instr -> instr.getTYPE() == InstructionType.TRY && instr.isRetry()).forEach(instr -> {
			try {
				System.out.println(objectMapper.writeValueAsString(instr));
				RetryCatch retry = instr.cast();
				System.out.println(objectMapper.writeValueAsString(retry));
				TryCatch _try = instr.cast();
				System.out.println(objectMapper.writeValueAsString(_try));
				maxTries.add(retry.getMaxTries());
				retry.setMaxTries(7);
			} catch (ClassCastException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		});
		System.out.println(objectMapper.writeValueAsString(workflow));
		assertEquals("readRetryInWorkflowTest", Integer.valueOf(3), maxTries.get(0));
	}
	
	@Test
    public void readInventoryRequestWithWorkflowTest() throws Exception {
	    String json = "{\"jobschedulerId\": \"\", \"configuration\": {\"instructions\": [{\"id\": \"26\", \"uuid\": \"3f2d6e02-3a7e-4fd8-a50a-6ce417cecc48\", \"TYPE\": \"Execute.Named\", \"jobName\": \"job1\", \"label\": \"\", \"defaultArguments\": {}}]}, \"path\": \"/workflow2\", \"id\": 5, \"valid\": false, \"objectType\": \"WORKFLOW\"}";
	    JsonValidator.validateFailFast(json.getBytes(StandardCharsets.UTF_8), JSObject.class);
	    JSObject request = objectMapper.readValue(json, JSObject.class);
	    Workflow workflow = (Workflow) request.getConfiguration();
	    workflow.setPath(request.getPath());
	    byte[] workflowBytes = objectMapper.writeValueAsBytes(workflow);
	    JsonValidator.validateFailFast(workflowBytes, URI.create("classpath:/raml/jobscheduler/schemas/workflow/workflow-schema.json"));
	    System.out.println(new String(workflowBytes, StandardCharsets.UTF_8));
	    assertEquals("readInventoryRequestWithWorkflowTest", workflow.getTYPE(), DeployType.WORKFLOW);
	}

}
