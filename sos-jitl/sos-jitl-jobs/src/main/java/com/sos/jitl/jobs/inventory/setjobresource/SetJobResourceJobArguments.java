package com.sos.jitl.jobs.inventory.setjobresource;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;

public class SetJobResourceJobArguments extends JobArguments {

	private JobArgument<String> controllerId = new JobArgument<String>("controller_id", false);
	private JobArgument<String> jobResource = new JobArgument<String>("job_resource", true);
	private JobArgument<String> key = new JobArgument<String>("key", true);
	private JobArgument<String> value = new JobArgument<String>("value", true);
	private JobArgument<String> timeZone = new JobArgument<String>("tine_zone", false);
	private JobArgument<String> environmentVariable = new JobArgument<String>("environment_variable", false);

	public SetJobResourceJobArguments() {
		super(new SOSCredentialStoreArguments());
	}

	public String getControllerId() {
		return controllerId.getValue();
	}

	public void setControllerId(String controller) {
		this.controllerId.setValue(controller);
	}

	public String getJobResource() {
		return jobResource.getValue();
	}

	public void setJobResource(String jobResource) {
		this.jobResource.setValue(jobResource);
	}

	public String getKey() {
		return key.getValue();
	}

	public void setKey(String key) {
		this.key.setValue(key);
	}

	public String getValue() {
		return value.getValue();
	}

	public void setValue(String value) {
		this.value.setValue(value);
	}

	public String getTimeZone() {
		return timeZone.getValue();
	}

	public void setTimeZone(String timeZone) {
		this.timeZone.setValue(timeZone);
	}

	public String getEnvironmentVariable() {
		return environmentVariable.getValue();
	}

	public void setEnvironmentVariable(String environmentVariable) {
		this.environmentVariable.setValue(environmentVariable);
	}

}
