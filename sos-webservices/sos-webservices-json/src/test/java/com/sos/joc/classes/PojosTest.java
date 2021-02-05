package com.sos.joc.classes;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
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
import com.sos.controller.model.command.CancelOrder;
import com.sos.controller.model.command.JSBatchCommands;
import com.sos.controller.model.common.Variables;
import com.sos.controller.model.order.FreshOrder;
import com.sos.controller.model.order.OrderMode;
import com.sos.controller.model.order.OrderModeType;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.instruction.IfElse;
import com.sos.inventory.model.instruction.InstructionType;
import com.sos.inventory.model.instruction.Instructions;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.inventory.model.instruction.RetryCatch;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.workflow.Jobs;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.model.inventory.ConfigurationObject;
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

		CancelOrder cancelnotStartedOrder = new CancelOrder(Arrays.asList("TEST-NOT_STARTED-ORDER"), new OrderMode(OrderModeType.FRESH_ONLY, null));
		CancelOrder cancelFreshOrStartedOrder = new CancelOrder(Arrays.asList("TEST-FRESH_OR_STARTED-ORDER"), new OrderMode(OrderModeType.FRESH_OR_STARTED, null));
		batch.setCommands(Arrays.asList(cancelnotStartedOrder, cancelFreshOrStartedOrder));
//		System.out.println(objectMapper.writeValueAsString(batch));
		String expected = "{\"TYPE\":\"Batch\",\"commands\":[{\"TYPE\":\"CancelOrders\",\"orderIds\":[\"TEST-NOT_STARTED-ORDER\"],\"mode\":{\"TYPE\":\"FreshOnly\"}},{\"TYPE\":\"CancelOrders\",\"orderIds\":[\"TEST-FRESH_OR_STARTED-ORDER\"],\"mode\":{\"TYPE\":\"FreshOrStarted\"}}]}";
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
	    JsonValidator.validate(json.getBytes(StandardCharsets.UTF_8), ConfigurationObject.class);
	    ConfigurationObject request = objectMapper.readValue(json, ConfigurationObject.class);
	    Workflow workflow = (Workflow) request.getConfiguration();
	    workflow.setPath(request.getPath());
	    Job job = new Job();
	    job.setAgentId("myAgent");
	    job.setExecutable(new ExecutableScript("echo hallo", null));
	    Jobs jobs = new Jobs();
	    jobs.setAdditionalProperty("job1", job);
	    workflow.setJobs(jobs);
	    byte[] workflowBytes = objectMapper.writeValueAsBytes(workflow);
	    JsonValidator.validate(workflowBytes, URI.create("classpath:/raml/inventory/schemas/workflow/workflow-schema.json"));
	    System.out.println(new String(workflowBytes, StandardCharsets.UTF_8));
	    assertEquals("readInventoryRequestWithWorkflowTest", workflow.getTYPE(), DeployType.WORKFLOW);
	}
	
    @Test
    public void readAndWriteVariables() throws IOException {
        Variables vars = new Variables(0);
        vars.setAdditionalProperty("myString", "MyStringValue");
        vars.setAdditionalProperty("myNumber", 4711);
        vars.setAdditionalProperty("myNumber2", 1.34E2);
        vars.setAdditionalProperty("myBigDecimal", new BigDecimal(4711));
        vars.setAdditionalProperty("myBoolean", true);
        System.out.println(vars.getAdditionalProperties().get("myString").getClass());
        System.out.println(vars.getAdditionalProperties().get("myNumber").getClass());
        System.out.println(vars.getAdditionalProperties().get("myNumber2").getClass());
        System.out.println(vars.getAdditionalProperties().get("myBigDecimal").getClass());
        System.out.println(vars.getAdditionalProperties().get("myBoolean").getClass());
        System.out.println(objectMapper.writeValueAsString(vars));
        String json = "{\"returnCode\":0,\"myString\":\"MyStringValue\",\"myBoolean\":true,\"myNumber\":4711,\"myNumber2\":1.34E2}";
        vars = objectMapper.readValue(json, Variables.class);
        System.out.println(vars.getAdditionalProperties().get("myString").getClass());
        System.out.println(vars.getAdditionalProperties().get("myNumber").getClass());
        System.out.println(vars.getAdditionalProperties().get("myNumber2").getClass());
        System.out.println(vars.getAdditionalProperties().get("myBoolean").getClass());
        System.out.println(vars.getAdditionalProperties().get("myString").toString());
        System.out.println(vars.getAdditionalProperties().get("myNumber").toString());
        System.out.println(vars.getAdditionalProperties().get("myNumber2").toString());
        System.out.println(vars.getAdditionalProperties().get("myBoolean").toString());

    }

}
