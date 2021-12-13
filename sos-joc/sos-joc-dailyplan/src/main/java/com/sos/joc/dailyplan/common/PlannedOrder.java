package com.sos.joc.dailyplan.common;

import com.sos.controller.model.order.FreshOrder;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.schedule.Schedule;

public class PlannedOrder {

    private final FreshOrder freshOrder;
    private final Schedule schedule;
    private final String controllerId;
    private final Long calendarId;

    private Long submissionHistoryId;
    private Period period;
    private Long averageDuration = 0L;
    private boolean storedInDb = false;
    private String workflowPath;
    private String orderName;

    public PlannedOrder(String controllerId, FreshOrder freshOrder, Schedule schedule, Long calendarId) {
        this.controllerId = controllerId;
        this.freshOrder = freshOrder;
        this.schedule = schedule;
        this.calendarId = calendarId;
    }

    public PlannedOrderKey uniqueOrderkey() {
        PlannedOrderKey key = new PlannedOrderKey();
        key.setControllerId(this.getControllerId());
        key.setOrderId(freshOrder.getId());
        key.setWorkflowName(freshOrder.getWorkflowPath());
        return key;
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

    public String getWorkflowPath() {
        return workflowPath;
    }

    public void setWorkflowPath(String val) {
        workflowPath = val;
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
