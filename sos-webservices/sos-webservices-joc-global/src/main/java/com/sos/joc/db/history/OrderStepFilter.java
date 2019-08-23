package com.sos.joc.db.history;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OrderStepFilter extends HistoryFilter {

    private Set<Long> taskIds;
    private String orderHistoryId;
    private String status = "";
    private Set<String> jobs;
    private Set<String> excludedJobs;
    private Set<String> states;

    public OrderStepFilter() {
        super();
    }

    public Set<String> getJobs() {
        return jobs;
    }

    public Set<String> getExcludedJobs() {
        return excludedJobs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void addState(String state) {
        if (state != null) {
            if (states == null) {
                states = new HashSet<String>();
            }
            states.add(state);
        }
    }

    public void addJob(String job) {
        if (job != null) {
            if (jobs == null) {
                jobs = new HashSet<String>();
            }
            jobs.add(job);
        }
    }

    public void addExcludedJob(String job) {
        if (job != null) {
            if (excludedJobs == null) {
                excludedJobs = new HashSet<String>();
            }
            excludedJobs.add(job);
        }
    }

    public String getOrderHistoryId() {
        return orderHistoryId;
    }

    public void setOrderHistoryId(String orderHistoryId) {
        this.orderHistoryId = orderHistoryId;
    }

    public Set<String> getStates() {
        return states;
    }

    public Set<Long> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(Set<Long> taskIds) {
        this.taskIds = taskIds;
    }

    public void setTaskIds(List<Long> taskIds) {
        this.taskIds = new HashSet<Long>(taskIds);
    }
    
}
