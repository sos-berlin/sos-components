package com.sos.jitl.jobs.inventory.setjobresource;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArguments;

public class SetJobResourceJobArguments extends JobArguments {

    private JobArgument<String> controllerId = new JobArgument<String>("controller_id", false);
    private JobArgument<String> jobResource = new JobArgument<String>("job_resource", true);
    private JobArgument<String> key = new JobArgument<String>("key", true);
    private JobArgument<String> value = new JobArgument<String>("value", false);
    private JobArgument<String> file = new JobArgument<String>("file", false);
    private JobArgument<String> timeZone = new JobArgument<String>("time_zone", false);
    private JobArgument<String> environmentVariable = new JobArgument<String>("environment_variable", false);
    private JobArgument<String> enciphermentCertificate = new JobArgument<String>("encipherment_certificate", false);
    private String encodedFileReturn;

    public SetJobResourceJobArguments() {
        super(new CredentialStoreArguments());
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

    public String getFile() {
        return file.getValue();
    }

    public void setFile(String file) {
        this.value.setValue(file);
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

    public String getEnciphermentCertificate() {
        return enciphermentCertificate.getValue();
    }

    public void setEnciphermentCertificate(String encryptCert) {
        this.enciphermentCertificate.setValue(encryptCert);
    }

    
    public String getEncodedFileReturn() {
        return encodedFileReturn;
    }

    
    public void setEncodedFileReturn(String encodedFileReturn) {
        this.encodedFileReturn = encodedFileReturn;
    }

}
