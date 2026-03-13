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
	private final JobArgument<String> myRequest = new JobArgument<>("request", true);
	private final JobArgument<String> LogItems = new JobArgument<>("log_items", false, "request:body");
	private final JobArgument<String> apiServerUsername = new JobArgument<>("username", false);
	private final JobArgument<String> apiServerPassword = new JobArgument<>("password", false,
			SOSArgument.DisplayMode.MASKED);
	private final JobArgument<String> apiServerPrivateKeyPath = new JobArgument<>("encipherment_private_key_path",
			false);
	private final JobArgument<String> editorUrl = new JobArgument<>("url", false);
	
	//Client credential flow
//	private JobArgument<String> clientId = new JobArgument<String>("clientId", false);
//	private JobArgument<String> clientSecret = new JobArgument<String>("clientSecret", false);
//	private JobArgument<String> identityService = new JobArgument<String>("identityService", false);
//	private JobArgument<String> issuer = new JobArgument<String>("issuer", false);
//	private JobArgument<String> oidcTrustStorePath = new JobArgument<String>("oidcTrustStorePath", false);
//	private JobArgument<String> oidcTrustStorePasswd = new JobArgument<String>("oidcTrustStorePasswd", false);
//	private JobArgument<String> oidcTrustStoreType = new JobArgument<String>("oidcTrustStoreType", false);
//	private JobArgument<Boolean> useOidcLogin = new JobArgument<>("useOidcLogin", false);
	
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
