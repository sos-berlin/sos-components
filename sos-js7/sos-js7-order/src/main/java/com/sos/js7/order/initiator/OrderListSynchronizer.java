package com.sos.js7.order.initiator;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSDuration;
import com.sos.commons.util.SOSDurations;
import com.sos.joc.Globals;
import com.sos.joc.classes.OrderHelper;
import com.sos.joc.db.orders.DBItemDailyPlanOrders;
import com.sos.joc.db.orders.DBItemDailyPlanWithHistory;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JobSchedulerObjectNotExistException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.js7.order.initiator.classes.OrderApi;
import com.sos.js7.order.initiator.classes.OrderInitiatorGlobals;
import com.sos.js7.order.initiator.classes.PlannedOrder;
import com.sos.js7.order.initiator.classes.PlannedOrderKey;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;

import js7.data.order.OrderId;

public class OrderListSynchronizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderListSynchronizer.class);
    private Map<PlannedOrderKey, PlannedOrder> listOfPlannedOrders;
    private Map<PlannedOrderKey, Long> listOfDurations;

    public OrderListSynchronizer() {
        listOfPlannedOrders = new HashMap<PlannedOrderKey, PlannedOrder>();
    }

    public OrderListSynchronizer(OrderInitiatorSettings orderInitiatorSettings) {
        listOfPlannedOrders = new HashMap<PlannedOrderKey, PlannedOrder>();
        OrderInitiatorGlobals.orderInitiatorSettings = orderInitiatorSettings;
    }

    public void add(PlannedOrder o) {
        listOfPlannedOrders.put(o.uniqueOrderkey(), o);
    }

    private void calculateDuration(PlannedOrder plannedOrder) throws SOSHibernateException, JocConfigurationException, DBConnectionRefusedException,
            DBOpenSessionException {

        if (listOfDurations.get(plannedOrder.uniqueOrderkey()) == null) {

            SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("calculateDurations");
            try {

                FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
                DBLayerDailyPlannedOrders dbLayerDailyPlan = new DBLayerDailyPlannedOrders(sosHibernateSession);
                Globals.beginTransaction(sosHibernateSession);
                filter.setControllerId(plannedOrder.getControllerId());
                filter.addWorkflowPath(plannedOrder.getSchedule().getWorkflowPath());
                List<DBItemDailyPlanWithHistory> listOfPlannedOrders = dbLayerDailyPlan.getDailyPlanWithHistoryList(filter, 0);
                SOSDurations sosDurations = new SOSDurations();
                for (DBItemDailyPlanWithHistory dbItemDailyPlanWithHistory : listOfPlannedOrders) {
                    if (dbItemDailyPlanWithHistory.getOrderHistoryId() != null) {
                        SOSDuration sosDuration = new SOSDuration();
                        Date startTime = dbItemDailyPlanWithHistory.getStartTime();
                        Date endTime = dbItemDailyPlanWithHistory.getEndTime();
                        sosDuration.setStartTime(startTime);
                        sosDuration.setEndTime(endTime);
                        sosDurations.add(sosDuration);
                    }
                }
                listOfDurations.put(plannedOrder.uniqueOrderkey(), sosDurations.average());
            } finally {
                Globals.disconnect(sosHibernateSession);
            }
        }
    }

    private void calculateDurations() throws SOSHibernateException, JocConfigurationException, DBConnectionRefusedException, DBOpenSessionException {
        LOGGER.debug("... calculateDurations");
        listOfDurations = new HashMap<PlannedOrderKey, Long>();

        for (PlannedOrder plannedOrder : listOfPlannedOrders.values()) {
            calculateDuration(plannedOrder);
        }
    }

    public void submitOrdersToController() throws JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException,
            DBConnectionRefusedException, InterruptedException, ExecutionException, SOSHibernateException, TimeoutException {
     
        LOGGER.debug(listOfPlannedOrders.size() + " orders will be submitted to the controller");
        
        Set<PlannedOrder> addedOrders = new HashSet<PlannedOrder>();
        for (PlannedOrder p : listOfPlannedOrders.values()) {
            if (p.isStoredInDb() && p.getSchedule().getSubmitOrderToControllerWhenPlanned()) {
                addedOrders.add(p);
            }
        }
        
        SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("submitOrdersToController");
    
        sosHibernateSession.setAutoCommit(false);
        Globals.beginTransaction(sosHibernateSession);

        DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
        OrderApi.addOrderToController(addedOrders);
        Globals.beginTransaction(sosHibernateSession);
        try {

            Set<OrderId> setOfOrderIds = OrderApi.getNotMarkWithRemoveOrdersWhenTerminated();

            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
            filter.setControllerId(OrderInitiatorGlobals.orderInitiatorSettings.getControllerId());
            filter.setSetOfPlannedOrder(addedOrders);
            filter.setSubmitted(true);
            dbLayerDailyPlannedOrders.setSubmitted(filter);
            OrderApi.setRemoveOrdersWhenTerminated(setOfOrderIds);
            Globals.commit(sosHibernateSession);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

    public void addPlannedOrderToDB() throws JocConfigurationException, DBConnectionRefusedException, SOSHibernateException, ParseException,
            JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException, DBMissingDataException, DBOpenSessionException,
            DBInvalidDataException, JsonProcessingException, InterruptedException, ExecutionException {
        LOGGER.debug("... addPlannedOrderToDB");

        calculateDurations();
        SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("addPlannedOrderToDB");
        DBLayerDailyPlannedOrders dbLayerDailyPlan = new DBLayerDailyPlannedOrders(sosHibernateSession);
        sosHibernateSession.setAutoCommit(false);

        Globals.beginTransaction(sosHibernateSession);
        try {

            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
            if (OrderInitiatorGlobals.orderInitiatorSettings.isOverwrite()) {
                for (PlannedOrder plannedOrder : listOfPlannedOrders.values()) {
                    FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
                    filter.setPlannedStart(new Date(plannedOrder.getFreshOrder().getScheduledFor()));
                    LOGGER.debug("----> " + plannedOrder.getFreshOrder().getScheduledFor() + ":" + new Date(plannedOrder.getFreshOrder()
                            .getScheduledFor()));
                    filter.setControllerId(OrderInitiatorGlobals.orderInitiatorSettings.getControllerId());
                    filter.addWorkflowPath(plannedOrder.getFreshOrder().getWorkflowPath());
                    List<DBItemDailyPlanOrders> listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filter, 0);
                    try {
                        OrderHelper.removeFromJobSchedulerController(plannedOrder.getControllerId(), listOfPlannedOrders);
                    } catch (JobSchedulerObjectNotExistException e) {
                        LOGGER.warn("Order unknown in JS7 Controller");
                    }
                    dbLayerDailyPlannedOrders.deleteCascading(filter);
                }
            }
            for (PlannedOrder plannedOrder : listOfPlannedOrders.values()) {
                DBItemDailyPlanOrders dbItemDailyPlan = null;
                dbItemDailyPlan = dbLayerDailyPlan.getUniqueDailyPlan(plannedOrder);

                if (OrderInitiatorGlobals.orderInitiatorSettings.isOverwrite() || dbItemDailyPlan == null) {
                    LOGGER.debug("snchronizer: adding planned order to database: " + plannedOrder.uniqueOrderkey());
                    plannedOrder.setAverageDuration(listOfDurations.get(plannedOrder.uniqueOrderkey()));
                    dbLayerDailyPlan.store(plannedOrder);
                    plannedOrder.setStoredInDb(true);
                }
            }

            Globals.commit(sosHibernateSession);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

    public void addPlannedOrderToControllerAndDB(Boolean withSubmit) throws JocConfigurationException, DBConnectionRefusedException,
            JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException, DBMissingDataException, DBOpenSessionException,
            DBInvalidDataException, SOSHibernateException, JsonProcessingException, ParseException, InterruptedException, ExecutionException,
            TimeoutException {
        LOGGER.debug("... addPlannedOrderToControllerAndDB");

        addPlannedOrderToDB();

        if (withSubmit == null || withSubmit) {
            submitOrdersToController();
        } else {
            LOGGER.debug("Orders will not be submitted to the controller");
        }
    }

    public Map<PlannedOrderKey, PlannedOrder> getListOfPlannedOrders() {
        return listOfPlannedOrders;
    }
}
