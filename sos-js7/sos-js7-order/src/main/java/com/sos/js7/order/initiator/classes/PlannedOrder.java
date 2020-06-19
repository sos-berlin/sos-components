package com.sos.js7.order.initiator.classes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.db.orders.DBItemDailyPlannedOrders;
import com.sos.jobscheduler.model.order.FreshOrder;
import com.sos.js7.order.initiator.model.OrderTemplate;

public class PlannedOrder {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlannedOrder.class);
    private FreshOrder freshOrder;
    private Long calendarId;
    private Long planId;
    private com.sos.js7.order.initiator.model.Period period;
    private Long averageDuration = 0L;

    OrderTemplate orderTemplate;

    public PlannedOrder(DBItemDailyPlannedOrders dbItemDailyPlannedOrders) {
       this.freshOrder = new FreshOrder();
       freshOrder.setId(dbItemDailyPlannedOrders.getOrderKey());
       freshOrder.setScheduledFor(dbItemDailyPlannedOrders.getPlannedStart().getTime());
       freshOrder.setWorkflowPath(dbItemDailyPlannedOrders.getWorkflow());
       this.orderTemplate = new OrderTemplate();
       orderTemplate.setJobschedulerId(dbItemDailyPlannedOrders.getJobschedulerId());
       orderTemplate.setWorkflowPath((dbItemDailyPlannedOrders.getWorkflow()));
       //orderTemplate.setTemplateId(dbItemDailyPlan.getOrderTemplateId());
       orderTemplate.setOrderTemplateName(dbItemDailyPlannedOrders.getOrderTemplateName());
    }

    public PlannedOrder() {
     }

    public FreshOrder getFreshOrder() {
        return freshOrder;
    }

    public com.sos.js7.order.initiator.model.Period getPeriod() {
        return period;
    }

    public void setPeriod(com.sos.js7.order.initiator.model.Period period) {
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

    public OrderTemplate getOrderTemplate() {
        return orderTemplate;
    }

    public void setOrderTemplate(OrderTemplate orderTemplate) {
        this.orderTemplate = orderTemplate;
    }

        public void setAverageDuration(Long averageDuration) {
        this.averageDuration = averageDuration;
    }

    public Long getAverageDuration() {
        return averageDuration;
    }
 
    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public Long getPlanId() {
        return planId;

    }

    public PlannedOrderKey uniqueOrderkey() {
        PlannedOrderKey plannedOrderKey = new PlannedOrderKey();
        plannedOrderKey.setJobschedulerId(orderTemplate.getJobschedulerId());
        plannedOrderKey.setOrderId(freshOrder.getId());
        plannedOrderKey.setWorkflowPath(freshOrder.getWorkflowPath());
        return plannedOrderKey;
    }

   
}
