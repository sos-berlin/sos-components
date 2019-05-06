package com.sos.webservices.deployment.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.jobscheduler.model.instruction.ForkJoin;
import com.sos.jobscheduler.model.instruction.IInstructible;
import com.sos.jobscheduler.model.instruction.IfElse;
import com.sos.jobscheduler.model.instruction.NamedJob;
import com.sos.jobscheduler.model.workflow.Branch;
import com.sos.jobscheduler.model.workflow.Workflow;

public class TestSimpleDeployment {
	
	private static final String IF_ELSE_JSON = "{\"id\":{\"path\":\"/test/IfElseWorkflow\",\"versionId\":\"2.0.0-SNAPSHOT\"},\"instructions\":[{\"TYPE\":\"If\",\"then\":[{\"TYPE\":\"Job\",\"jobPath\":\"/test/job1\",\"agentPath\":\"/test/agent1\",\"returnCodeMeaning\":{\"success\":[0,100],\"failure\":[1,2]}},{\"TYPE\":\"Job\",\"jobPath\":\"/test/job2\",\"agentPath\":\"/test/agent1\",\"returnCodeMeaning\":{\"success\":[0,101,102],\"failure\":[1,3,4]}}],\"else\":[{\"TYPE\":\"Job\",\"jobPath\":\"/test/job3\",\"agentPath\":\"/test/agent2\",\"returnCodeMeaning\":{\"success\":[0,103],\"failure\":[1,5,6]}},{\"TYPE\":\"Job\",\"jobPath\":\"/test/job4\",\"agentPath\":\"/test/agent2\",\"returnCodeMeaning\":{\"success\":[0,104,105],\"failure\":[-1,1,99]}}]}]}";
	private static final String FORK_JOIN_JSON = "{\"id\":{\"path\":\"/test/ForkJoinWorkflow\",\"versionId\":\"2.0.0-SNAPSHOT\"},\"instructions\":[{\"TYPE\":\"ForkJoin\",\"branches\":[{\"id\":\"BRANCH1\",\"instructions\":[{\"TYPE\":\"Job\",\"jobPath\":\"/test/jobBranch1\",\"agentPath\":\"/test/agent1\",\"returnCodeMeaning\":{\"success\":[0,100],\"failure\":[1]}}]},{\"id\":\"BRANCH2\",\"instructions\":[{\"TYPE\":\"Job\",\"jobPath\":\"/test/jobBranch2\",\"agentPath\":\"/test/agent1\",\"returnCodeMeaning\":{\"success\":[0,101],\"failure\":[1,2]}}]},{\"id\":\"BRANCH3\",\"instructions\":[{\"TYPE\":\"Job\",\"jobPath\":\"/test/jobBranch3\",\"agentPath\":\"/test/agent1\",\"returnCodeMeaning\":{\"success\":[0,102],\"failure\":[1,2,3]}}]}]},{\"TYPE\":\"Job\",\"jobPath\":\"/test/jobAfterJoin\",\"agentPath\":\"/test/agent1\",\"returnCodeMeaning\":{\"success\":[0],\"failure\":[1,99]}}]}";

    @Test
    public void testWorkflowToJsonString() {
    	Workflow ifElseWorkflow = createIfElseWorkflow();
    	Workflow forkJoinWorkflow = createForkJoinWorkflow();
		ObjectMapper om = new ObjectMapper();
		String workflowJson = null;
		try {
			om.enable(SerializationFeature.INDENT_OUTPUT);
	    	System.out.println("******************************  IfElse  ******************************");
			workflowJson = om.writeValueAsString(ifElseWorkflow);
	    	System.out.println(workflowJson);
	    	System.out.println("*****************************  ForkJoin  *****************************");
	    	workflowJson = null;
			workflowJson = om.writeValueAsString(forkJoinWorkflow);
	    	System.out.println(workflowJson);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @Test
    public void testJsonStringToWorkflow() {
		ObjectMapper om = new ObjectMapper();
    	try {
			Workflow ifElseWorkflow = om.readValue(IF_ELSE_JSON, Workflow.class);
			Workflow forkJoinWorkflow = om.readValue(FORK_JOIN_JSON, Workflow.class);
			Assert.assertNotNull(ifElseWorkflow);
			Assert.assertNotNull(forkJoinWorkflow);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private Workflow createForkJoinWorkflow() {
    	Workflow workflow = new Workflow();
//    	WorkflowId wfId = new WorkflowId();
    	workflow.setVersionId("2.0.0-SNAPSHOT");
    	workflow.setPath("/test/ForkJoinWorkflow");
//    	workflow.setId(wfId);
    	
    	ForkJoin forkJoinInstruction = createForkJoinInstruction();
    	
    	List<Branch> branches = new ArrayList<Branch>();
    	Branch branch1 = new Branch();
    	List<IInstructible> branch1Instructions = new ArrayList<IInstructible>();
    	branch1Instructions.add(createJobInstruction("/test/agent1", "jobBranch1", new Integer[]{0, 100}, new Integer[]{1}));
    	branch1.setInstructions(branch1Instructions);
    	branch1.setId("BRANCH1");
    	branches.add(branch1);
    	Branch branch2 = new Branch();
    	List<IInstructible> branch2Instructions = new ArrayList<IInstructible>();
    	branch2Instructions.add(createJobInstruction("/test/agent1", "jobBranch2", new Integer[]{0, 101}, new Integer[]{1, 2}));
    	branch2.setInstructions(branch2Instructions);
    	branch2.setId("BRANCH2");
    	branches.add(branch2);
    	Branch branch3 = new Branch();
    	List<IInstructible> branch3Instructions = new ArrayList<IInstructible>();
    	branch3Instructions.add(createJobInstruction("/test/agent1", "jobBranch3", new Integer[]{0, 102}, new Integer[]{1, 2, 3}));
    	branch3.setInstructions(branch3Instructions);
    	branch3.setId("BRANCH3");
    	branches.add(branch3);
    	forkJoinInstruction.setBranches(branches);
    	
    	NamedJob afterForkJoin = createJobInstruction("/test/agent1", "jobAfterJoin", new Integer[]{0}, new Integer[]{1, 99});

    	List<IInstructible> workflowInstructions = new ArrayList<IInstructible>();
    	workflowInstructions.add(forkJoinInstruction);
    	workflowInstructions.add(afterForkJoin);
    	workflow.setInstructions(workflowInstructions);

    	return workflow;
    }
    
    private Workflow createIfElseWorkflow() {
    	Workflow workflow = new Workflow();
//    	WorkflowId wfId = new WorkflowId();
    	workflow.setVersionId("2.0.0-SNAPSHOT");
    	workflow.setPath("/test/IfElseWorkflow");
//    	workflow.setId(wfId);
    	List<IInstructible> thenInstructions = new ArrayList<IInstructible>();
    	List<IInstructible> elseInstructions = new ArrayList<IInstructible>();

    	NamedJob job1 = createJobInstruction("/test/agent1", "job1", new Integer[]{0, 100}, new Integer[]{1, 2});
    	NamedJob job2 = createJobInstruction("/test/agent1", "job2", new Integer[]{0, 101, 102}, new Integer[]{1, 3, 4});
    	NamedJob job3 = createJobInstruction("/test/agent2", "job3", new Integer[]{0, 103}, new Integer[]{1, 5, 6});
    	NamedJob job4 = createJobInstruction("/test/agent2", "job4", new Integer[]{0, 104, 105}, new Integer[]{-1, 1, 99});
    	
    	IfElse ifInstruction = createIfInstruction("variable('OrderValueParam1', 'true').toBoolean");
    	
    	thenInstructions.add(job1);
    	thenInstructions.add(job2);
    	ifInstruction.setThen(thenInstructions);
    	
    	elseInstructions.add(job3);
    	elseInstructions.add(job4);
    	ifInstruction.setElse(elseInstructions);
    	
    	List<IInstructible> workflowInstructions = new ArrayList<IInstructible>();
    	workflowInstructions.add(ifInstruction);
    	workflow.setInstructions(workflowInstructions);
    	return workflow;
    }
    
    private IfElse createIfInstruction (String condition) {
    	IfElse ifInstruction = new IfElse();
    	ifInstruction.setPredicate(condition);
    	return ifInstruction;
    }
    
    private ForkJoin createForkJoinInstruction () {
    	ForkJoin forkJoinInstruction = new ForkJoin();
    	return forkJoinInstruction;
    }
    
    private NamedJob createJobInstruction (String agentPath, String jobName, Integer[] successes, Integer[] errors) {
    	NamedJob job = new NamedJob();
//    	JobReturnCode jrc = new JobReturnCode();
//    	jrc.setSuccess(new ArrayList<Integer>(Arrays.asList(successes)));
//    	jrc.setFailure(new ArrayList<Integer>(Arrays.asList(errors)));
    	job.setJobName(jobName);
    	return job;
    }
}
