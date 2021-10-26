package com.sos.js7.order.initiator.db;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.db.DBFilter;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.DailyPlanOrderStateText;

import js7.data.order.OrderId;

public class FilterDailyPlannedOrders extends DBFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterDailyPlannedOrders.class);
    private String dailyPlanDate;
    private Date orderPlannedStartFrom;
    private Date orderPlannedStartTo;
    private Date submitTime;

    private Date periodBegin;
    private Date periodEnd;
    private Long repeatInterval;

    private String orderId;
    private Boolean submitted;
    private Collection<String> listOfOrders;
    private List<String> listOfCyclicOrdersMainParts;

    private List<DailyPlanOrderStateText> states;
    private Set<Folder> setOfWorkflowFolders;
    private Set<Folder> setOfScheduleFolders;
    private Date plannedStart;
    private Boolean isLate;
    private String controllerId;
    private List<String> listOfWorkflowNames;
    private String workflowName;
    private List<Long> listOfSubmissionIds;

    private Long calendarId;

    private Long plannedOrderId;
    private List<String> listOfScheduleNames;
    private String scheduleName;
    private String orderName;
    private Integer startMode;

    public boolean isCyclicStart() {
        return startMode != null && startMode.equals(1);
    }

    public void setCyclicStart() {
        startMode = 1;
    }

    public void setSingleStart() {
        startMode = 0;
    }

    public Integer getStartMode() {
        return startMode;
    }

    public void setStartMode(Integer startMode) {
        this.startMode = startMode;
    }

    public Collection<String> getListOfOrders() {
        return listOfOrders;
    }

    public void setListOfOrders(Collection<String> listOfOrders) {
        this.listOfOrders = listOfOrders;
    }

    public List<String> getListOfCyclicOrdersMainParts() {
        return listOfCyclicOrdersMainParts;
    }

    public void setListOfCyclicOrdersMainParts(List<String> val) {
        listOfCyclicOrdersMainParts = val;
    }

    public void setDailyPlanDate(String dailyPlanDate, String timeZone, String periodBegin) {
        if (dailyPlanDate != null) {
            this.dailyPlanDate = dailyPlanDate;
            setOrderPlanDateInterval(timeZone, periodBegin);
        }
    }

    public void addScheduleName(String scheduleName) {
        if (listOfScheduleNames == null) {
            listOfScheduleNames = new ArrayList<String>();
        }
        listOfScheduleNames.add(scheduleName);
    }

    private void setOrderPlanDateInterval(String timeZone, String periodBegin) {
        String dateInString = String.format("%s %s", dailyPlanDate, periodBegin);

        Optional<Instant> oInstant = JobSchedulerDate.getScheduledForInUTC(dateInString, timeZone);
        if (!oInstant.isPresent()) {
            throw new JocMissingRequiredParameterException("wrong parameter (dailyPlanDate periodBegin -->" + periodBegin + " " + dateInString);
        }
        Instant instant = oInstant.get();
        orderPlannedStartFrom = Date.from(instant);

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(orderPlannedStartFrom);
        calendar.add(java.util.Calendar.HOUR, 24);
        orderPlannedStartTo = calendar.getTime();
    }

    public Date getOrderPlannedStartFrom() {
        return orderPlannedStartFrom;
    }

    public Date getOrderPlannedStartTo() {
        return orderPlannedStartTo;
    }

    public Set<Folder> getListOfWorkflowFolders() {
        return setOfWorkflowFolders;
    }

    public void setListOfWorkflowFolders(Set<Folder> listOfWorkflowFolders) {
        this.setOfWorkflowFolders = listOfWorkflowFolders;
    }

    public FilterDailyPlannedOrders() {
        super();
        this.setSortMode("DESC");
        this.setOrderCriteria("plannedStart");
    }

    public void addWorkflowFolders(Set<Folder> workflowFolders) {
        if (setOfWorkflowFolders == null) {
            setOfWorkflowFolders = new HashSet<Folder>();
        }
        if (workflowFolders != null) {
            setOfWorkflowFolders.addAll(workflowFolders);
        }
    }

    public void addWorkflowFolders(String workflowFolder, boolean recursive) {
        LOGGER.debug("Add workflowFolder: " + workflowFolder);
        if (setOfWorkflowFolders == null) {
            setOfWorkflowFolders = new HashSet<Folder>();
        }
        Folder filterFolder = new Folder();
        filterFolder.setFolder(workflowFolder);
        filterFolder.setRecursive(recursive);
        setOfWorkflowFolders.add(filterFolder);
    }

    public void addScheduleFolders(Set<Folder> scheduleFolders) {
        if (setOfScheduleFolders == null) {
            setOfScheduleFolders = new HashSet<Folder>();
        }
        if (setOfScheduleFolders != null) {
            setOfScheduleFolders.addAll(scheduleFolders);
        }
    }

    public void addScheduleFolders(String scheduleFolder, boolean recursive) {
        LOGGER.debug("Add scheduleFolder: " + scheduleFolder);
        if (setOfScheduleFolders == null) {
            setOfScheduleFolders = new HashSet<Folder>();
        }
        Folder filterFolder = new Folder();
        filterFolder.setFolder(scheduleFolder);
        filterFolder.setRecursive(recursive);
        setOfScheduleFolders.add(filterFolder);
    }

    public List<DailyPlanOrderStateText> getStates() {
        return states;
    }

    public Boolean isLate() {
        return isLate != null && isLate;
    }

    public Boolean getIsLate() {
        return isLate;
    }

    public void setLate(Boolean late) {
        this.isLate = late;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    public void addState(DailyPlanOrderStateText state) {
        if (states == null) {
            states = new ArrayList<DailyPlanOrderStateText>();
        }
        states.add(state);
    }

    public void setPlannedStart(Date plannedStart) {
        this.plannedStart = plannedStart;
    }

    public Date getPlannedStart() {
        return this.plannedStart;
    }

    public Long getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(Long calendarId) {
        this.calendarId = calendarId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Boolean getSubmitted() {
        return submitted;
    }

    public void setSubmitted(Boolean submitted) {
        this.submitted = submitted;
    }

    public void setSetOfOrders(Set<OrderId> setOfOrders) {
        if (this.listOfOrders == null) {
            this.listOfOrders = new ArrayList<String>();
        }
        for (OrderId orderId : setOfOrders) {
            this.listOfOrders.add(orderId.string());
        }
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }

    public void addWorkflowName(String workflowName) {
        if (this.listOfWorkflowNames == null) {
            this.listOfWorkflowNames = new ArrayList<String>();
        }
        this.listOfWorkflowNames.add(workflowName);
    }

    public void setStates(List<DailyPlanOrderStateText> states) {
        this.states = states;
    }

    public List<Long> getListOfSubmissionIds() {
        return listOfSubmissionIds;
    }

    public void setListOfSubmissionIds(List<Long> listOfSubmissionIds) {
        this.listOfSubmissionIds = listOfSubmissionIds;
    }

    public void addSubmissionHistoryId(Long submissionHistoryId) {
        if (this.listOfSubmissionIds == null) {
            this.listOfSubmissionIds = new ArrayList<Long>();
        }
        this.listOfSubmissionIds.add(submissionHistoryId);

    }

    public Long getPlannedOrderId() {
        return plannedOrderId;
    }

    public void setPlannedOrderId(Long plannedOrderId) {
        this.plannedOrderId = plannedOrderId;
    }

    public List<String> getListOfWorkflowNames() {
        return listOfWorkflowNames;
    }

    public void setListOfWorkflowNames(List<String> listOfWorkflowNames) {
        this.listOfWorkflowNames = listOfWorkflowNames;
    }

    public List<String> getListOfScheduleNames() {
        return listOfScheduleNames;
    }

    public void setListOfScheduleNames(List<String> listOfScheduleNames) {
        this.listOfScheduleNames = listOfScheduleNames;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public Date getPeriodBegin() {
        return periodBegin;
    }

    public void setPeriodBegin(Date periodBegin) {
        this.periodBegin = periodBegin;
    }

    public Date getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(Date periodEnd) {
        this.periodEnd = periodEnd;
    }

    public Long getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(Long repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public Set<Folder> getSetOfScheduleFolders() {
        return setOfScheduleFolders;
    }

    public void setSetOfScheduleFolders(Set<Folder> setOfScheduleFolders) {
        this.setOfScheduleFolders = setOfScheduleFolders;
    }

    public Set<Folder> getSetOfWorkflowFolders() {
        return setOfWorkflowFolders;
    }

    public void setSetOfWorkflowFolders(Set<Folder> setOfWorkflowFolders) {
        this.setOfWorkflowFolders = setOfWorkflowFolders;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

}
