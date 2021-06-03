package com.sos.js7.history.helper;

import com.sos.inventory.model.job.JobCriticality;

public class CachedWorkflowJob {

    private final String title;
    private final JobCriticality criticality;

    public CachedWorkflowJob(String title, JobCriticality criticality) {
        this.title = title;
        this.criticality = criticality;
    }

    public String getTitle() {
        return title;
    }

    public JobCriticality getCriticality() {
        return criticality;
    }
}
