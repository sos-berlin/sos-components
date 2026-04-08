package com.sos.jitl.jobs.rest;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.util.keystore.KeyStoreArguments;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArguments;

public class HealthCheckJobArguments extends JobArguments{
	
	public HealthCheckJobArguments() {
		super(new CredentialStoreArguments(), new KeyStoreArguments());
	}
	
	private JobArgument<String> filePath = new JobArgument<>("filePath", false);
	
	public JobArgument<String> getFilePath() {
		return filePath;
	}
	
}
