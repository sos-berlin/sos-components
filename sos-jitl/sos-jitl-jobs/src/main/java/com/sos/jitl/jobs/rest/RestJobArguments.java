package com.sos.jitl.jobs.rest;

import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArguments;

public class RestJobArguments extends JobArguments {
    private final JobArgument<String> myReturnVariable = new JobArgument<>("return_variable", false);
    private final JobArgument<String> myRequest = new JobArgument<>("request", false);
    private final JobArgument<String> LogItems = new JobArgument<>("log_items", false,"request:body");
    private final JobArgument<String> keystoreFile = new JobArgument<>("js7.web.https.keystore.file", false);
    private final JobArgument<String> keystoreKeyPassword = new JobArgument<>("js7.web.https.keystore.key-password", false, SOSArgument.DisplayMode.MASKED);
    private final JobArgument<String> keystoreStorePassword = new JobArgument<>("js7.web.https.keystore.store-password", false, SOSArgument.DisplayMode.MASKED);
    private final JobArgument<String> keystoreAlias = new JobArgument<>("js7.web.https.keystore.alias", false);
    private final JobArgument<String> truststoreFile = new JobArgument<>("js7.web.https.truststore.file", false);
    private final JobArgument<String> truststoreStorePassword = new JobArgument<>("js7.web.https.truststore.store-password", false, SOSArgument.DisplayMode.MASKED);
    private final JobArgument<String> apiServerCSFile = new JobArgument<>("js7.api-server.cs-file", false);
    private final JobArgument<String> apiServerCSKey = new JobArgument<>("js7.api-server.cs-key", false);
    private final JobArgument<String> apiServerCSPassword = new JobArgument<>("js7.api-server.cs-password", false, SOSArgument.DisplayMode.MASKED);
    private final JobArgument<String> apiServerUsername = new JobArgument<>("js7.api-server.username", false);
    private final JobArgument<String> apiServerToken = new JobArgument<>("js7.api-server.token", false);
    private final JobArgument<String> apiServerPassword = new JobArgument<>("js7.api-server.password", false, SOSArgument.DisplayMode.MASKED);
    private final JobArgument<String> apiServerPrivateKeyPath = new JobArgument<>("js7.api-server.privatekey.path", false);
    private final JobArgument<String> editorUrl = new JobArgument<>("js7.api-server.url", false);


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
}
