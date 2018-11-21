package com.sos.webservices.order.initiator.classes;

import java.text.ParseException;
import java.util.Date;
import java.util.Map.Entry;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.db.orders.DBItemDailyPlan;
import com.sos.jobscheduler.db.orders.DBItemDailyPlanVariables;
import com.sos.jobscheduler.model.order.FreshOrder;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.webservices.order.initiator.db.DBLayerDailyPlan;
import com.sos.webservices.order.initiator.model.OrderTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlannedOrder {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlannedOrder.class);
    private FreshOrder freshOrder;
    private Long calendarId;
    private com.sos.webservices.order.initiator.model.Period period;
    OrderTemplate orderTemplate;

    public FreshOrder getFreshOrder() {
        return freshOrder;
    }

    public com.sos.webservices.order.initiator.model.Period getPeriod() {
        return period;
    }

    public void setPeriod(com.sos.webservices.order.initiator.model.Period period) {
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

    public boolean orderExist() throws JocConfigurationException, DBConnectionRefusedException, SOSHibernateException {
        SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("OrderInitiatorRunner");
        try {
            DBLayerDailyPlan dbLayerDailyPlan = new DBLayerDailyPlan(sosHibernateSession);
            Globals.beginTransaction(sosHibernateSession);
            DBItemDailyPlan dbItemDailyPlan = new DBItemDailyPlan();
            dbItemDailyPlan.setOrderKey(freshOrder.getId());
            LOGGER.info("----> " + freshOrder.getScheduledAt() + ":" + new Date(freshOrder.getScheduledAt()));
            dbItemDailyPlan.setMasterId(orderTemplate.getMasterId());
            dbItemDailyPlan.setWorkflow(freshOrder.getWorkflowPath());
            return dbLayerDailyPlan.getUniqueDailyPlan(dbItemDailyPlan) != null;
        } finally {
            Globals.commit(sosHibernateSession);
            Globals.disconnect(sosHibernateSession);
        }
    }

    public void store() throws JocConfigurationException, DBConnectionRefusedException, SOSHibernateException, ParseException {
        SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("OrderInitiatorRunner");
        try {
            Globals.beginTransaction(sosHibernateSession);
            DBItemDailyPlan dbItemDailyPlan = new DBItemDailyPlan();
            dbItemDailyPlan.setOrderName(orderTemplate.getOrderName());
            dbItemDailyPlan.setOrderKey(freshOrder.getId());
            dbItemDailyPlan.setPlannedStart(new Date(freshOrder.getScheduledAt()));
            if (this.getPeriod() != null) {
                dbItemDailyPlan.setPeriodBegin(this.getPeriod().getBegin());
                dbItemDailyPlan.setPeriodEnd(this.getPeriod().getEnd());
                dbItemDailyPlan.setRepeatInterval(this.getPeriod().getRepeat());
            }
            dbItemDailyPlan.setMasterId(orderTemplate.getMasterId());
            dbItemDailyPlan.setWorkflow(freshOrder.getWorkflowPath());
            dbItemDailyPlan.setCalendarId(calendarId);
            dbItemDailyPlan.setCreated(new Date());
            dbItemDailyPlan.setExpectedEnd(new Date());
            dbItemDailyPlan.setModified(new Date());
            sosHibernateSession.save(dbItemDailyPlan);
            DBItemDailyPlanVariables dbItemDailyPlanVariables = new DBItemDailyPlanVariables();
            for (Entry<String, String> variable : freshOrder.getVariables().getAdditionalProperties().entrySet()) {
                dbItemDailyPlanVariables.setCreated(new Date());
                dbItemDailyPlanVariables.setModified(new Date());
                dbItemDailyPlanVariables.setPlanId(dbItemDailyPlan.getId());
                dbItemDailyPlanVariables.setVariableName(variable.getKey());
                dbItemDailyPlanVariables.setVariableValue(variable.getValue());
                sosHibernateSession.save(dbItemDailyPlanVariables);
            }
        } finally {
            Globals.commit(sosHibernateSession);
            Globals.disconnect(sosHibernateSession);
        }
    }
}
