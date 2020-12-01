package com.sos.js7.order.initiator.classes;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import com.sos.jobscheduler.model.order.FreshOrder;
import com.sos.joc.db.orders.DBItemDailyPlanOrders;
import com.sos.joc.model.calendar.Period;
import com.sos.webservices.order.initiator.model.Schedule;
 
public class PlannedOrder{

   // private static final Logger LOGGER = LoggerFactory.getLogger(PlannedOrder.class);
    private FreshOrder freshOrder;
    private Long calendarId;
    private Long submissionHistoryId;
    private Period period;
    private Long averageDuration = 0L;
    private boolean storedInDb = false;
    private Schedule schedule;

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
        freshOrder.setId(dbItemDailyPlannedOrders.getOrderKey());
        freshOrder.setScheduledFor(dbItemDailyPlannedOrders.getPlannedStart().getTime());
        freshOrder.setWorkflowPath(dbItemDailyPlannedOrders.getWorkflow());
        this.schedule = new Schedule();
        schedule.setControllerId(dbItemDailyPlannedOrders.getControllerId());
        schedule.setWorkflowPath((dbItemDailyPlannedOrders.getWorkflow()));
        schedule.setPath(dbItemDailyPlannedOrders.getSchedulePath());
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
        plannedOrderKey.setJobschedulerId(schedule.getControllerId());
        plannedOrderKey.setOrderId(freshOrder.getId());
        plannedOrderKey.setWorkflowPath(freshOrder.getWorkflowPath());
        return plannedOrderKey;
    }

}
