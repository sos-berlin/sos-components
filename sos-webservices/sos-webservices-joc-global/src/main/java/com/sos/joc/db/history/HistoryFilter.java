package com.sos.joc.db.history;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sos.joc.model.common.Folder;

public class HistoryFilter {
    
    private Set<Long> historyIds;
    private Set<String> workflows;
    private Date executedFrom;
    private Date executedTo;
    private Date startTime;
    private Date endTime;
    private String schedulerId = "";
    private String orderId;
    private String workflow;
    private Set<Folder> folders;
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

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    public String getWorkflow() {
        return workflow;
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
    
    public Set<String> getWorkflows() {
        return workflows;
    }
    
    public void addWorkflow(String workflow) {
        if (workflows == null) {
            workflows = new HashSet<String>();
        }
        workflows.add(workflow);

    }

}
