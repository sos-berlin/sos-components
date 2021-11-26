package com.sos.jitl.jobs.sap.common;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.sos.commons.sign.keys.keyStore.KeyStoreType;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;
import com.sos.jitl.jobs.sap.common.bean.AbstractJob;
import com.sos.jitl.jobs.sap.common.bean.RunIds;

public class CommonJobArguments extends JobArguments {
    
    private static final String hanaPrefix = "hana";
    
    /* internal arguments */
    private JobArgument<Long> iJobId = new JobArgument<Long>(null, false, 0L);
    private JobArgument<String> iScheduleId = new JobArgument<String>(null, false, "");
    private JobArgument<String> iRunId = new JobArgument<String>(null, false, "");
    private JobArgument<RunIds.Scope> iRunScope = new JobArgument<RunIds.Scope>(null, false, RunIds.Scope.SCHEDULE);

    private JobArgument<Long> checkInterval = new JobArgument<Long>(hanaPrefix + "_check_interval", false, TimeUnit.SECONDS.toSeconds(1));

    /* http credentialStore arguments */
    private JobArgument<String> credentialStoreFile = new JobArgument<String>("credential_store_file", false);
    private JobArgument<String> credentialStoreKeyFile = new JobArgument<String>("credential_store_key_file", false);
    private JobArgument<String> credentialStorePassword = new JobArgument<String>("credential_store_password", false);
    private JobArgument<String> credentialStoreEntryPath = new JobArgument<String>("credential_store_entry_path", false);

    /* http client arguments */
    private JobArgument<Long> connectionTimeout = new JobArgument<Long>(hanaPrefix + "_connection_timeout", false, TimeUnit.SECONDS.toSeconds(2));
    private JobArgument<Long> socketTimeout = new JobArgument<Long>(hanaPrefix + "_socket_timeout", false, TimeUnit.SECONDS.toSeconds(5));
    private JobArgument<Boolean> hostnameVerification = new JobArgument<Boolean>(hanaPrefix + "_hostname_verification", false, true);
    private JobArgument<Path> truststorePath = new JobArgument<Path>(hanaPrefix + "_truststore_path", false);
    private JobArgument<String> truststorePwd = new JobArgument<String>(hanaPrefix + "_truststore_password", false, "", DisplayMode.MASKED, null);
    private JobArgument<KeyStoreType> truststoreType = new JobArgument<KeyStoreType>(hanaPrefix + "_truststore_type", false, KeyStoreType.PKCS12);
    private JobArgument<String> user = new JobArgument<String>(hanaPrefix + "_user", true);
    private JobArgument<String> pwd = new JobArgument<String>(hanaPrefix + "_password", true, DisplayMode.MASKED);
    private JobArgument<String> mandant = new JobArgument<String>(hanaPrefix + "_mandant", false);
    private JobArgument<URI> uri = new JobArgument<URI>(hanaPrefix + "_base_uri", true);
    
    /* create job arguments */
    // has to set required programmatically
    private JobArgument<URI> actionEndpoint = new JobArgument<URI>(hanaPrefix + "_action_endpoint", false);
    // DELETE, PUT, POST (default), GET
    private JobArgument<AbstractJob.HttpMethod> actionEndpointHTTPMethod = new JobArgument<AbstractJob.HttpMethod>(hanaPrefix + "_action_http_method",
            false, AbstractJob.HttpMethod.POST);
    private JobArgument<String> jobDescription = new JobArgument<String>(hanaPrefix + "_job_description", false);

    /* create schedule arguments */
    // one of them has to set required programmatically
    private JobArgument<String> jobName = new JobArgument<String>(hanaPrefix + "_job_name", false);
    private JobArgument<Long> jobId = new JobArgument<Long>(hanaPrefix + "_job_id", false);
    // has to set required programmatically
    private JobArgument<String> scheduleId = new JobArgument<String>(hanaPrefix + "_schedule_id", false);

    public JobArgument<Long> getCheckInterval() {
        return checkInterval;
    }
    
    /* http credentialStore arguments */
    public String getCredentialStoreFile() {
        return credentialStoreFile.getValue();
    }

    public String getCredentialStoreKeyFile() {
        return credentialStoreKeyFile.getValue();
    }

    public String getCredentialStorePassword() {
        return credentialStorePassword.getValue();
    }

    public String getCredentialStoreEntryPath() {
        return credentialStoreEntryPath.getValue();
    }
    /* end of credentialStore arguments */
    
    /* http client arguments */
    public JobArgument<Long> getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public JobArgument<Long> getSocketTimeout() {
        return socketTimeout;
    }
    
    public JobArgument<Boolean> getHostnameVerification() {
        return hostnameVerification;
    }
    
    public JobArgument<Path> getTruststorePath() {
        return truststorePath;
    }
    
    public JobArgument<String> getTruststorePwd() {
        return truststorePwd;
    }
    
    public JobArgument<KeyStoreType> getTruststoreType() {
        return truststoreType;
    }
    
    public JobArgument<String> getUser() {
        return user;
    }
    
    public JobArgument<String> getMandant() {
        return mandant;
    }
    
    public JobArgument<String> getPwd() {
        return pwd;
    }
    
    public JobArgument<URI> getUri() {
        return uri;
    }
    /* end of http client arguments */
    
    /* create job arguments */
    public JobArgument<URI> getActionEndpoint() {
        return actionEndpoint;
    }
    
    public JobArgument<AbstractJob.HttpMethod> getActionEndpointHTTPMethod() {
        return actionEndpointHTTPMethod;
    }
    
    public JobArgument<String> getJobDescription() {
        return jobDescription;
    }
    
    public List<JobArgument<?>> setCreateJobArgumentsRequired() {
        actionEndpoint.setRequired(true);
        return Collections.singletonList(actionEndpoint);
    }
    /* end of create job arguments */
    
    /* create schedule arguments */
    public JobArgument<Long> getJobId() {
        return jobId;
    }
    
    public void setJobId(Long val) {
        jobId.setValue(val);
    }
    
    public JobArgument<String> getJobName() {
        return jobName;
    }

    public void setJobName(String val) {
        jobName.setValue(val);
    }
    
    public JobArgument<String> getScheduleId() {
        return scheduleId;
    }
    
    public List<JobArgument<?>> setCreateScheduleArgumentsRequired() {
        scheduleId.setRequired(true);
        return Collections.singletonList(scheduleId);
    }
    /* end of create schedule arguments */
    
    public String idsToString() {
        if (iRunId.isEmpty()) {
            return String.format("jobId=%d, scheduleId=%s", iJobId.getValue(), iScheduleId.getValue());
        } else {
            return String.format("jobId=%d, scheduleId=%s, runId=%s", iJobId.getValue(), iScheduleId.getValue(), iRunId.getValue());
        }
    }
    
    public RunIds getIds() {
        return new RunIds(iJobId.getValue(), iScheduleId.getValue(), iRunScope.getValue());
    }
    
    public void setIds(RunIds runIds) {
        iJobId.setValue(runIds.getJobId());
        iScheduleId.setValue(runIds.getScheduleId());
        iRunId.setValue(runIds.getRunId());
    }
    
    public void setIJobId(Long val) {
        iJobId.setValue(val);
    }
    
    public void setIScheduleId(String val) {
        iScheduleId.setValue(val);
    }
    
    public void setIRunId(String val) {
        iRunId.setValue(val);
    }
    
    public void setIRunScope(RunIds.Scope val) {
        iRunScope.setValue(val);
    }
    
}
