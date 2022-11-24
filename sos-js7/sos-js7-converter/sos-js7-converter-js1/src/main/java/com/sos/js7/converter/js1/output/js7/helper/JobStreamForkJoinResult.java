package com.sos.js7.converter.js1.output.js7.helper;

import java.util.List;

public class JobStreamForkJoinResult {

    private List<JobStreamClosingJob> nestedClosingJobs;
    private List<JobStreamClosingJob> closingJobs;

    public JobStreamForkJoinResult() {
    }

    public void setNestedClosingJobs(List<JobStreamClosingJob> val) {
        nestedClosingJobs = val;
    }

    public List<JobStreamClosingJob> getNestedClosingJobs() {
        return nestedClosingJobs;
    }

    public void setClosingJobs(List<JobStreamClosingJob> val) {
        closingJobs = val;
    }

    public List<JobStreamClosingJob> getClosingJobs() {
        return closingJobs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("nestedClosingJobs=").append(nestedClosingJobs);
        sb.append(",closingJobs=").append(closingJobs);
        return sb.toString();
    }
}
