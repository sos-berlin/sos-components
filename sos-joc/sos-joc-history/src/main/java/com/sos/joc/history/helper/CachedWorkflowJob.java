package com.sos.joc.history.helper;

import com.sos.inventory.model.job.JobCriticality;

public class CachedWorkflowJob {

    private final JobCriticality criticality;
    private final String title;
    private final String agentName;
    private final String subagentClusterId;
    private final String warnIfLonger;
    private final String warnIfShorter;
    private final String notification;

    public CachedWorkflowJob(JobCriticality criticality, String title, String agentName, String subagentClusterId, String warnIfLonger,
            String warnIfShorter, String notification) {
        this.criticality = criticality;
        this.title = title;
        this.agentName = agentName;
        this.subagentClusterId = subagentClusterId;
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

    public String getAgentName() {
        return agentName;
    }

    public String getSubagentClusterId() {
        return subagentClusterId;
    }

    public String getWarnIfLonger() {
        return warnIfLonger;
    }

    public String getWarnIfShorter() {
        return warnIfShorter;
    }

    public String getNotification() {
        return notification;
    }
}
