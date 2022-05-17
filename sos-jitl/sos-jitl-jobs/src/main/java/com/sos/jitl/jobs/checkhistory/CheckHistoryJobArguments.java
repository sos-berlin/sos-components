package com.sos.jitl.jobs.checkhistory;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;

public class CheckHistoryJobArguments extends JobArguments {

    private JobArgument<String> controllerId = new JobArgument<String>("controller-id", false);
    private JobArgument<String> workflow = new JobArgument<String>("workflow", false);
    private JobArgument<String> job = new JobArgument<String>("job", false);
    private JobArgument<String> jocUrl = new JobArgument<String>("joc_url", false);
    private JobArgument<String> account = new JobArgument<String>("account", false);
    private JobArgument<String> password = new JobArgument<String>("password", false);
    private JobArgument<String> query = new JobArgument<String>("query", false);

    public CheckHistoryJobArguments() {
        super(new SOSCredentialStoreArguments());
    }

    public String getControllerId() {
        return controllerId.getValue();
    }

    public void setControllerId(String controller) {
        this.controllerId.setValue(controller);
    }

    public String getJob() {
        return job.getValue();
    }

    public void setJob(String job) {
        this.job.setValue(job);
    }

    public String getWorkflow() {
        return workflow.getValue();
    }

    public void setWorkflow(String workflow) {
        this.workflow.setValue(workflow);
    }

    public String getJocUrl() {
        return jocUrl.getValue();
    }

    public void setJocUrl(String jocUrl) {
        this.jocUrl.setValue(jocUrl);
    }

    public String getAccount() {
        return account.getValue();
    }

    public void setAccount(String account) {
        this.account.setValue(account);
    }

    public String getPassword() {
        return password.getValue();
    }

    public void setPassword(String password) {
        this.password.setValue(password);
    }

    public String getQuery() {
        return query.getValue();
    }

    public void setQuery(String query) {
        this.query.setValue(query);
    }

}
