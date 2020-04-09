package com.sos.joc.deploy.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.commons.exception.SOSException;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.jobscheduler.model.agent.AgentRef;
import com.sos.jobscheduler.model.command.CommandType;
import com.sos.jobscheduler.model.command.UpdateRepo;
import com.sos.jobscheduler.model.deploy.Signature;
import com.sos.jobscheduler.model.deploy.SignatureType;
import com.sos.jobscheduler.model.deploy.SignedObject;
import com.sos.jobscheduler.model.instruction.ForkJoin;
import com.sos.jobscheduler.model.instruction.IfElse;
import com.sos.jobscheduler.model.instruction.Instruction;
import com.sos.jobscheduler.model.instruction.NamedJob;
import com.sos.jobscheduler.model.workflow.Branch;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.model.publish.JSObject;

public class MappingTest {

	private static final String IF_ELSE_JSON = "{\"TYPE\":\"Workflow\",\"path\":\"/test/IfElseWorkflow\",\"versionId\":\"2.0.0-SNAPSHOT\",\"instructions\":[{\"TYPE\":\"If\",\"then\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"job1\"},{\"TYPE\":\"Execute.Named\",\"jobName\":\"job2\"}],\"else\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"job3\"},{\"TYPE\":\"Execute.Named\",\"jobName\":\"job4\"}]}]}";
	private static final String FORK_JOIN_JSON = "{\"TYPE\":\"Workflow\",\"path\":\"/test/ForkJoinWorkflow\",\"versionId\":\"2.0.0-SNAPSHOT\",\"instructions\":[{\"TYPE\":\"Fork\",\"branches\":[{\"id\":\"BRANCH1\",\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"jobBranch1\"}]},{\"id\":\"BRANCH2\",\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"/test/jobBranch2\"}]},{\"id\":\"BRANCH3\",\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"jobBranch3\"}]}]},{\"TYPE\":\"Execute.Named\",\"jobName\":\"jobAfterJoin\"}]}";
	private static final String AGENT_REF_JSON = "{\"TYPE\":\"AgentRef\",\"path\":\"/test/Agent\",\"versionId\":\"2.0.0-SNAPSHOT\",\"uri\":\"http://localhost:4223\"}";

    @Test
    public void testWorkflowToJsonString() {
    	System.out.println("*************************  Test Workflow to JSON  *************************");
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
		} finally {
	    	System.out.println("************************ End Test Workflow to JSON  ***********************");
	    	System.out.println();
		}
    }
    
    @Test
    public void testWorkflowToJSObject() {
    	System.out.println("*************************  Test Workflow to JSObject  *************************");
		ObjectMapper om = new ObjectMapper();
		Workflow ifElseWorkflow = null;
		try {
			ifElseWorkflow = om.readValue(IF_ELSE_JSON, Workflow.class);
		} catch (JsonParseException | JsonMappingException e) {
			System.out.println("***** JsonParseException | JsonMappingException *****");
			Assert.fail(e.toString());
		} catch (IOException e) {
			System.out.println("***** IOException *****");
			Assert.fail(e.toString());
		}
    	JSObject jsObject = new JSObject();
    	jsObject.setContent(ifElseWorkflow);
    	Assert.assertEquals("/test/IfElseWorkflow", ((Workflow)jsObject.getContent()).getPath());
    	System.out.println("********************** End Test Workflow to JSObject  *************************");
    	System.out.println();
    }
    
    @Test
    public void testAgentRefToJSObject() {
		ObjectMapper om = new ObjectMapper();
		AgentRef agent = null;
		try {
			agent = om.readValue(AGENT_REF_JSON, AgentRef.class);
		} catch (JsonParseException | JsonMappingException e) {
			System.out.println("***** JsonParseException | JsonMappingException *****");
			Assert.fail(e.toString());
		} catch (IOException e) {
			System.out.println("***** IOException *****");
			Assert.fail(e.toString());
		}
    	JSObject jsObject = new JSObject();
    	jsObject.setContent(agent);
    	Assert.assertEquals("/test/Agent", ((AgentRef)jsObject.getContent()).getPath());
    }
    
	@Test
	public void testJsonStringToWorkflow() {
		ObjectMapper om = new ObjectMapper();
		try {
			Workflow ifElseWorkflow = om.readValue(IF_ELSE_JSON, Workflow.class);
			Workflow forkJoinWorkflow = om.readValue(FORK_JOIN_JSON, Workflow.class);
			
			IfElse ifElse = ifElseWorkflow.getInstructions().get(0).cast();
			NamedJob mj = ifElse.getThen().get(0).cast();
			Assert.assertEquals("testJsonStringToWorkflow: firstJobOfThen1", "job1", mj.getJobName());

			String firstJobOfThen = ifElseWorkflow.getInstructions().get(0).cast(IfElse.class)
					.getThen().get(0).cast(NamedJob.class).getJobName();
			Assert.assertEquals("testJsonStringToWorkflow: firstJobOfThen2", "job1", firstJobOfThen);
			
			Assert.assertNotNull(ifElseWorkflow);
			Assert.assertNotNull(forkJoinWorkflow);
		} catch (ClassCastException e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		} catch (JsonParseException e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		} catch (JsonMappingException e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}
    
    private Workflow createForkJoinWorkflow() {
    	Workflow workflow = new Workflow();
    	workflow.setVersionId("2.0.0-SNAPSHOT");
    	workflow.setPath("/test/ForkJoinWorkflow");
    	
    	ForkJoin forkJoinInstruction = createForkJoinInstruction();
    	
    	List<Branch> branches = new ArrayList<Branch>();
    	Branch branch1 = new Branch();
    	List<Instruction> branch1Instructions = new ArrayList<Instruction>();
    	branch1Instructions.add(createJobInstruction("/test/agent1", "jobBranch1", new Integer[]{0, 100}, new Integer[]{1}));
    	branch1.setInstructions(branch1Instructions);
    	branch1.setId("BRANCH1");
    	branches.add(branch1);
    	Branch branch2 = new Branch();
    	List<Instruction> branch2Instructions = new ArrayList<Instruction>();
    	branch2Instructions.add(createJobInstruction("/test/agent1", "jobBranch2", new Integer[]{0, 101}, new Integer[]{1, 2}));
    	branch2.setInstructions(branch2Instructions);
    	branch2.setId("BRANCH2");
    	branches.add(branch2);
    	Branch branch3 = new Branch();
    	List<Instruction> branch3Instructions = new ArrayList<Instruction>();
    	branch3Instructions.add(createJobInstruction("/test/agent1", "jobBranch3", new Integer[]{0, 102}, new Integer[]{1, 2, 3}));
    	branch3.setInstructions(branch3Instructions);
    	branch3.setId("BRANCH3");
    	branches.add(branch3);
    	forkJoinInstruction.setBranches(branches);
    	
    	NamedJob afterForkJoin = createJobInstruction("/test/agent1", "jobAfterJoin", new Integer[]{0}, new Integer[]{1, 99});

    	List<Instruction> workflowInstructions = new ArrayList<Instruction>();
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
    	List<Instruction> thenInstructions = new ArrayList<Instruction>();
    	List<Instruction> elseInstructions = new ArrayList<Instruction>();

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
    	
    	List<Instruction> workflowInstructions = new ArrayList<Instruction>();
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
    
//  @Test
  public void testUpdateRepo () {
  	SignedObject signedObject = new SignedObject();
  	signedObject.setString("{\"TYPE\": \"AgentRef\", \"path\": \"/test-agent-2\", \"versionId\": \"testVersion1\", \"uri\":\"http://localhost:41420\"}");
  	Signature signature = new Signature();
  	signature.setSignatureString("-----BEGIN PGP SIGNATURE-----\r\n" + 
  			"\r\n" + 
  			"iQEzBAEBCAAdFiEEzMIAKBvtuj6rEL9KUGGKu7ZjpJ0FAl5eWN8ACgkQUGGKu7Zj\r\n" + 
  			"pJ2f4Af9HLFEGbDQeq4SfNwznY1AA0xSalHCSqsZA7K5t85uaG3qkkIDxmr+UDPI\r\n" + 
  			"4Oa7Bx3gMvrBY/lJrwARwGLkXIdGmIyuXt2VvRU+yTfZK4oI5BSgqkKkkRVEwLL+\r\n" + 
  			"OrEPDquVRs/smh1df5mzUdx7kRaViLVD4ZqYp+87Qoqd7JbKdf3X7MV4A23efiY/\r\n" + 
  			"Ko+ZW2nnVuFmplt4E0ZZw7XHa7HHWw2iYyKCcNLNWuwI0AL1ecG5kmQMUXrg1NiZ\r\n" + 
  			"QRYs8SDPl+qTv3Jop+45Vh9OKuh6qkuanEjQcwqnRzoaXIahZSJg1MlmNrRRR1pa\r\n" + 
  			"BJ3nqv0B4WUvaGgZA3GPkYaStS6PpA==\r\n" + 
  			"=KGoC\r\n" + 
  			"-----END PGP SIGNATURE-----");
  	signature.setTYPE(SignatureType.PGP);
  	signedObject.setSignature(signature);
  	UpdateRepo updateRepo = new UpdateRepo();
  	updateRepo.setVersionId("testVersion1");
  	updateRepo.getChange().add(signedObject);
  	updateRepo.setTYPE(CommandType.UPDATE_REPO);
		ObjectMapper om = new ObjectMapper();
		om.enable(SerializationFeature.INDENT_OUTPUT);
		try {
	    	System.out.println("***************************  Request Body  ***************************");
			System.out.println(om.writeValueAsString(updateRepo));
	        SOSRestApiClient httpClient = new SOSRestApiClient();
	        httpClient.setAllowAllHostnameVerifier(false);
	        httpClient.setBasicAuthorization("VGVzdDp0ZXN0");
	        httpClient.addHeader("Accept", "application/json");
	        httpClient.addHeader("Content-Type", "application/json");
//	        httpClient.addHeader("userId", "test");
	        String response =httpClient.postRestService(UriBuilder.fromPath("http://localhost:4222/master/api/command").build(), om.writeValueAsString(updateRepo));
	    	System.out.println("*****************************  Response  *****************************");
	        System.out.println(response);
		} catch (IllegalArgumentException | UriBuilderException | SOSException | IOException e) {
			System.out.println(e.toString());
		} finally {
	    	System.out.println("************************** End Update Repo  **************************\n");
		}
  }
}
