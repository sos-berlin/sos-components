package com.sos.jitl.jobs.rest;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.keystore.KeyStoreArguments;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArguments;

public class JS7RestJobArguments extends JobArguments {

	public JS7RestJobArguments() {
		super(new CredentialStoreArguments(), new KeyStoreArguments());
	}

	private final JobArgument<String> myReturnVariable = new JobArgument<>("return_variable", false);
	private final JobArgument<String> myRequest = new JobArgument<>("request", true);
	private final JobArgument<String> LogItems = new JobArgument<>("log_items", false, "request:body");
	private final JobArgument<String> keystoreFile = new JobArgument<>("js7.web.https.keystore.file", false);
	private final JobArgument<String> keystoreAlias = new JobArgument<>("js7.web.https.keystore.alias", false);
	private final JobArgument<String> truststoreFile = new JobArgument<>("js7.web.https.truststore.file", false);
	private final JobArgument<String> keystoreKeyPassword = new JobArgument<>("js7.web.https.keystore.key-password",false, SOSArgument.DisplayMode.MASKED);
	private final JobArgument<String> keystoreStorePassword = new JobArgument<>("js7.web.https.keystore.store-password",false, SOSArgument.DisplayMode.MASKED);
	private final JobArgument<String> truststoreStorePassword = new JobArgument<>("js7.web.https.truststore.store-password", false, SOSArgument.DisplayMode.MASKED);
	private final JobArgument<String> apiServerCSFile = new JobArgument<>("js7.api-server.cs-file", false);
	private final JobArgument<String> apiServerCSKey = new JobArgument<>("js7.api-server.cs-key", false);
	private final JobArgument<String> apiServerCSPassword = new JobArgument<>("js7.api-server.cs-password", false,	SOSArgument.DisplayMode.MASKED);
	private final JobArgument<String> apiServerUsername = new JobArgument<>("js7.api-server.username", false);
	private final JobArgument<String> apiServerToken = new JobArgument<>("js7.api-server.token", false);
	private final JobArgument<String> apiServerPassword = new JobArgument<>("js7.api-server.password", false, SOSArgument.DisplayMode.MASKED);
	private final JobArgument<String> apiServerPrivateKeyPath = new JobArgument<>("js7.api-server.privatekey.path",false);
	private final JobArgument<String> editorUrl = new JobArgument<>("js7.api-server.url", false);

	//Client credential flow
	private JobArgument<String> clientId = new JobArgument<String>("clientId", false);
	private JobArgument<String> clientSecret = new JobArgument<String>("clientSecret", false, SOSArgument.DisplayMode.MASKED);
	private JobArgument<String> identityService = new JobArgument<String>("identityService", false);
	private JobArgument<String> issuer = new JobArgument<String>("issuer", false);
	private JobArgument<String> oidcTrustStorePath = new JobArgument<String>("oidcTrustStorePath", false);
	private JobArgument<String> oidcTrustStorePasswd = new JobArgument<String>("oidcTrustStorePasswd", false, SOSArgument.DisplayMode.MASKED);
	private JobArgument<String> oidcTrustStoreType = new JobArgument<String>("oidcTrustStoreType", false);
	private JobArgument<Boolean> useOidcLogin = new JobArgument<>("useOidcLogin", false, false);
	
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

	public JobArgument<String> getKeystoreFile() {
		return keystoreFile;
	}

	public JobArgument<String> getKeystoreKeyPassword() {
		return keystoreKeyPassword;
	}

	public JobArgument<String> getKeystoreStorePassword() {
		return keystoreStorePassword;
	}

	public JobArgument<String> getKeystoreAlias() {
		return keystoreAlias;
	}

	public JobArgument<String> getTruststoreFile() {
		return truststoreFile;
	}

	public JobArgument<String> getTruststoreStorePassword() {
		return truststoreStorePassword;
	}

	public JobArgument<String> getApiServerCSFile() {
		return apiServerCSFile;
	}

	public JobArgument<String> getApiServerCSKey() {
		return apiServerCSKey;
	}

	public JobArgument<String> getApiServerCSPassword() {
		return apiServerCSPassword;
	}

	public JobArgument<String> getApiServerUsername() {
		return apiServerUsername;
	}

	public JobArgument<String> getApiServerToken() {
		return apiServerToken;
	}

	public JobArgument<String> getApiServerPassword() {
		return apiServerPassword;
	}

	public JobArgument<String> getApiServerPrivateKeyPath() {
		return apiServerPrivateKeyPath;
	}
	
	public JobArgument<String> getClientId() {
		return clientId;
	}
	
	public JobArgument<String> getClientSecret() {
		return clientSecret;
	}
	
	public JobArgument<String> getIdentityService() {
		return identityService;
	}
	
	public JobArgument<String> getIssuer() {
		return issuer;
	}
	
	public JobArgument<String> getOidcTrustStorePath() {
		return oidcTrustStorePath;
	}
	
	public JobArgument<String> getOidcTrustStorePasswd() {
		return oidcTrustStorePasswd;
	}
	
	public JobArgument<String> getOidcTrustStoreType() {
		return oidcTrustStoreType;
	}
	
	public JobArgument<Boolean> getUseOidcLogin() {
		return useOidcLogin;
	}
	
}
