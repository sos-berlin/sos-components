package com.sos.js7.history.helper;

import java.util.Map;

import com.sos.inventory.model.job.JobCriticality;

public class CachedWorkflow {

    private final String path;
    private final Map<String, CachedWorkflowJob> jobs;

    public CachedWorkflow(final String path, final Map<String, CachedWorkflowJob> jobs) {
        this.path = path;
        this.jobs = jobs;
    }

    public String getPath() {
        return path;
    }

    public CachedWorkflowJob getJob(String name) {
        if (jobs == null || jobs.isEmpty()) {
            return defaultJob(name);
        }
        CachedWorkflowJob wj = jobs.get(name);
        return wj == null ? defaultJob(name) : wj;
    }

    private CachedWorkflowJob defaultJob(String name) {
        return new CachedWorkflowJob(null, JobCriticality.NORMAL);
    }
}
