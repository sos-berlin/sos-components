package com.sos.js7.converter.js1.output.js7.helper;

import java.util.List;

public class JobStreamClosingJob {

    private final JobStreamJS1JS7Job job;
    private final List<JobStreamJS1JS7Job> used;

    public JobStreamClosingJob(JobStreamJS1JS7Job job, List<JobStreamJS1JS7Job> used) {
        this.job = job;
        this.used = used;
    }

    public JobStreamJS1JS7Job getJob() {
        return job;
    }

    public List<JobStreamJS1JS7Job> getUsed() {
        return used;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("job=").append(job.getJS7JobName());
        sb.append(",used=").append(used);
        return sb.toString();
    }

}
