package com.sos.js7.history.helper;

import com.sos.inventory.model.job.JobCriticality;

public class CachedWorkflowJob {

    private final JobCriticality criticality;
    private final String title;
    private final Integer warnIfLonger;
    private final Integer warnIfShorter;

    public CachedWorkflowJob(JobCriticality criticality, String title, Integer warnIfLonger, Integer warnIfShorter) {
        this.criticality = criticality;
        this.title = title;
        this.warnIfLonger = warnIfLonger;
        this.warnIfShorter = warnIfShorter;
    }

    public JobCriticality getCriticality() {
        return criticality;
    }

    public String getTitle() {
        return title;
    }

    public Integer getWarnIfLonger() {
        return warnIfLonger;
    }

    public Integer getWarnIfShorter() {
        return warnIfShorter;
    }

}
