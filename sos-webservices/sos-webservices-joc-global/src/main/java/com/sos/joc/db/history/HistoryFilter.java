package com.sos.joc.db.history;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.joc.model.common.Folder;
import com.sos.joc.model.common.HistoryStateText;
import com.sos.joc.model.inventory.common.JobCriticality;

public class HistoryFilter {
    
    private Set<Long> historyIds;
    private Set<String> workflows;
    private Date executedFrom;
    private Date executedTo;
    private Date startTime;
    private Date endTime;
    private String schedulerId = "";
    private Set<Folder> folders;
    private Set<HistoryStateText> states;
    private Set<String> criticalities;
    private Map<String, Set<String>> orders;
    private Map<String, Set<String>> excludedOrders;
    private Map<String, Set<String>> jobs;
    private Map<String, Set<String>> excludedJobs;
    private boolean mainOrder = false;
    private Integer limit;

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getLimit() {
        if (limit == null) {
            return -1;
        }
        return limit;
    }
    
    public void setMainOrder(boolean value) {
        this.mainOrder = value;
    }

    public boolean isMainOrder() {
        return mainOrder;
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
                states = new HashSet<HistoryStateText>();
            }
            states.add(state);
        }
    }

    public void setState(HistoryStateText state) {
        if (state != null) {
            if (states == null) {
                states = new HashSet<HistoryStateText>();
            }
            states.clear();
            states.add(state);
        }
    }
    
    public void setState(Collection<HistoryStateText> states) {
        if (states != null) {
            this.states = states.stream().collect(Collectors.toSet());
        } else {
            this.states = null;
        }
    }
    
    public Set<HistoryStateText> getStates() {
        return states;
    }
    
    public void setCriticalities(Collection<JobCriticality> criticalities) {
        if (criticalities != null) {
            this.criticalities = criticalities.stream().map(c -> c.value().toLowerCase()).collect(Collectors.toSet());
        } else {
            this.criticalities = null;
        }
    }
    
    public Set<String> getCriticalities() {
        return criticalities;
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

    public void setHistoryIds(Collection<Long> historyIds) {
        if (historyIds != null) {
            this.historyIds = historyIds.stream().filter(Objects::nonNull).collect(Collectors.toSet()); 
        } else {
            this.historyIds = null;
        }
    }

    public Set<Long> getHistoryIds() {
        return historyIds;
    }
    
    public void setWorkflows(Collection<String> workflows) {
        if (workflows != null) {
            this.workflows = workflows.stream().filter(Objects::nonNull).collect(Collectors.toSet()); 
        } else {
            this.workflows = null;
        }
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
    
    public Map<String, Set<String>> getJobs() {
        return jobs;
    }
    
    public void setJobs(Map<String, Set<String>> jobs) {
        this.jobs = jobs;
    }

    public Map<String, Set<String>> getExcludedJobs() {
        return excludedJobs;
    }

    public void setExcludedJobs(Map<String, Set<String>> jobs) {
        excludedJobs = jobs;
    }

}
