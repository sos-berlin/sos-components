package com.sos.jitl.jobs.checkhistory;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;

public class CheckHistoryJobArguments extends JobArguments {

    private JobArgument<String> controllerId = new JobArgument<String>("controller_id", false);
    private JobArgument<String> workflow = new JobArgument<String>("workflow", false);
    private JobArgument<String> job = new JobArgument<String>("job", false);
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

    public String getQuery() {
        return query.getValue();
    }

    public void setQuery(String query) {
        this.query.setValue(query);
    }

}
