package com.sos.joc.history.helper;

import com.sos.inventory.model.job.JobCriticality;

public class CachedWorkflowJob {

    private final JobCriticality criticality;
    private final String title;
    private final Integer warnIfLonger;
    private final Integer warnIfShorter;
    private final String notification;

    public CachedWorkflowJob(JobCriticality criticality, String title, Integer warnIfLonger, Integer warnIfShorter, String notification) {
        this.criticality = criticality;
        this.title = title;
        this.warnIfLonger = warnIfLonger;
        this.warnIfShorter = warnIfShorter;
        this.notification = notification;
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

    public String getNotification() {
        return notification;
    }
}
