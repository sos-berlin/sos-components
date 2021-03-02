package com.sos.js7.order.initiator;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
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
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.OrderHelper;
import com.sos.joc.db.orders.DBItemDailyPlanHistory;
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
import com.sos.js7.order.initiator.classes.CycleOrderKey;
import com.sos.js7.order.initiator.classes.OrderApi;
import com.sos.js7.order.initiator.classes.OrderInitiatorGlobals;
import com.sos.js7.order.initiator.classes.PlannedOrder;
import com.sos.js7.order.initiator.classes.PlannedOrderKey;
import com.sos.js7.order.initiator.db.DBLayerDailyPlanHistory;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;

import js7.data.order.OrderId;

public class OrderListSynchronizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderListSynchronizer.class);
    private Map<PlannedOrderKey, PlannedOrder> listOfPlannedOrders;
    private Map<String, Long> listOfDurations;

    public OrderListSynchronizer() {
        listOfPlannedOrders = new TreeMap<PlannedOrderKey, PlannedOrder>();
    }

    public OrderListSynchronizer(OrderInitiatorSettings orderInitiatorSettings) {
        listOfPlannedOrders = new TreeMap<PlannedOrderKey, PlannedOrder>();
        OrderInitiatorGlobals.orderInitiatorSettings = orderInitiatorSettings;
    }

    public void add(PlannedOrder o) {
        listOfPlannedOrders.put(o.uniqueOrderkey(), o);
    }

    private void calculateDuration(PlannedOrder plannedOrder) throws SOSHibernateException, JocConfigurationException, DBConnectionRefusedException,
            DBOpenSessionException {

        if (listOfDurations.get(plannedOrder.getSchedule().getWorkflowName()) == null) {

            SOSHibernateSession sosHibernateSession = null;
            try {
                sosHibernateSession = Globals.createSosHibernateStatelessConnection("calculateDurations");

                FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
                DBLayerDailyPlannedOrders dbLayerDailyPlan = new DBLayerDailyPlannedOrders(sosHibernateSession);
                Globals.beginTransaction(sosHibernateSession);
                filter.setControllerId(plannedOrder.getControllerId());
                filter.addWorkflowName(plannedOrder.getSchedule().getWorkflowName());
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
                listOfDurations.put(plannedOrder.getSchedule().getWorkflowName(), sosDurations.average());
            } finally {
                Globals.disconnect(sosHibernateSession);
            }
        }
    }

    private void calculateDurations() throws SOSHibernateException, JocConfigurationException, DBConnectionRefusedException, DBOpenSessionException {
        LOGGER.debug("... calculateDurations");
        listOfDurations = new HashMap<String, Long>();

        for (PlannedOrder plannedOrder : listOfPlannedOrders.values()) {
            calculateDuration(plannedOrder);
        }
    }

    public void submitOrdersToController() throws JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException,
            DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
            InterruptedException, ExecutionException, SOSHibernateException, TimeoutException, ParseException {

        LOGGER.debug(listOfPlannedOrders.size() + " orders will be submitted to the controller");
        Map<String, Integer> listOfExceptions = new HashMap<String, Integer>();

        Set<PlannedOrder> addedOrders = new HashSet<PlannedOrder>();
        for (PlannedOrder p : listOfPlannedOrders.values()) {
            if (p.isStoredInDb() && p.getSchedule().getSubmitOrderToControllerWhenPlanned()) {
                addedOrders.add(p);
            }
        }
        SOSHibernateSession sosHibernateSession = null;

        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("submitOrdersToController");
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);

            try {
                OrderApi.addOrderToController(addedOrders);
            } catch (Exception e) {
                Integer cnt = listOfExceptions.get(e.getMessage());
                if (cnt == null) {
                    cnt = 0;
                }
                cnt = cnt + 1;
                listOfExceptions.put(e.getMessage(), cnt);
            }

            for (Entry<String, Integer> entry : listOfExceptions.entrySet()) {
                Integer cnt = entry.getValue();
                String s = "";
                if (cnt > 1) {
                    s = " occurs " + cnt + " times";
                }

                LOGGER.warn(entry.getKey() + s);
            }

            Set<OrderId> setOfOrderIds = OrderApi.getNotMarkWithRemoveOrdersWhenTerminated();

            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
            filter.setControllerId(OrderInitiatorGlobals.orderInitiatorSettings.getControllerId());
            filter.setSetOfPlannedOrder(addedOrders);
            filter.setSubmitted(true);
            dbLayerDailyPlannedOrders.setSubmitted(filter);
            OrderApi.setRemoveOrdersWhenTerminated(setOfOrderIds);

            if (OrderInitiatorGlobals.submissionTime != null && OrderInitiatorGlobals.dailyPlanDate != null) {

                DBLayerDailyPlanHistory dbLayerDailyPlanHistory = new DBLayerDailyPlanHistory(sosHibernateSession);
                for (PlannedOrder addedOrder : addedOrders) {
                    DBItemDailyPlanHistory dbItemDailyPlanHistory = new DBItemDailyPlanHistory();
                    dbItemDailyPlanHistory.setSubmitted(true);
                    dbItemDailyPlanHistory.setControllerId(addedOrder.getControllerId());
                    dbItemDailyPlanHistory.setCreated(JobSchedulerDate.nowInUtc());
                    dbItemDailyPlanHistory.setDailyPlanDate(OrderInitiatorGlobals.dailyPlanDate);
                    dbItemDailyPlanHistory.setOrderId(addedOrder.getFreshOrder().getId());
                    dbItemDailyPlanHistory.setScheduledFor(new Date(addedOrder.getFreshOrder().getScheduledFor()));
                    dbItemDailyPlanHistory.setWorkflowPath(addedOrder.getSchedule().getWorkflowPath());
                    dbItemDailyPlanHistory.setSubmissionTime(OrderInitiatorGlobals.submissionTime);
                    dbItemDailyPlanHistory.setUserAccount(OrderInitiatorGlobals.orderInitiatorSettings.getUserAccount());
                    dbLayerDailyPlanHistory.storeDailyPlanHistory(dbItemDailyPlanHistory);
                }
            }
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
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("addPlannedOrderToDB");
            DBLayerDailyPlannedOrders dbLayerDailyPlan = new DBLayerDailyPlannedOrders(sosHibernateSession);
            sosHibernateSession.setAutoCommit(false);

            Globals.beginTransaction(sosHibernateSession);

            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
            if (OrderInitiatorGlobals.orderInitiatorSettings.isOverwrite()) {
                LOGGER.debug("Overwrite orders");
                for (PlannedOrder plannedOrder : listOfPlannedOrders.values()) {
                    FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
                    filter.setPlannedStart(new Date(plannedOrder.getFreshOrder().getScheduledFor()));
                    LOGGER.trace("----> Remove: " + plannedOrder.getFreshOrder().getScheduledFor() + ":" + new Date(plannedOrder.getFreshOrder()
                            .getScheduledFor()));
                    filter.setControllerId(OrderInitiatorGlobals.orderInitiatorSettings.getControllerId());
                    filter.addWorkflowName(plannedOrder.getFreshOrder().getWorkflowPath());
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
                if (plannedOrder.getPeriod().getSingleStart() != null) {
                    DBItemDailyPlanOrders dbItemDailyPlan = null;
                    dbItemDailyPlan = dbLayerDailyPlan.getUniqueDailyPlan(plannedOrder);

                    if (OrderInitiatorGlobals.orderInitiatorSettings.isOverwrite() || dbItemDailyPlan == null) {
                        LOGGER.trace("snchronizer: adding planned order to database: " + plannedOrder.uniqueOrderkey());
                        plannedOrder.setAverageDuration(listOfDurations.get(plannedOrder.getSchedule().getWorkflowName()));
                        dbLayerDailyPlan.store(plannedOrder);
                        plannedOrder.setStoredInDb(true);
                    }
                }
            }

            Map<CycleOrderKey, List<PlannedOrder>> mapOfCycledOrders = new TreeMap<CycleOrderKey, List<PlannedOrder>>();

            for (PlannedOrder plannedOrder : listOfPlannedOrders.values()) {
                if (plannedOrder.getPeriod().getSingleStart() == null) {

                    CycleOrderKey cycleOrderKey = new CycleOrderKey();
                    cycleOrderKey.setPeriodBegin(plannedOrder.getPeriod().getBegin());
                    cycleOrderKey.setPeriodEnd(plannedOrder.getPeriod().getEnd());
                    cycleOrderKey.setRepeat(plannedOrder.getPeriod().getRepeat());
                    cycleOrderKey.setSchedulePath(plannedOrder.getSchedule().getPath());
                    cycleOrderKey.setWorkflowPath(plannedOrder.getSchedule().getWorkflowPath());

                    if (mapOfCycledOrders.get(cycleOrderKey) == null) {
                        mapOfCycledOrders.put(cycleOrderKey, new ArrayList<PlannedOrder>());
                    }
                    mapOfCycledOrders.get(cycleOrderKey).add(plannedOrder);
                }
            }

            for (Entry<CycleOrderKey, List<PlannedOrder>> entry : mapOfCycledOrders.entrySet()) {
                int size = entry.getValue().size();
                int nr = 1;
                Long firstId = null;
                for (PlannedOrder plannedOrder : entry.getValue()) {

                    DBItemDailyPlanOrders dbItemDailyPlan = null;
                    dbItemDailyPlan = dbLayerDailyPlan.getUniqueDailyPlan(plannedOrder);

                    if (OrderInitiatorGlobals.orderInitiatorSettings.isOverwrite() || dbItemDailyPlan == null) {
                        LOGGER.trace("snchronizer: adding planned cylced order to database: " + nr + " of " + size + " " + plannedOrder
                                .uniqueOrderkey());
                        plannedOrder.setAverageDuration(listOfDurations.get(plannedOrder.getSchedule().getWorkflowName()));
                        Long fId = dbLayerDailyPlan.store(plannedOrder, firstId, nr, size);
                        if (firstId == null) {
                            firstId = fId;
                        }
                        nr = nr + 1;
                        plannedOrder.setStoredInDb(true);
                    }
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

    public void resetListOfPlannedOrders() {
        listOfPlannedOrders = null;
    }
}
