package com.sos.joc.dailyplan.db;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.db.DBFilter;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.DailyPlanOrderStateText;

import js7.data.order.OrderId;

public class FilterDailyPlannedOrders extends DBFilter {

    private Collection<String> orderIds;
    private Collection<Folder> workflowFolders;
    private Collection<Folder> scheduleFolders;
    private List<DailyPlanOrderStateText> states;
    private List<Long> submissionIds;
    private List<String> cyclicOrdersMainParts;
    private List<String> workflowNames;
    private List<String> scheduleNames;
    private List<String> controllerIds;

    private Date plannedStart;
    private Date periodBegin;
    private Date periodEnd;
    private Date orderPlannedStartFrom;
    private Date orderPlannedStartTo;
    private Date submitTime;
    private Date submissionForDate;
    private Date submissionForDateFrom;
    private Date submissionForDateTo;

    private Long repeatInterval;
    private Long calendarId;
    private Long plannedOrderId;

    private Integer startMode;

    private Boolean submitted;
    private Boolean isLate;

    private String controllerId;
    private String orderId;
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

        filter.setOrderIds(orderIds);
        filter.setWorkflowFolders(workflowFolders);
        filter.setScheduleFolders(scheduleFolders);
        filter.setStates(states);
        filter.setSubmissionIds(submissionIds);
        filter.setCyclicOrdersMainParts(cyclicOrdersMainParts);
        filter.setWorkflowNames(workflowNames);
        filter.setScheduleNames(scheduleNames);
        filter.setControllerIds(controllerIds);

        filter.setPlannedStart(plannedStart);
        filter.setPeriodBegin(periodBegin);
        filter.setPeriodEnd(periodEnd);
        filter.setOrderPlannedStartFrom(orderPlannedStartFrom);
        filter.setOrderPlannedStartTo(orderPlannedStartTo);
        filter.setSubmitTime(submitTime);
        filter.setSubmissionForDate(submissionForDate);
        filter.setSubmissionForDateFrom(submissionForDateFrom);
        filter.setSubmissionForDateTo(submissionForDateTo);

        filter.setRepeatInterval(repeatInterval);
        filter.setCalendarId(calendarId);
        filter.setPlannedOrderId(plannedOrderId);

        filter.setStartMode(startMode);

        filter.setSubmitted(submitted);
        filter.setLate(isLate);

        filter.setControllerId(controllerId);
        filter.setOrderId(orderId);
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

    public Set<String> getOrderIds() {
        if (orderIds != null) {
            return orderIds.stream().collect(Collectors.toSet());
        }
        return null;
    }

    public void setOrderIds(Collection<String> val) {
        orderIds = val;
    }

    public void setOrderIds(Set<OrderId> val) {
        if (orderIds == null) {
            orderIds = new ArrayList<String>();
        }
        for (OrderId orderId : val) {
            orderIds.add(orderId.string());
        }
    }

    public List<String> getCyclicOrdersMainParts() {
        return cyclicOrdersMainParts;
    }

    public void setCyclicOrdersMainParts(List<String> val) {
        cyclicOrdersMainParts = val;
    }
    
    public void setDailyPlanDate(String dailyPlanDate, String timeZone, String periodBegin) {
        if (dailyPlanDate != null) {
            setDailyPlanInterval(dailyPlanDate, dailyPlanDate, timeZone, periodBegin);
        }
    }
    
    public void setOrderPlannedStartFrom(Date val) {
        orderPlannedStartFrom = val;
    }

    public Date getOrderPlannedStartFrom() {
        return orderPlannedStartFrom;
    }

    public void setOrderPlannedStartTo(Date val) {
        orderPlannedStartTo = val;
    }

    public Date getOrderPlannedStartTo() {
        return orderPlannedStartTo;
    }

    public Collection<Folder> getWorkflowFolders() {
        return workflowFolders;
    }

    public void setWorkflowFolders(Collection<Folder> val) {
        workflowFolders = val;
    }

    public void addWorkflowFolders(Collection<Folder> val) {
        if (workflowFolders == null) {
            workflowFolders = new HashSet<Folder>();
        }
        if (val != null) {
            workflowFolders.addAll(val);
        }
    }

    public Collection<Folder> getScheduleFolders() {
        return scheduleFolders;
    }

    public void setScheduleFolders(Collection<Folder> val) {
        scheduleFolders = val;
    }

    public void addScheduleFolders(Collection<Folder> val) {
        if (scheduleFolders == null) {
            scheduleFolders = new HashSet<Folder>();
        }
        if (val != null) {
            scheduleFolders.addAll(val);
        }
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
        return plannedStart;
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

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date val) {
        submitTime = val;
    }

    public Date getSubmissionForDate() {
        return submissionForDate;
    }

    public void setSubmissionForDate(Date val) {
        submissionForDate = val;
    }

    public Date getSubmissionForDateFrom() {
        return submissionForDateFrom;
    }

    public void setSubmissionForDateFrom(Date val) {
        submissionForDateFrom = val;
    }

    public Date getSubmissionForDateTo() {
        return submissionForDateTo;
    }

    public void setSubmissionForDateTo(Date val) {
        submissionForDateTo = val;
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
    
    public List<String> getControllerIds() {
        return controllerIds;
    }

    public void setControllerIds(List<String> val) {
        controllerIds = val;
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

    public void setDailyPlanInterval(String dailyPlanDateFrom, String dailyPlanDateTo, String timeZone, String periodBegin) {
        if (dailyPlanDateFrom != null) {
            String dateInStringFrom = String.format("%s %s", dailyPlanDateFrom, periodBegin);

            Optional<Instant> oInstantFrom = JobSchedulerDate.getScheduledForInUTC(dateInStringFrom, timeZone);
            if (!oInstantFrom.isPresent()) {
                throw new JocMissingRequiredParameterException("wrong parameter (dailyPlanDateFrom periodBegin -->" + periodBegin + " "
                        + dateInStringFrom);
            }
            orderPlannedStartFrom = Date.from(oInstantFrom.get());

            if (dailyPlanDateTo != null) {
                String dateInStringTo = String.format("%s %s", dailyPlanDateTo, periodBegin);
                Optional<Instant> oInstantTo = JobSchedulerDate.getScheduledForInUTC(dateInStringTo, timeZone);
                if (!oInstantTo.isPresent()) {
                    throw new JocMissingRequiredParameterException("wrong parameter (dailyPlanDateTo periodBegin -->" + periodBegin + " "
                            + dateInStringTo);
                }
                java.util.Calendar calendar = java.util.Calendar.getInstance();
                calendar.setTime(Date.from(oInstantTo.get()));
                calendar.add(java.util.Calendar.HOUR, 24);
                orderPlannedStartTo = calendar.getTime();
            }
        }
    }
}
