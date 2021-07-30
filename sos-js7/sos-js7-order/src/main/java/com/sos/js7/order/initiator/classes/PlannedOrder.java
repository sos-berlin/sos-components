package com.sos.js7.order.initiator.classes;

import com.sos.controller.model.order.FreshOrder;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.calendar.Period;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

import com.sos.joc.db.orders.DBItemDailyPlanOrders;

public class PlannedOrder {

    // private static final Logger LOGGER = LoggerFactory.getLogger(PlannedOrder.class);
    private FreshOrder freshOrder;
    private String controllerId;
    private Long calendarId;
    private Long submissionHistoryId;
    private Period period;
    private Long averageDuration = 0L;
    private boolean storedInDb = false;
    private Schedule schedule;
    private String workflowPath;

    public boolean isStoredInDb() {
        return storedInDb;
    }

    public void setStoredInDb(boolean storedInDb) {
        this.storedInDb = storedInDb;
    }

    public PlannedOrder() {
    }

    public PlannedOrder(DBItemDailyPlanOrders dbItemDailyPlannedOrders) {
        this.freshOrder = new FreshOrder();
        freshOrder.setId(dbItemDailyPlannedOrders.getOrderId());
        freshOrder.setScheduledFor(dbItemDailyPlannedOrders.getPlannedStart().getTime());
        freshOrder.setWorkflowPath(dbItemDailyPlannedOrders.getWorkflowPath());
        this.schedule = new Schedule();
        schedule.setWorkflowPath((dbItemDailyPlannedOrders.getWorkflowPath()));
        schedule.setPath(dbItemDailyPlannedOrders.getSchedulePath());
        schedule.setWorkflowName(dbItemDailyPlannedOrders.getWorkflowName());

    }

    public FreshOrder getFreshOrder() {
        return freshOrder;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public void setFreshOrder(FreshOrder freshOrder) {
        this.freshOrder = freshOrder;
    }

    public Long getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(Long calendarId) {
        this.calendarId = calendarId;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public void setAverageDuration(Long averageDuration) {
        this.averageDuration = averageDuration;
    }

    public Long getAverageDuration() {
        return averageDuration;
    }

    public void setSubmissionHistoryId(Long planId) {
        this.submissionHistoryId = planId;
    }

    public Long getSubmissionHistoryId() {
        return submissionHistoryId;

    }

    public PlannedOrderKey uniqueOrderkey() {
        PlannedOrderKey plannedOrderKey = new PlannedOrderKey();
        plannedOrderKey.setControllerId(this.getControllerId());
        plannedOrderKey.setOrderId(freshOrder.getId());
        plannedOrderKey.setWorkflowName(freshOrder.getWorkflowPath());
        return plannedOrderKey;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    
    public String getWorkflowPath() {
        return workflowPath;
    }

    
    public void setWorkflowPath(String workflowPath) {
        this.workflowPath = workflowPath;
    }

 

}
