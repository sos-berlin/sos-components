package com.sos.js7.job.jocapi.helper;

import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArguments;

public class TestApiExecutorOIDCJobArguments extends JobArguments {

    private JobArgument<String> apiURL = new JobArgument<String>("api_url", true);
    private JobArgument<String> body = new JobArgument<String>("body", true);
    private JobArgument<String> clientId = new JobArgument<String>("clientId", true);;
    private JobArgument<String> clientSecret = new JobArgument<String>("clientSecret", true);;
    private JobArgument<String> identityService = new JobArgument<String>("identityService", true);;
    private JobArgument<String> issuer = new JobArgument<String>("issuer", true);
    private JobArgument<String> oidcTrustStorePath = new JobArgument<String>("oidcTrustStorePath", true);
    private JobArgument<String> oidcTrustStorePasswd = new JobArgument<String>("oidcTrustStorePasswd", true);
    private JobArgument<String> oidcTrustStoreType = new JobArgument<String>("oidcTrustStoreType", true);

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