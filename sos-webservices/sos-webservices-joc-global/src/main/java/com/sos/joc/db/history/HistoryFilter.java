package com.sos.joc.db.history;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.inventory.model.job.JobCriticality;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.common.HistoryStateText;
import com.sos.joc.model.order.OrderStateText;

public class HistoryFilter {
    
    private Collection<List<Long>> historyIds;
    private Date executedFrom;
    private Date executedTo;
    private Date stateFrom;
    private Date stateTo;
    private Date startTime;
    private Date endTime;
    private Date endFrom;
    private Date endTo;

    public Date getEndFrom() {
		return endFrom;
	}

	public void setEndFrom(Date endFrom) {
		this.endFrom = endFrom;
	}

	public Date getEndTo() {
		return endTo;
	}

	public void setEndTo(Date endTo) {
		this.endTo = endTo;
	}

	private Collection<String> controllerIds;
    private Collection<String> agentIds;
    private Set<Folder> folders;
    private Set<HistoryStateText> states;
    private Set<OrderStateText> orderStates;
    private Set<Integer> criticalities;
    private Map<String, Set<String>> orders;
//    private Map<String, Set<String>> excludedOrders;
    private Map<String, Set<String>> jobs;
    private Map<String, Set<String>> excludedJobs;
    private Set<String> excludedWorkflows;
    private boolean mainOrder = false;
    private Integer limit;
    private String jobName;
    private String orderId;
    private String workflowPath;
    private String workflowName;
    private Collection<List<String>> workflowNames;
    private Collection<List<String>> mainOrderIds;
    private Collection<List<Long>> nonExclusiveHistoryIds;
    private LongSummaryStatistics nonExclusiveHistoryIdsStats;
    
    
    private boolean hasPermission = true;
    private boolean taskFromHistoryIdAndNode = false;
    private boolean folderPermissionsAreChecked = false;

    
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
    
    public void setOrderState(Collection<OrderStateText> states) {
        if (states != null) {
            this.orderStates = states.stream().collect(Collectors.toSet());
        } else {
            this.orderStates = null;
        }
    }
    
    public Set<OrderStateText> getOrderStates() {
        return orderStates;
    }
    
    public void setCriticalities(Collection<JobCriticality> criticalities) {
        if (criticalities != null) {
            this.criticalities = criticalities.stream().map(JobCriticality::intValue).collect(Collectors.toSet());
        } else {
            this.criticalities = null;
        }
    }
    
    public Set<Integer> getCriticalities() {
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
    
    public void setStateFrom(Date stateFrom) {
        this.stateFrom = stateFrom;
    }

    public Date getStateFrom() {
        return stateFrom;
    }
    
    public void setStateTo(Date stateTo) {
        this.stateTo = stateTo;
    }

    public Date getStateTo() {
        return stateTo;
    }

    public void setControllerIds(Collection<String> controllerIds) {
        this.controllerIds = controllerIds;
    }

    public Collection<String> getControllerIds() {
        return controllerIds;
    }
    
    public void setAgentIds(Collection<String> agentIds) {
        this.agentIds = agentIds;
    }

    public Collection<String> getAgentIds() {
        return agentIds;
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
            AtomicInteger counter = new AtomicInteger();
            this.historyIds = historyIds.stream().distinct().collect(Collectors.groupingBy(it -> counter.getAndIncrement()
                    / SOSHibernate.LIMIT_IN_CLAUSE)).values();
        } else {
            this.historyIds = null;
        }
    }

    public Collection<List<Long>> getHistoryIds() {
        return historyIds;
    }
    
    public void setNonExclusiveHistoryIds(Collection<Long> historyIds) {
        if (historyIds != null) {
            AtomicInteger counter = new AtomicInteger();
            this.nonExclusiveHistoryIds = historyIds.stream().distinct().collect(Collectors.groupingBy(it -> counter.getAndIncrement()
                    / SOSHibernate.LIMIT_IN_CLAUSE)).values();
            if (historyIds.size() > 10) {
                this.nonExclusiveHistoryIdsStats = historyIds.stream().collect(Collectors.summarizingLong(i -> i));
            } else {
                this.nonExclusiveHistoryIdsStats = null;
            }
        } else {
            this.nonExclusiveHistoryIds = null;
            this.nonExclusiveHistoryIdsStats = null;
        }
    }

    public Collection<List<Long>> getNonExclusiveHistoryIds() {
        return nonExclusiveHistoryIds;
    }
    
    public LongSummaryStatistics getNonExclusiveHistoryIdsStats() {
        return nonExclusiveHistoryIdsStats;
    }
    
    public Map<String, Set<String>> getOrders() {
        return orders;
    }

//    public Map<String, Set<String>> getExcludedOrders() {
//        return excludedOrders;
//    }
    
    public void setOrders(Map<String, Set<String>> orders) {
        this.orders = orders;
    }

//    public void setExcludedOrders(Map<String, Set<String>> orders) {
//        this.excludedOrders = orders;
//    }
    
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
    
    public void setExcludedWorkflows(Set<String> workflows) {
        this.excludedWorkflows = workflows;
    }

    public Set<String> getExcludedWorkflows() {
        return excludedWorkflows;
    }
    
    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName == null ? null : jobName.trim();
    }
    
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId == null ? null : orderId.trim();
    }
    
    public String getWorkflowPath() {
        return workflowPath;
    }

    public void setWorkflowPath(String workflowPath) {
        this.workflowPath = workflowPath == null ? null : workflowPath.trim();
    }
    
    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName == null ? null : workflowName.trim();
    }
    
    public Collection<List<String>> getWorkflowNames() {
        return workflowNames;
    }

    public void setWorkflowNames(Collection<String> workflowNames) {
        if (workflowNames != null) {
            AtomicInteger counter = new AtomicInteger();
            this.workflowNames = workflowNames.stream().distinct().collect(Collectors.groupingBy(it -> counter.getAndIncrement()
                    / SOSHibernate.LIMIT_IN_CLAUSE)).values();
        } else {
            this.workflowNames = null;
        }
    }
    
    public Collection<List<String>> getMainOrderIds() {
        return mainOrderIds;
    }

    public void setMainOrderIds(Collection<String> mainOrderIds) {
        if (mainOrderIds != null) {
            AtomicInteger counter = new AtomicInteger();
            this.mainOrderIds = mainOrderIds.stream().distinct().collect(Collectors.groupingBy(it -> counter.getAndIncrement()
                    / SOSHibernate.LIMIT_IN_CLAUSE)).values();
        } else {
            this.mainOrderIds = null;
        }
    }

    public boolean hasPermission() {
        return hasPermission;
    }

    public void setHasPermission(boolean hasPermission) {
        this.hasPermission = hasPermission;
    }

    public boolean getTaskFromHistoryIdAndNode() {
        return taskFromHistoryIdAndNode;
    }

    public void setTaskFromHistoryIdAndNode(boolean taskFromHistoryIdAndNode) {
        this.taskFromHistoryIdAndNode = taskFromHistoryIdAndNode;
    }

    public boolean isFolderPermissionsAreChecked() {
        return folderPermissionsAreChecked;
    }

    public void setFolderPermissionsAreChecked(boolean folderPermissionsAreChecked) {
        this.folderPermissionsAreChecked = folderPermissionsAreChecked;
    }

}
