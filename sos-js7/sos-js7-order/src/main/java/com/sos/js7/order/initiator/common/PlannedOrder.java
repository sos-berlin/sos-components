package com.sos.js7.order.initiator.common;

import com.sos.controller.model.order.FreshOrder;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;

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
    private String orderName;

    public PlannedOrder() {
    }

    public PlannedOrder(DBItemDailyPlanOrder item) {
        freshOrder = new FreshOrder();
        freshOrder.setId(item.getOrderId());
        freshOrder.setScheduledFor(item.getPlannedStart().getTime());
        freshOrder.setWorkflowPath(item.getWorkflowPath());

        schedule = new Schedule();
        schedule.setWorkflowPath((item.getWorkflowPath()));
        schedule.setPath(item.getSchedulePath());
        schedule.setWorkflowName(item.getWorkflowName());
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

    public void setFreshOrder(FreshOrder val) {
        freshOrder = val;
    }

    public Long getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(Long val) {
        calendarId = val;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule val) {
        schedule = val;
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

    public void setControllerId(String val) {
        controllerId = val;
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
