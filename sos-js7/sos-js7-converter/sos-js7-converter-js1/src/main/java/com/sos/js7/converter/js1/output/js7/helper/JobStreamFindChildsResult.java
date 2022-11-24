package com.sos.js7.converter.js1.output.js7.helper;

import java.util.ArrayList;
import java.util.List;

public class JobStreamFindChildsResult {

    List<JobStreamJS1JS7Job> jobs = new ArrayList<>();
    List<JobStreamClosingJob> closingJobs = new ArrayList<>();

    public List<JobStreamJS1JS7Job> getJobs() {
        return jobs;
    }

    public List<JobStreamClosingJob> getClosingJobs() {
        return closingJobs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("jobs=").append(jobs);
        sb.append(",closingJobs=").append(closingJobs);
        return sb.toString();
    }

}
