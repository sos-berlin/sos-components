package com.sos.joc.db.history;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.joc.model.common.Folder;
import com.sos.joc.model.common.HistoryStateText;

public class HistoryFilter {
    
    private Set<Long> historyIds;
    private Set<Long> taskIds;
    private Set<String> workflows;
    private Date executedFrom;
    private Date executedTo;
    private Date startTime;
    private Date endTime;
    private String schedulerId = "";
    private Set<Folder> folders;
    private Set<String> states;
    private Map<String, Set<String>> orders;
    private Map<String, Set<String>> excludedOrders;
    private Set<String> jobs;
    private Set<String> excludedJobs;
    private Integer limit;

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getLimit() {
        return limit;
    }
    
    public void setFolders(Set<Folder> folders) {
        this.folders = folders;
    }

    public Set<Folder> getFolders() {
        return folders;
    }
    
    public void addFolders(Collection<Folder> folders) {
        if (folders != null) {
            if (this.folders == null) {
                this.folders = new HashSet<Folder>();
            }
            this.folders.addAll(folders);
        }
    }
    
    public void addFolder(Folder folder) {
        if (folder != null) {
            if (this.folders == null) {
                this.folders = new HashSet<Folder>();
            }
            this.folders.add(folder);
        }
    }

    public void addFolder(String folder, boolean recursive) {
        if (folder != null) {
            if (this.folders == null) {
                this.folders = new HashSet<Folder>();
            }
            Folder filterFolder = new Folder();
            filterFolder.setFolder(folder);
            filterFolder.setRecursive(recursive);
            this.folders.add(filterFolder);
        }
    }
    
    public void addState(HistoryStateText state) {
        if (state != null) {
            if (states == null) {
                states = new HashSet<String>();
            }
            states.add(state.toString());
        }
    }

    public void setState(HistoryStateText state) {
        if (state != null) {
            if (states == null) {
                states = new HashSet<String>();
            }
            states.clear();
            states.add(state.toString());
        }
    }
    
    public void setState(Collection<HistoryStateText> states) {
        if (states != null) {
            this.states = states.stream().map(HistoryStateText::toString).collect(Collectors.toSet());
        } else {
            this.states = null;
        }
    }
    
    public Set<String> getStates() {
        return states;
    }

    public void setExecutedFrom(Date executedFrom) {
        this.executedFrom = executedFrom;
    }

    public Date getExecutedFrom() {
        return executedFrom;
    }
    
    public void setExecutedTo(Date executedTo) {
        this.executedTo = executedTo;
    }

    public Date getExecutedTo() {
        return executedTo;
    }

    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

    public String getSchedulerId() {
        return schedulerId;
    }

    public void setStartTime(final Date start) {
        startTime = start;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setEndTime(final Date end) {
        endTime = end;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setHistoryIds(Set<Long> historyIds) {
        this.historyIds = historyIds;
    }
    
    public void setHistoryIds(List<Long> historyIds) {
        this.historyIds = new HashSet<Long>(historyIds);
    }

    public Set<Long> getHistoryIds() {
        return historyIds;
    }
    
    public void setWorkflows(Set<String> workflows) {
        this.workflows = workflows;
    }
    
    public Set<String> getWorkflows() {
        return workflows;
    }
    
    public void addWorkflow(String workflow) {
        if (workflows == null) {
            workflows = new HashSet<String>();
        }
        workflows.add(workflow);
    }
    
    public Map<String, Set<String>> getOrders() {
        return orders;
    }

    public Map<String, Set<String>> getExcludedOrders() {
        return excludedOrders;
    }
    
    public void setOrders(Map<String, Set<String>> orders) {
        this.orders = orders;
    }

    public void setExcludedOrders(Map<String, Set<String>> orders) {
        this.excludedOrders = orders;
    }
    
    public Set<String> getJobs() {
        return jobs;
    }

    public void addJob(String job) {
        if (job != null) {
            if (jobs == null) {
                jobs = new HashSet<String>();
            }
            jobs.add(job);
        }
    }
    
    public void setJobs(Set<String> jobs) {
        this.jobs = jobs;
    }

    public Set<String> getExcludedJobs() {
        return excludedJobs;
    }

    public void addExcludedJob(String job) {
        if (job != null) {
            if (excludedJobs == null) {
                excludedJobs = new HashSet<String>();
            }
            excludedJobs.add(job);
        }
    }
    
    public void setExcludedJobs(Set<String> jobs) {
        excludedJobs = jobs;
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
