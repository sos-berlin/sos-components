package com.sos.webservices.configuration;

import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Test;

import com.sos.jobscheduler.base.problem.Problem;
import com.sos.jobscheduler.master.javaapi.MasterJsonValidator;

public class ValidationTest {

	private static final MasterJsonValidator VALIDATOR = new MasterJsonValidator();
	
	@Test
	public void JobTest() {
		String jobInstruction = "{ \"TYPE\": \"Job\", \"jobPath\": \"/JOB\", \"agentPath\": \"/AGENT\" }";
		Optional<Problem> result = VALIDATOR.checkInstructionJson(jobInstruction);
		assertEquals(result, Optional.empty());
	}
	
	@Test
	public void JobWithWrongDataTypeInJobPathTest() {
		String jobInstruction = "{ \"TYPE\": \"Job\", \"jobPath\": [\"/JOB\"], \"agentPath\": \"/AGENT\" }";
		Optional<Problem> result = VALIDATOR.checkInstructionJson(jobInstruction);
		assertEquals(result.get().toString(), "String: DownField(jobPath)");
	}
	
	@Test
	public void JobWithMissingRequiredJobPathTest() {
		String jobInstruction = "{ \"TYPE\": \"Job\", \"path\": \"/JOB\", \"agentPath\": \"/AGENT\" }";
		Optional<Problem> result = VALIDATOR.checkInstructionJson(jobInstruction);
		assertEquals(result.get().toString(), "Attempt to decode value on failed cursor: DownField(jobPath)");
	}
	
	@Test
	public void JobWithUnknownFieldTest() {
		String jobInstruction = "{ \"TYPE\": \"Job\", \"jobPath\": \"/JOB\", \"agentPath\": \"/AGENT\", \"unknown\": [] }";
		Optional<Problem> result = VALIDATOR.checkInstructionJson(jobInstruction);
		assertEquals(result, Optional.empty());
	}
	
	@Test
	public void JobWithWrongDataTypeInOptionalFieldTest() {
		String jobInstruction = "{ \"TYPE\": \"Job\", \"jobPath\": \"/JOB\", \"agentPath\": \"/AGENT\", \"returnCodeMeaning\": [] }";
		Optional<Problem> result = VALIDATOR.checkInstructionJson(jobInstruction);
		assertEquals(result.get().toString(), "Attempt to decode value on failed cursor: DownField(failure),DownField(returnCodeMeaning)");
	}
	
	@Test
	public void JobWithSuccessAndFailureConcurrentlyTest() {
		String jobInstruction = "{ \"TYPE\": \"Job\", \"jobPath\": \"/JOB\", \"agentPath\": \"/AGENT\", \"returnCodeMeaning\": {\"success\": [ 0, 1 ], \"failure\": [ 0, 1 ]} }";
		Optional<Problem> result = VALIDATOR.checkInstructionJson(jobInstruction);
		assertNotEquals(result, Optional.empty());
	}
}
