package com.sos.jitl.jobs.rest;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.keystore.KeyStoreArguments;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArguments;

public class RestJobArguments extends JobArguments {
	
	public RestJobArguments() { 
		super(new CredentialStoreArguments(), new KeyStoreArguments());
	}
	
    private final JobArgument<String> myReturnVariable = new JobArgument<>("return_variable", false);
    private final JobArgument<String> myRequest = new JobArgument<>("request", false);
    private final JobArgument<String> LogItems = new JobArgument<>("log_items", false,"request:body");
    private final JobArgument<String> apiServerUsername = new JobArgument<>("username", false);
    private final JobArgument<String> apiServerPassword = new JobArgument<>("password", false, SOSArgument.DisplayMode.MASKED);
    private final JobArgument<String> apiServerPrivateKeyPath = new JobArgument<>("encipherment_private_key_path", false);
    private final JobArgument<String> editorUrl = new JobArgument<>("url", false);
//  private final JobArgument<String> apiToken = new JobArgument<>("apiToken", false);// for bearer token


    public JobArgument<String> getReturnVariable() {
        return myReturnVariable;
    }

    public JobArgument<String> getMyRequest() {
        return myRequest;
    }

    public JobArgument<String> getLogItems() {
        return LogItems;
    }

    public JobArgument<String> getApiUrl() {
        return editorUrl;
    }

    public JobArgument<String> getApiServerUsername() {
        return apiServerUsername;
    }

//    public JobArgument<String> getApiToken() {
//        return apiToken;
//    }

    public JobArgument<String> getApiServerPassword() {
        return apiServerPassword;
    }

    public JobArgument<String> getApiServerPrivateKeyPath() {
        return apiServerPrivateKeyPath;
    }
}
