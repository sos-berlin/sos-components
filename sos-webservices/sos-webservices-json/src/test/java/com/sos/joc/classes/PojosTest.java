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
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.controller.model.command.CancelOrder;
import com.sos.controller.model.command.JSBatchCommands;
import com.sos.controller.model.order.FreshOrder;
import com.sos.controller.model.order.OrderMode;
import com.sos.controller.model.order.OrderModeType;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.instruction.Cycle;
import com.sos.inventory.model.instruction.IfElse;
import com.sos.inventory.model.instruction.InstructionType;
import com.sos.inventory.model.instruction.Instructions;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.inventory.model.instruction.RetryCatch;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.instruction.schedule.Repeat;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.workflow.Jobs;
import com.sos.inventory.model.workflow.ListParameterType;
import com.sos.inventory.model.workflow.ParameterType;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.schema.JsonValidator;
import com.sos.sign.model.workflow.ListParameters;
import com.sos.sign.model.workflow.OrderPreparation;
import com.sos.sign.model.workflow.Parameter;
import com.sos.sign.model.workflow.ParameterListType;
import com.sos.sign.model.workflow.Parameters;

public class PojosTest {
	
	private ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(
            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);
	
	private static final String invOrderPreparation = "{\"parameters\":{\"myOptionalNumberVar\":{\"type\":\"Number\",\"default\":\"0\"},\"myFinalVar\":{\"final\":\"'hello'\"},\"myRequiredStringVar\":{\"type\":\"String\"},\"myListVar\":{\"type\":\"List\",\"listParameters\":{\"myListVar2\":{\"type\":\"String\"},\"myListVar1\":{\"type\":\"Number\"}}}},\"allowUndeclared\":false}";
	private static final String signOrderPreparation = "{\"parameters\":{\"myOptionalNumberVar\":{\"type\":\"Number\",\"default\":\"0\"},\"myFinalVar\":{\"final\":\"'hello'\"},\"myRequiredStringVar\":{\"type\":\"String\"},\"myListVar\":{\"type\":{\"TYPE\":\"List\",\"elementType\":{\"TYPE\":\"Object\",\"myListVar2\":\"String\",\"myListVar1\":\"Number\"}}}},\"allowUndeclared\":false}";
    

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
	    Job job = new Job();
	    job.setAgentName("myAgent");
	    job.setExecutable(new ExecutableScript("echo hallo", null, true, null, null));
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
        Variables vars = new Variables();
        vars.setAdditionalProperty("myString", "MyStringValue");
        vars.setAdditionalProperty("myNumber", 4711);
        vars.setAdditionalProperty("myNumber2", 1.34E2);
        vars.setAdditionalProperty("myBigDecimal", new BigDecimal(4711));
        vars.setAdditionalProperty("myBoolean", true);
        vars.setAdditionalProperty("myArray", new ArrayList<String>(Arrays.asList("hallo")));
        vars.setAdditionalProperty("myArray2", Arrays.asList("hallo"));
        vars.setAdditionalProperty("myArray3", Collections.singletonList("hallo"));
        System.out.println(vars.getAdditionalProperties().get("myString").getClass());
        System.out.println(vars.getAdditionalProperties().get("myNumber").getClass());
        System.out.println(vars.getAdditionalProperties().get("myNumber2").getClass());
        System.out.println(vars.getAdditionalProperties().get("myBigDecimal").getClass());
        System.out.println(vars.getAdditionalProperties().get("myBoolean").getClass());
        System.out.println(vars.getAdditionalProperties().get("myArray").getClass());
        if (vars.getAdditionalProperties().get("myArray") instanceof List) {
            System.out.println("It's a List!");
        }
        if (vars.getAdditionalProperties().get("myArray2") instanceof List) {
            System.out.println("It's a List!");
        }
        if (vars.getAdditionalProperties().get("myArray3") instanceof List) {
            System.out.println("It's a List!");
        }
        System.out.println(objectMapper.writeValueAsString(vars));
        String json = "{\"returnCode\":0,\"myString\":\"MyStringValue\",\"myBoolean\":true,\"myNumber\":4711,\"myNumber2\":1.34E2,\"myList\": [{\"countryCode\": 4711,\"countryName\": \"Germany\"},{\"countryCode\": \"UK\",\"countryName\": \"United Kingdom\"}]}";
        vars = objectMapper.readValue(json, Variables.class);
        System.out.println(vars.getAdditionalProperties().get("myString").getClass());
        System.out.println(vars.getAdditionalProperties().get("myNumber").getClass());
        System.out.println(vars.getAdditionalProperties().get("myNumber2").getClass());
        System.out.println(vars.getAdditionalProperties().get("myBoolean").getClass());
        System.out.println(vars.getAdditionalProperties().get("myList").getClass());
        System.out.println(vars.getAdditionalProperties().get("myString").toString());
        System.out.println(vars.getAdditionalProperties().get("myNumber").toString());
        System.out.println(vars.getAdditionalProperties().get("myNumber2").toString());
        System.out.println(vars.getAdditionalProperties().get("myBoolean").toString());
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> x = (List<Map<String, Object>>) vars.getAdditionalProperties().get("myList");
        
        x.forEach(m -> {
            m.forEach((k, v) -> {
                System.out.println(k + ": " + v.getClass() + " -> " + v.toString());
            });
        });
        
        
 
        //System.out.println(vars.getAdditionalProperties().get("myList").toString());

    }
    
    @Test
    public void retainOrderTest() throws JsonParseException, JsonMappingException, IOException {
        String json = "{\"configuration\":{\"env\":{\"value\":\"'b'\",\"name\":\"'a'\"}},\"valid\":false,\"id\":5,\"objectType\":\"JOBRESOURCE\"}";
        ConfigurationObject conf = objectMapper.readValue(json, ConfigurationObject.class);
        System.out.println(objectMapper.writeValueAsString(conf.getConfiguration()));
    }
    
    @Test
    public void invOrderPrepToSignOrderPrep() throws JsonParseException, JsonMappingException, IOException {
        Requirements conf = objectMapper.readValue(invOrderPreparation, Requirements.class);
        Parameters params = new Parameters();
        if (conf.getParameters() != null && conf.getParameters().getAdditionalProperties() != null) {
            conf.getParameters().getAdditionalProperties().forEach((k, v) -> {
                Parameter p = new Parameter();
                p.setDefault(v.getDefault());
                p.setFinal(v.getFinal());
                if (ParameterType.List.equals(v.getType())) {
                    ListParameters lps = new ListParameters();
                    v.getListParameters().getAdditionalProperties().forEach((k1, v1) -> {
                        lps.setAdditionalProperty(k1, v1.getType());
                    });
                    p.setType(new ParameterListType("List", lps));
                } else {
                    p.setType(v.getType()); // wrong type enum
                }
                params.setAdditionalProperty(k, p);
            });
        }
        OrderPreparation op = new OrderPreparation(params, conf.getAllowUndeclared());
        String result = objectMapper.writeValueAsString(op);
        System.out.println(result);
        assertEquals("signOrderPrepToInvOrderPrep", result, signOrderPreparation);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void signOrderPrepToInvOrderPrep() throws JsonParseException, JsonMappingException, IOException {
        OrderPreparation conf = objectMapper.readValue(signOrderPreparation, OrderPreparation.class);
        com.sos.inventory.model.workflow.Parameters params = new com.sos.inventory.model.workflow.Parameters();
        if (conf.getParameters() != null && conf.getParameters().getAdditionalProperties() != null) {
            conf.getParameters().getAdditionalProperties().forEach((k, v) -> {
                com.sos.inventory.model.workflow.Parameter p = new com.sos.inventory.model.workflow.Parameter();
                p.setDefault(v.getDefault());
                p.setFinal(v.getFinal());
                if (v.getType() != null) {
                    //System.out.println(v.getType().getClass().getName());
                    if (v.getType() instanceof String) {
                        try {
                            p.setType(ParameterType.fromValue((String) v.getType()));
                        } catch (Exception e) {
                        }
                    } else if (v.getType() instanceof ParameterListType) {
                        p.setType(ParameterType.List);
                        ParameterListType plt = (ParameterListType) v.getType();
                        if (plt.getElementType() != null && plt.getElementType().getAdditionalProperties() != null) {
                            com.sos.inventory.model.workflow.ListParameters lp = new com.sos.inventory.model.workflow.ListParameters();
                            plt.getElementType().getAdditionalProperties().forEach((k1, v1) -> {
                                lp.setAdditionalProperty(k1, new com.sos.inventory.model.workflow.ListParameter(v1));
                                p.setListParameters(lp);
                            });
                        }
                    } else if (v.getType() instanceof Map) {
                        p.setType(ParameterType.List);
                        Map<String, String> slp = (Map<String, String>) ((Map<String, Object>) v.getType()).get("elementType");
                        if (slp != null) {
                            com.sos.inventory.model.workflow.ListParameters lp = new com.sos.inventory.model.workflow.ListParameters();
                            slp.forEach((k1, v1) -> {
                                if (!"TYPE".equals(k1)) {
                                    try {
                                        lp.setAdditionalProperty(k1, new com.sos.inventory.model.workflow.ListParameter(ListParameterType.fromValue(v1)));
                                        p.setListParameters(lp);
                                    } catch (Exception e) {
                                    }
                                }
                            });
                        }
                    }
                }
                params.setAdditionalProperty(k, p);
            });
        }
        Requirements r = new Requirements(params, conf.getAllowUndeclared()); 
        String result = objectMapper.writeValueAsString(r);
        System.out.println(invOrderPreparation);
        System.out.println(result);
        assertEquals("signOrderPrepToInvOrderPrep", result, invOrderPreparation);
    }
    
    @Test
    public void cycleInstruction() throws JsonParseException, JsonMappingException, IOException {
        String json = "{\"TYPE\":\"Cycle\",\"cycleWorkflow\":{\"instructions\":[]},\"schedule\":{\"schemes\":[{\"repeat\":{\"TYPE\":\"Periodic\",\"period\":3600,\"offsets\":[600,900,1200]}},{\"repeat\":{\"TYPE\":\"Ticking\",\"interval\":1200},\"admissionTimeScheme\":{\"periods\":[{\"TYPE\":\"DailyPeriod\",\"secondOfDay\":200,\"duration\":3600},{\"TYPE\":\"WeekdayPeriod\",\"secondOfWeek\":360000,\"duration\":3600}]}},{\"repeat\":{\"TYPE\":\"Continuous\",\"pause\":300},\"admissionTimeScheme\":{\"periods\":[{\"TYPE\":\"WeekdayPeriod\",\"secondOfWeek\":583200,\"duration\":1800}]}},{\"repeat\":{\"TYPE\":\"Continuous\",\"pause\":60,\"limit\":3},\"admissionTimeScheme\":{\"periods\":[{\"TYPE\":\"WeekdayPeriod\",\"secondOfWeek\":590400,\"duration\":1800}]}}]}}";
        String json2 = "{\"TYPE\": \"Periodic\",\"offsets\": [600,900,1200],\"period\": 3600}";
        Cycle cycle = objectMapper.readValue(json, Cycle.class);
        Repeat periodic = objectMapper.readValue(json2, Repeat.class);
        String result = objectMapper.writeValueAsString(cycle);
        System.out.println(result);
        System.out.println(objectMapper.writeValueAsString(periodic));
        assertEquals("cycleInstruction", result, json);
    }

}
