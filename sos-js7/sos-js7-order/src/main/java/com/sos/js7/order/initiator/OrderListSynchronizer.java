package com.sos.js7.order.initiator;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.exception.SOSException;
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
                filter.setControllerId(plannedOrder.getOrderTemplate().getControllerId());
                filter.setWorkflow(plannedOrder.getOrderTemplate().getWorkflowPath());
                List<DBItemDailyPlanWithHistory> listOfPlannedOrders = dbLayerDailyPlan.getDailyPlanWithHistoryList(filter, 0);
                SOSDurations sosDurations = new SOSDurations();
                for (DBItemDailyPlanWithHistory dbItemDailyPlanWithHistory : listOfPlannedOrders) {
                    if (dbItemDailyPlanWithHistory.getDbItemOrder() != null) {
                        SOSDuration sosDuration = new SOSDuration();
                        Date startTime = dbItemDailyPlanWithHistory.getDbItemOrder().getStartTime();
                        Date endTime = dbItemDailyPlanWithHistory.getDbItemOrder().getEndTime();
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

    public void addPlannedOrderToControllerAndDB() throws JsonProcessingException, SOSException, URISyntaxException, JocConfigurationException,
            DBConnectionRefusedException, ParseException, DBOpenSessionException, JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, DBMissingDataException, DBInvalidDataException, InterruptedException, ExecutionException,
            TimeoutException {
        LOGGER.debug("... addPlannedOrderToControllerAndDB");

        calculateDurations();
        SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("synchronizePlannedOrderWithDB");
        DBLayerDailyPlannedOrders dbLayerDailyPlan = new DBLayerDailyPlannedOrders(sosHibernateSession);
        sosHibernateSession.setAutoCommit(false);

        Globals.beginTransaction(sosHibernateSession);
        try {

            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
            for (PlannedOrder plannedOrder : listOfPlannedOrders.values()) {
                DBItemDailyPlanOrders dbItemDailyPlan = null;
                if (OrderInitiatorGlobals.orderInitiatorSettings.isOverwrite()) {

                    FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
                    filter.setPlannedStart(new Date(plannedOrder.getFreshOrder().getScheduledFor()));
                    LOGGER.info("----> " + plannedOrder.getFreshOrder().getScheduledFor() + ":" + new Date(plannedOrder.getFreshOrder()
                            .getScheduledFor()));
                    filter.setControllerId(OrderInitiatorGlobals.orderInitiatorSettings.getControllerId());
                    filter.setWorkflow(plannedOrder.getFreshOrder().getWorkflowPath());
                    List<DBItemDailyPlanOrders> listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filter, 0);
                    OrderHelper.removeFromJobSchedulerController(plannedOrder.getOrderTemplate().getControllerId(), listOfPlannedOrders);
                    dbLayerDailyPlannedOrders.delete(filter);

                } else {
                    dbItemDailyPlan = dbLayerDailyPlan.getUniqueDailyPlan(plannedOrder);
                }
                if (OrderInitiatorGlobals.orderInitiatorSettings.isOverwrite() || dbItemDailyPlan == null) {
                    LOGGER.debug("snchronizer: adding planned order to database: " + plannedOrder.uniqueOrderkey());
                    plannedOrder.setAverageDuration(listOfDurations.get(plannedOrder.uniqueOrderkey()));
                    dbLayerDailyPlan.store(plannedOrder);
                    plannedOrder.setStoredInDb(true);
                }
            }

            OrderApi.addOrderToController(listOfPlannedOrders);

            Set<OrderId> setOfOrderIds = OrderApi.getNotMarkWithRemoveOrdersWhenTerminated();
              
            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
            filter.setControllerId(OrderInitiatorGlobals.orderInitiatorSettings.getControllerId());
            filter.setSetOfOrders(setOfOrderIds);
            dbLayerDailyPlannedOrders.markOrdersAsSubmitted(filter);
            OrderApi.setRemoveOrdersWhenTerminated(setOfOrderIds);

            Globals.commit(sosHibernateSession);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

    public Map<PlannedOrderKey, PlannedOrder> getListOfPlannedOrders() {
        return listOfPlannedOrders;
    }
}
