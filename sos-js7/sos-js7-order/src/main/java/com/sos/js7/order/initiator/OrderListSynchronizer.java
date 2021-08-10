package com.sos.js7.order.initiator;

import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
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
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.orders.DBItemDailyPlanHistory;
import com.sos.joc.db.orders.DBItemDailyPlanOrders;
import com.sos.joc.db.orders.DBItemDailyPlanWithHistory;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocError;
import com.sos.js7.order.initiator.classes.CycleOrderKey;
import com.sos.js7.order.initiator.classes.OrderApi;
import com.sos.js7.order.initiator.classes.PlannedOrder;
import com.sos.js7.order.initiator.classes.PlannedOrderKey;
import com.sos.js7.order.initiator.db.DBLayerDailyPlanHistory;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data_for_java.controller.JControllerState;

public class OrderListSynchronizer {

    final JControllerState currentState;
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderListSynchronizer.class);
    private OrderInitiatorSettings orderInitiatorSettings;

    private Map<PlannedOrderKey, PlannedOrder> listOfPlannedOrders;
    private Map<String, Long> listOfDurations;
    private Map<WorkflowAtController, Boolean> listOfExistingWorkflows;
    private String accessToken;
    private JocError jocError;

    class WorkflowAtController {

        protected String workflowName;
        protected String controllerId;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((controllerId == null) ? 0 : controllerId.hashCode());
            result = prime * result + ((workflowName == null) ? 0 : workflowName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            WorkflowAtController other = (WorkflowAtController) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (controllerId == null) {
                if (other.controllerId != null)
                    return false;
            } else if (!controllerId.equals(other.controllerId))
                return false;
            if (workflowName == null) {
                if (other.workflowName != null)
                    return false;
            } else if (!workflowName.equals(other.workflowName))
                return false;
            return true;
        }

        private OrderListSynchronizer getOuterType() {
            return OrderListSynchronizer.this;
        }
    }

    public OrderListSynchronizer(JControllerState _currentState, OrderInitiatorSettings orderInitiatorSettings) {
        listOfPlannedOrders = new TreeMap<PlannedOrderKey, PlannedOrder>();
        this.orderInitiatorSettings = orderInitiatorSettings;
        currentState = _currentState;
    }

    public boolean add(String controllerId, PlannedOrder o) {
        boolean added = false;
        if (listOfExistingWorkflows == null) {
            listOfExistingWorkflows = new HashMap<WorkflowAtController, Boolean>();
            LOGGER.debug("Create list of existing workflows");
        }
        WorkflowAtController w = new WorkflowAtController();
        w.controllerId = controllerId;
        w.workflowName = o.getSchedule().getWorkflowName();
        if (listOfExistingWorkflows.get(w) == null) {
            listOfExistingWorkflows.put(w, WorkflowsHelper.workflowCurrentlyExists(currentState, o.getSchedule().getWorkflowName()));
            LOGGER.debug("Adding workflow " + w.workflowName + " for controller " + w.controllerId + " to list of existing workflows");
        }

        Boolean exists = listOfExistingWorkflows.get(w);
        if (exists) {
            added = true;
            listOfPlannedOrders.put(o.uniqueOrderkey(), o);
        } else {
            LOGGER.debug("Workflow " + w.workflowName + " not deployed for controller " + w.controllerId);
        }
        return added;
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

    private List<DBItemDailyPlanHistory> insertHistory(SOSHibernateSession sosHibernateSession, Set<PlannedOrder> addedOrders)
            throws SOSHibernateException {

        List<DBItemDailyPlanHistory> listOfInsertHistoryEntries = new ArrayList<DBItemDailyPlanHistory>();
        Date submissionTime = null;
        Date dailyPlanDate = null;

        if (orderInitiatorSettings.getSubmissionTime() == null) {
            submissionTime = new Date();
        } else {
            submissionTime = orderInitiatorSettings.getSubmissionTime();
        }

        DBLayerDailyPlanHistory dbLayerDailyPlanHistory = new DBLayerDailyPlanHistory(sosHibernateSession);
        for (PlannedOrder addedOrder : addedOrders) {
            if (orderInitiatorSettings.getDailyPlanDate() == null) {
                Calendar dp = Calendar.getInstance();
                dp.setTimeInMillis(addedOrder.getFreshOrder().getScheduledFor());
                dp.set(Calendar.MINUTE, 0);
                dp.set(Calendar.SECOND, 0);
                dp.set(Calendar.HOUR_OF_DAY, 0);
                dailyPlanDate = dp.getTime();
            } else {

                dailyPlanDate = orderInitiatorSettings.getDailyPlanDate();
            }
            DBItemDailyPlanHistory dbItemDailyPlanHistory = new DBItemDailyPlanHistory();
            dbItemDailyPlanHistory.setSubmitted(false);
            dbItemDailyPlanHistory.setControllerId(addedOrder.getControllerId());
            dbItemDailyPlanHistory.setCreated(JobSchedulerDate.nowInUtc());
            dbItemDailyPlanHistory.setDailyPlanDate(dailyPlanDate);
            dbItemDailyPlanHistory.setOrderId(addedOrder.getFreshOrder().getId());
            dbItemDailyPlanHistory.setScheduledFor(new Date(addedOrder.getFreshOrder().getScheduledFor()));
            dbItemDailyPlanHistory.setWorkflowPath(addedOrder.getSchedule().getWorkflowPath());
            if (dbItemDailyPlanHistory.getWorkflowPath() != null) {
                String folderName = Paths.get(dbItemDailyPlanHistory.getWorkflowPath()).getParent().toString().replace('\\', '/');
                dbItemDailyPlanHistory.setWorkflowFolder(folderName);
            }
            dbItemDailyPlanHistory.setSubmissionTime(submissionTime);
            dbItemDailyPlanHistory.setUserAccount(orderInitiatorSettings.getUserAccount());
            dbLayerDailyPlanHistory.storeDailyPlanHistory(dbItemDailyPlanHistory);
            listOfInsertHistoryEntries.add(dbItemDailyPlanHistory);

        }
        return listOfInsertHistoryEntries;
    }

    public void submitOrdersToController(String controllerId) throws ControllerConnectionResetException, ControllerConnectionRefusedException,
            DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
            InterruptedException, ExecutionException, SOSHibernateException, TimeoutException, ParseException {

        LOGGER.debug(listOfPlannedOrders.size() + " orders will be submitted to the controller");

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

            List<DBItemDailyPlanHistory> listOfInsertHistoryEntries = new ArrayList<DBItemDailyPlanHistory>();
            try {
                Globals.beginTransaction(sosHibernateSession);
                listOfInsertHistoryEntries = insertHistory(sosHibernateSession, addedOrders);
                Globals.commit(sosHibernateSession);

                OrderApi.addOrderToController(controllerId, jocError, accessToken, addedOrders, listOfInsertHistoryEntries);

            } catch (Exception e) {
                LOGGER.warn(e.getLocalizedMessage());
            }
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

    private void executeStore() throws JocConfigurationException, DBConnectionRefusedException, SOSHibernateException, ParseException {
        SOSHibernateSession sosHibernateSession = null;

        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("addPlannedOrderToDB");
            DBLayerDailyPlannedOrders dbLayerDailyPlan = new DBLayerDailyPlannedOrders(sosHibernateSession);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            for (PlannedOrder plannedOrder : listOfPlannedOrders.values()) {
                if (plannedOrder.getPeriod().getSingleStart() != null) {
                    DBItemDailyPlanOrders dbItemDailyPlan = null;
                    dbItemDailyPlan = dbLayerDailyPlan.getUniqueDailyPlan(plannedOrder);

                    if (orderInitiatorSettings.isOverwrite() || dbItemDailyPlan == null) {
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
                    cycleOrderKey.setOrderName(plannedOrder.getOrderName());
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

                    if (orderInitiatorSettings.isOverwrite() || dbItemDailyPlan == null) {
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

   
    public void addPlannedOrderToControllerAndDB(String controllerId, Boolean withSubmit) throws JocConfigurationException,
            DBConnectionRefusedException, ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException,
            DBOpenSessionException, DBInvalidDataException, SOSHibernateException, JsonProcessingException, ParseException, InterruptedException,
            ExecutionException, TimeoutException {
        LOGGER.debug("... addPlannedOrderToControllerAndDB");

        LOGGER.debug("... addPlannedOrderToDB");

        calculateDurations();
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("addPlannedOrderToDB");

            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
            if (orderInitiatorSettings.isOverwrite()) {
                LOGGER.debug("Overwrite orders");
                List<DBItemDailyPlanOrders> listOfDailyPlanOrders = new ArrayList<DBItemDailyPlanOrders>();
                for (PlannedOrder plannedOrder : listOfPlannedOrders.values()) {
                    final FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
                    filter.setPlannedStart(new Date(plannedOrder.getFreshOrder().getScheduledFor()));

                    filter.setControllerId(controllerId);
                    String workflowName = Paths.get(plannedOrder.getFreshOrder().getWorkflowPath()).getFileName().toString();
                    filter.addWorkflowName(workflowName);
                    List<DBItemDailyPlanOrders> l = dbLayerDailyPlannedOrders.getDailyPlanList(filter, 0);
                    listOfDailyPlanOrders.addAll(l);
                }
                CompletableFuture<Either<Problem, Void>> c = OrdersHelper.removeFromJobSchedulerController(controllerId, listOfDailyPlanOrders);
                c.thenAccept(either -> {
                    ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
                    if (either.isRight()) {
                        SOSHibernateSession sosHibernateSession2 = null;
                        try {
                            sosHibernateSession2 = Globals.createSosHibernateStatelessConnection("addPlannedOrderToDB");
                            sosHibernateSession2.setAutoCommit(false);
                            Globals.beginTransaction(sosHibernateSession2);
                            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders2 = new DBLayerDailyPlannedOrders(sosHibernateSession2);
                            for (PlannedOrder plannedOrder : listOfPlannedOrders.values()) {
                                final FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
                                filter.setPlannedStart(new Date(plannedOrder.getFreshOrder().getScheduledFor()));
                                LOGGER.trace("----> Remove: " + plannedOrder.getFreshOrder().getScheduledFor() + ":" + new Date(plannedOrder
                                        .getFreshOrder().getScheduledFor()));
                                filter.setControllerId(controllerId);
                                String workflowName = Paths.get(plannedOrder.getFreshOrder().getWorkflowPath()).getFileName().toString();
                                filter.addWorkflowName(workflowName);
                                dbLayerDailyPlannedOrders2.deleteCascading(filter);
                            }
                            Globals.commit(sosHibernateSession2);


                            executeStore();
                            if (withSubmit == null || withSubmit) {
                                submitOrdersToController(controllerId);
                            } else {
                                LOGGER.debug("Orders will not be submitted to the controller");
                            }
                        } catch (SOSHibernateException | JocConfigurationException | DBConnectionRefusedException | ParseException | ControllerConnectionResetException | ControllerConnectionRefusedException | DBMissingDataException | DBOpenSessionException | DBInvalidDataException | InterruptedException | ExecutionException | TimeoutException e) {
                            ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), controllerId);
                        } finally {
                            Globals.disconnect(sosHibernateSession2);
                        }
                    }
                });

            } else {
                executeStore();
                if (withSubmit == null || withSubmit) {
                    submitOrdersToController(controllerId);
                } else {
                    LOGGER.debug("Orders will not be submitted to the controller");
                }
            }
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
       
    }

    public Map<PlannedOrderKey, PlannedOrder> getListOfPlannedOrders() {
        return listOfPlannedOrders;
    }

    public void resetListOfPlannedOrders() {
        listOfPlannedOrders = null;
    }

    public JocError getJocError() {
        return jocError;
    }

    public void setJocError(JocError jocError) {
        this.jocError = jocError;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
