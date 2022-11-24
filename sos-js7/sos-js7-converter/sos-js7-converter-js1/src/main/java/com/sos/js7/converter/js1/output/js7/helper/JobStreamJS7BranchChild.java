package com.sos.js7.converter.js1.output.js7.helper;

import java.util.List;

public class JobStreamJS7BranchChild {

    private List<JobStreamJS7Branch> children;
    private List<JobStreamJS1JS7Job> jobs;

    public List<JobStreamJS7Branch> getChildren() {
        return children;
    }

    public void setChildren(List<JobStreamJS7Branch> val) {
        children = val;
    }

    public List<JobStreamJS1JS7Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<JobStreamJS1JS7Job> val) {
        jobs = val;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{jobs=" + jobs);
        sb.append(",children=" + children);
        sb.append("}");
        return sb.toString();
    }

}
