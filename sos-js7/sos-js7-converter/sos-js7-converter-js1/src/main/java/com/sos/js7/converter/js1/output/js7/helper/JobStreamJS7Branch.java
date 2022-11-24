package com.sos.js7.converter.js1.output.js7.helper;

import java.util.ArrayList;
import java.util.List;

public class JobStreamJS7Branch {

    private String name;
    private String path;
    private List<JobStreamJS7Branch> childBranches = new ArrayList<>();
    private JobStreamJS1JS7Job childJob;

    public JobStreamJS7Branch(String parentPath, String name) {
        this.name = name;
        this.path = parentPath == null ? name : parentPath + "/" + name;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public List<JobStreamJS7Branch> getChildBranches() {
        return childBranches;
    }

    public void addChildBranch(JobStreamJS7Branch val) {
        childBranches.add(val);
    }

    public JobStreamJS1JS7Job getChildJob() {
        return childJob;
    }

    public void setChildJob(JobStreamJS1JS7Job val) {
        childJob = val;
        if (childJob != null) {
            childJob.setJS7BranchPath(path);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // sb.append("name=" + name);
        sb.append("path=" + path);
        sb.append(",childJob=" + childJob);
        sb.append(",childBranches=" + childBranches);
        return sb.toString();
    }

}
