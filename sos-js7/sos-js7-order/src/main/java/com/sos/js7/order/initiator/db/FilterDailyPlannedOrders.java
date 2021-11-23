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

    private Collection<String> listOfOrders;
    private Set<Folder> workflowFolders;
    private Set<Folder> scheduleFolders;
    private List<DailyPlanOrderStateText> states;
    private List<Long> submissionIds;
    private List<String> cyclicOrdersMainParts;
    private List<String> workflowNames;
    private List<String> scheduleNames;

    private Date plannedStart;
    private Date periodBegin;
    private Date periodEnd;
    private Date orderPlannedStartFrom;
    private Date orderPlannedStartTo;
    private Date submitTime;

    private Long repeatInterval;
    private Long calendarId;
    private Long plannedOrderId;

    private Integer startMode;

    private Boolean submitted;
    private Boolean isLate;

    private String controllerId;
    private String orderId;
    private String dailyPlanDate;
    private String workflowName;
    private String scheduleName;
    private String orderName;

    public FilterDailyPlannedOrders() {
        super();
        this.setSortMode("DESC");
        this.setOrderCriteria("plannedStart");
    }

    public FilterDailyPlannedOrders copy() {
        FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();

        filter.setListOfOrders(listOfOrders);
        filter.setWorkflowFolders(workflowFolders);
        filter.setScheduleFolders(scheduleFolders);
        filter.setStates(states);
        filter.setSubmissionIds(submissionIds);
        filter.setCyclicOrdersMainParts(cyclicOrdersMainParts);
        filter.setWorkflowNames(workflowNames);
        filter.setScheduleNames(scheduleNames);

        filter.setPlannedStart(plannedStart);
        filter.setPeriodBegin(periodBegin);
        filter.setPeriodEnd(periodEnd);
        filter.setOrderPlannedStartFrom(orderPlannedStartFrom);
        filter.setOrderPlannedStartTo(orderPlannedStartTo);
        filter.setSubmitTime(submitTime);

        filter.setRepeatInterval(repeatInterval);
        filter.setCalendarId(calendarId);
        filter.setPlannedOrderId(plannedOrderId);

        filter.setStartMode(startMode);

        filter.setSubmitted(submitted);
        filter.setLate(isLate);

        filter.setControllerId(controllerId);
        filter.setOrderId(orderId);
        filter.setDailyPlanDate(dailyPlanDate);
        filter.setWorkflowName(workflowName);
        filter.setScheduleName(scheduleName);
        filter.setOrderName(orderName);

        return filter;
    }

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

    public void setStartMode(Integer val) {
        startMode = val;
    }

    public Collection<String> getListOfOrders() {
        return listOfOrders;
    }

    public void setListOfOrders(Collection<String> val) {
        listOfOrders = val;
    }

    public List<String> getCyclicOrdersMainParts() {
        return cyclicOrdersMainParts;
    }

    public void setCyclicOrdersMainParts(List<String> val) {
        cyclicOrdersMainParts = val;
    }

    private void setDailyPlanDate(String val) {// for copy
        dailyPlanDate = val;
    }

    public void setDailyPlanDate(String dailyPlanDate, String timeZone, String periodBegin) {
        if (dailyPlanDate != null) {
            this.dailyPlanDate = dailyPlanDate;
            setOrderPlanDateInterval(timeZone, periodBegin);
        }
    }

    public void addScheduleName(String scheduleName) {
        if (scheduleNames == null) {
            scheduleNames = new ArrayList<String>();
        }
        scheduleNames.add(scheduleName);
    }

    private void setOrderPlannedStartFrom(Date val) {// for copy
        orderPlannedStartFrom = val;
    }

    public Date getOrderPlannedStartFrom() {
        return orderPlannedStartFrom;
    }

    private void setOrderPlannedStartTo(Date val) {// for copy
        orderPlannedStartTo = val;
    }

    public Date getOrderPlannedStartTo() {
        return orderPlannedStartTo;
    }

    public Set<Folder> getWorkflowFolders() {
        return workflowFolders;
    }

    public void setWorkflowFolders(Set<Folder> val) {
        workflowFolders = val;
    }

    public void addWorkflowFolders(Set<Folder> val) {
        if (workflowFolders == null) {
            workflowFolders = new HashSet<Folder>();
        }
        if (val != null) {
            workflowFolders.addAll(val);
        }
    }

    public void addWorkflowFolders(String workflowFolder, boolean recursive) {
        LOGGER.debug("Add workflowFolder: " + workflowFolder);
        if (workflowFolders == null) {
            workflowFolders = new HashSet<Folder>();
        }
        Folder filterFolder = new Folder();
        filterFolder.setFolder(workflowFolder);
        filterFolder.setRecursive(recursive);
        workflowFolders.add(filterFolder);
    }

    public Set<Folder> getScheduleFolders() {
        return scheduleFolders;
    }

    public void setScheduleFolders(Set<Folder> val) {
        scheduleFolders = val;
    }

    public void addScheduleFolders(Set<Folder> val) {
        if (scheduleFolders == null) {
            scheduleFolders = new HashSet<Folder>();
        }
        if (val != null) {
            scheduleFolders.addAll(val);
        }
    }

    public void addScheduleFolders(String scheduleFolder, boolean recursive) {
        LOGGER.debug("Add scheduleFolder: " + scheduleFolder);
        if (scheduleFolders == null) {
            scheduleFolders = new HashSet<Folder>();
        }
        Folder filterFolder = new Folder();
        filterFolder.setFolder(scheduleFolder);
        filterFolder.setRecursive(recursive);
        scheduleFolders.add(filterFolder);
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

    public void setLate(Boolean val) {
        isLate = val;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String val) {
        controllerId = val;
    }

    public void addState(DailyPlanOrderStateText state) {
        if (states == null) {
            states = new ArrayList<DailyPlanOrderStateText>();
        }
        states.add(state);
    }

    public void setPlannedStart(Date val) {
        plannedStart = val;
    }

    public Date getPlannedStart() {
        return this.plannedStart;
    }

    public Long getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(Long val) {
        calendarId = val;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String val) {
        orderId = val;
    }

    public Boolean getSubmitted() {
        return submitted;
    }

    public void setSubmitted(Boolean val) {
        submitted = val;
    }

    public void setSetOfOrders(Set<OrderId> val) {
        if (listOfOrders == null) {
            listOfOrders = new ArrayList<String>();
        }
        for (OrderId orderId : val) {
            listOfOrders.add(orderId.string());
        }
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date val) {
        submitTime = val;
    }

    public void addWorkflowName(String workflowName) {
        if (workflowNames == null) {
            workflowNames = new ArrayList<String>();
        }
        workflowNames.add(workflowName);
    }

    public void setStates(List<DailyPlanOrderStateText> val) {
        states = val;
    }

    public List<Long> getSubmissionIds() {
        return submissionIds;
    }

    public void setSubmissionIds(List<Long> val) {
        submissionIds = val;
    }

    public void addSubmissionHistoryId(Long val) {
        if (submissionIds == null) {
            submissionIds = new ArrayList<Long>();
        }
        submissionIds.add(val);
    }

    public Long getPlannedOrderId() {
        return plannedOrderId;
    }

    public void setPlannedOrderId(Long val) {
        plannedOrderId = val;
    }

    public List<String> getWorkflowNames() {
        return workflowNames;
    }

    public void setWorkflowNames(List<String> val) {
        workflowNames = val;
    }

    public List<String> getScheduleNames() {
        return scheduleNames;
    }

    public void setScheduleNames(List<String> val) {
        scheduleNames = val;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String val) {
        workflowName = val;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String val) {
        scheduleName = val;
    }

    public Date getPeriodBegin() {
        return periodBegin;
    }

    public void setPeriodBegin(Date val) {
        periodBegin = val;
    }

    public Date getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(Date val) {
        periodEnd = val;
    }

    public Long getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(Long val) {
        repeatInterval = val;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String val) {
        orderName = val;
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
}
