package com.sos.joc.history.helper;

import java.util.Map;

import com.sos.inventory.model.job.JobCriticality;

public class CachedWorkflow {

    private final String path;
    private final String title;
    private final Map<String, CachedWorkflowJob> jobs;

    public CachedWorkflow(final String path, final String title, final Map<String, CachedWorkflowJob> jobs) {
        this.path = path;
        this.title = title;
        this.jobs = jobs;
    }

    public String getPath() {
        return path;
    }

    public String getTitle() {
        return title;
    }

    public CachedWorkflowJob getJob(String name) {
        if (jobs == null || jobs.isEmpty()) {
            return defaultJob();
        }
        CachedWorkflowJob wj = jobs.get(name);
        return wj == null ? defaultJob() : wj;
    }

    private CachedWorkflowJob defaultJob() {
        return new CachedWorkflowJob(JobCriticality.NORMAL, null, null, null, null, null, null, null, null);
    }
}
