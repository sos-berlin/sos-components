package com.sos.joc.dailyplan.common;

import java.nio.file.Paths;

import com.sos.controller.model.order.FreshOrder;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.schedule.Schedule;

public class PlannedOrder {

    private final FreshOrder freshOrder;
    private final Schedule schedule;
    private final String controllerId;
    private final Long calendarId;

    private final String workflowName;
    private final String workflowPath;
    private final String scheduleName;
    private final String schedulePath;

    private Long submissionHistoryId;
    private Period period;
    private Long averageDuration = 0L;
    private boolean storedInDb = false;
    private String orderName;

    public PlannedOrder(String controllerId, FreshOrder freshOrder, Schedule schedule, Long calendarId) {
        this.controllerId = controllerId;
        this.freshOrder = freshOrder;
        this.schedule = schedule;
        this.calendarId = calendarId;

        this.workflowName = schedule.getWorkflowName();
        this.workflowPath = schedule.getWorkflowPath();
        this.scheduleName = Paths.get(schedule.getPath()).getFileName().toString();
        this.schedulePath = schedule.getPath();
    }

    public PlannedOrderKey uniqueOrderKey() {
        return new PlannedOrderKey(controllerId, workflowName, scheduleName, freshOrder.getId());
    }

    public FreshOrder getFreshOrder() {
        return freshOrder;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period val) {
        period = val;
    }

    public Long getCalendarId() {
        return calendarId;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setAverageDuration(Long val) {
        averageDuration = val;
    }

    public Long getAverageDuration() {
        return averageDuration;
    }

    public void setSubmissionHistoryId(Long val) {
        submissionHistoryId = val;
    }

    public Long getSubmissionHistoryId() {
        return submissionHistoryId;

    }

    public String getControllerId() {
        return controllerId;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public String getWorkflowPath() {
        return workflowPath;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public String getSchedulePath() {
        return schedulePath;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String val) {
        if (val.length() > 30) {
            orderName = val.substring(0, 30);
        } else {
            orderName = val;
        }
    }

    public boolean isStoredInDb() {
        return storedInDb;
    }

    public void setStoredInDb(boolean val) {
        storedInDb = val;
    }
}
