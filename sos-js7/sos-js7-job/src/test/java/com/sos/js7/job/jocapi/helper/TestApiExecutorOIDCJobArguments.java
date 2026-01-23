package com.sos.js7.job.jocapi.helper;

import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArguments;

public class TestApiExecutorOIDCJobArguments extends JobArguments {

    private JobArgument<String> apiURL = new JobArgument<String>("api_url", true);
    private JobArgument<String> body = new JobArgument<String>("body", true);
    private JobArgument<String> clientId = new JobArgument<String>("clientId", false);
    private JobArgument<String> clientSecret = new JobArgument<String>("clientSecret", false);
    private JobArgument<String> identityService = new JobArgument<String>("identityService", false);
    private JobArgument<String> issuer = new JobArgument<String>("issuer", false);
    private JobArgument<String> oidcTrustStorePath = new JobArgument<String>("oidcTrustStorePath", false);
    private JobArgument<String> oidcTrustStorePasswd = new JobArgument<String>("oidcTrustStorePasswd", false);
    private JobArgument<String> oidcTrustStoreType = new JobArgument<String>("oidcTrustStoreType", false);

    public JobArgument<String> getApiURL() {
        return apiURL;
    }

    public JobArgument<String> getBody() {
        return body;
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

}