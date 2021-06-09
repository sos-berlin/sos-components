package com.sos.js7.history.helper;

import com.sos.inventory.model.job.JobCriticality;

public class CachedWorkflowJob {

    private final JobCriticality criticality;
    private final String title;
    private final String taskIfLongerThan;
    private final String taskIfShorterThan;

    public CachedWorkflowJob(JobCriticality criticality, String title, String taskIfLongerThan, String taskIfShorterThan) {
        this.criticality = criticality;
        this.title = title;
        this.taskIfLongerThan = taskIfLongerThan;
        this.taskIfShorterThan = taskIfShorterThan;
    }

    public JobCriticality getCriticality() {
        return criticality;
    }

    public String getTitle() {
        return title;
    }

    public String getTaskIfLongerThan() {
        return taskIfLongerThan;
    }

    public String getTaskIfShorterThan() {
        return taskIfShorterThan;
    }

}
