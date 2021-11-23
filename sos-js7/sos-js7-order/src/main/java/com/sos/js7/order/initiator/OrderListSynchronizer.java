package com.sos.js7.order.initiator;

import java.nio.file.Paths;
import java.text.ParseException;
import java.time.Instant;
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
import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSDuration;
import com.sos.commons.util.SOSDurations;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.cluster.AJocClusterService;
import com.sos.joc.db.dailyplan.DBItemDailyPlanHistory;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanSubmission;
import com.sos.joc.db.dailyplan.DBItemDailyPlanWithHistory;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.js7.order.initiator.classes.CycleOrderKey;
import com.sos.js7.order.initiator.classes.OrderApi;
import com.sos.js7.order.initiator.classes.OrderCounter;
import com.sos.js7.order.initiator.classes.PlannedOrder;
import com.sos.js7.order.initiator.classes.PlannedOrderKey;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data_for_java.controller.JControllerState;

public class OrderListSynchronizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderListSynchronizer.class);

    private final JControllerState currentState;
    private DailyPlanSettings settings;
    private JocError jocError;
    private DBItemDailyPlanSubmission submission;
    private Map<PlannedOrderKey, PlannedOrder> plannedOrders;
    private Map<WorkflowAtController, Boolean> existingWorkflows;
    private Map<String, Long> durations;

    private String accessToken;

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

    public OrderListSynchronizer(JControllerState currentState, DailyPlanSettings settings) {
        this.plannedOrders = new TreeMap<PlannedOrderKey, PlannedOrder>();
        this.settings = settings;
        this.currentState = currentState;
    }

    public boolean add(String controllerId, PlannedOrder o) {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        boolean added = false;
        if (existingWorkflows == null) {
            existingWorkflows = new HashMap<WorkflowAtController, Boolean>();
        }
        WorkflowAtController w = new WorkflowAtController();
        w.controllerId = controllerId;
        w.workflowName = o.getSchedule().getWorkflowName();
        if (existingWorkflows.get(w) == null) {
            existingWorkflows.put(w, WorkflowsHelper.workflowCurrentlyExists(currentState, o.getSchedule().getWorkflowName()));
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][workflow=%s added", w.controllerId, w.workflowName));
            }
        }

        Boolean exists = existingWorkflows.get(w);
        if (exists) {
            added = true;
            plannedOrders.put(o.uniqueOrderkey(), o);
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][workflow=%s not deployed][skip]%s", w.controllerId, w.workflowName, SOSString.toString(o)));
            }
        }
        return added;
    }

    private void calculateDurations() throws SOSHibernateException, JocConfigurationException, DBConnectionRefusedException, DBOpenSessionException {
        LOGGER.debug("... calculateDurations");
        durations = new HashMap<String, Long>();

        for (PlannedOrder plannedOrder : plannedOrders.values()) {
            calculateDuration(plannedOrder);
        }
    }

    private void calculateDuration(PlannedOrder plannedOrder) throws SOSHibernateException, JocConfigurationException, DBConnectionRefusedException,
            DBOpenSessionException {

        if (durations.get(plannedOrder.getSchedule().getWorkflowName()) == null) {
            SOSHibernateSession session = null;
            try {
                FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
                filter.setOrderCriteria(null);
                filter.setControllerId(plannedOrder.getControllerId());
                filter.addWorkflowName(plannedOrder.getSchedule().getWorkflowName());

                session = Globals.createSosHibernateStatelessConnection("calculateDurations");
                DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
                // Globals.beginTransaction(session);
                List<DBItemDailyPlanWithHistory> orders = dbLayer.getDailyPlanWithHistoryList(filter, 0);
                session.close();
                session = null;

                SOSDurations durations = new SOSDurations();
                for (DBItemDailyPlanWithHistory item : orders) {
                    if (item.getOrderHistoryId() != null) {
                        SOSDuration duration = new SOSDuration();
                        duration.setStartTime(item.getStartTime());
                        duration.setEndTime(item.getEndTime());
                        durations.add(duration);
                    }
                }
                this.durations.put(plannedOrder.getSchedule().getWorkflowName(), durations.average());
            } finally {
                Globals.disconnect(session);
            }
        }
    }

    private List<DBItemDailyPlanHistory> insertHistory(SOSHibernateSession session, Set<PlannedOrder> addedOrders) throws SOSHibernateException {

        Date submissionTime = settings.getSubmissionTime() == null ? new Date() : settings.getSubmissionTime();

        List<DBItemDailyPlanHistory> result = new ArrayList<DBItemDailyPlanHistory>();
        for (PlannedOrder order : addedOrders) {
            Date dailyPlanDate = null;
            if (settings.getDailyPlanDate() == null) {
                Calendar dp = Calendar.getInstance();
                dp.setTimeInMillis(order.getFreshOrder().getScheduledFor());
                dp.set(Calendar.MINUTE, 0);
                dp.set(Calendar.SECOND, 0);
                dp.set(Calendar.HOUR_OF_DAY, 0);
                dailyPlanDate = dp.getTime();
            } else {
                dailyPlanDate = settings.getDailyPlanDate();
            }

            DBItemDailyPlanHistory item = new DBItemDailyPlanHistory();
            item.setSubmitted(false);
            item.setControllerId(order.getControllerId());
            item.setCreated(JobSchedulerDate.nowInUtc());
            item.setDailyPlanDate(dailyPlanDate);
            item.setOrderId(order.getFreshOrder().getId());
            item.setScheduledFor(new Date(order.getFreshOrder().getScheduledFor()));
            item.setWorkflowPath(order.getSchedule().getWorkflowPath());
            if (item.getWorkflowPath() != null) {
                String folderName = Paths.get(item.getWorkflowPath()).getParent().toString().replace('\\', '/');
                item.setWorkflowFolder(folderName);
            }
            item.setSubmissionTime(submissionTime);
            item.setUserAccount(settings.getUserAccount());

            session.save(item);
            result.add(item);
        }
        return result;
    }

    public void submitOrdersToController(String controllerId, boolean fromService) throws ControllerConnectionResetException,
            ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException,
            DBConnectionRefusedException, InterruptedException, ExecutionException, SOSHibernateException, TimeoutException, ParseException {

        String method = "submitOrdersToController";
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        Set<PlannedOrder> orders = new HashSet<PlannedOrder>();
        for (PlannedOrder p : plannedOrders.values()) {
            if (p.isStoredInDb() && p.getSchedule().getSubmitOrderToControllerWhenPlanned()) {
                orders.add(p);
            } else {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format(
                            "[%s][%s][skip because planned order !p.isStoredInDb() or !p.getSchedule().getSubmitOrderToControllerWhenPlanned()]%s",
                            method, controllerId, SOSString.toString(p)));
                }
            }
        }

        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("submitOrdersToController");
            session.setAutoCommit(false);

            List<DBItemDailyPlanHistory> inserted = new ArrayList<DBItemDailyPlanHistory>();
            try {
                Instant start = Instant.now();
                Globals.beginTransaction(session);
                inserted = insertHistory(session, orders);
                Globals.commit(session);
                Globals.disconnect(session); // disconnect here, not wait for the controller operations
                session = null;

                Instant end = Instant.now();
                LOGGER.info(String.format("[%s][%s][%s of %s orders]history added=%s(%s)", method, controllerId, orders.size(), plannedOrders.size(),
                        inserted.size(), SOSDate.getDuration(start, end)));

                OrderApi.addOrdersToController(controllerId, jocError, accessToken, orders, inserted, fromService);

            } catch (Exception e) {
                LOGGER.info(String.format("[%s][%s][%s of %s orders]history added=%s", method, controllerId, orders.size(), plannedOrders.size(),
                        inserted.size()));

                LOGGER.warn(e.getLocalizedMessage());
            }
        } finally {
            Globals.disconnect(session);
        }

    }

    private OrderCounter executeStore(String operation, String controllerId, String date) throws JocConfigurationException,
            DBConnectionRefusedException, SOSHibernateException, ParseException, JsonProcessingException {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        SOSHibernateSession session = null;
        OrderCounter counter = new OrderCounter();
        try {
            session = Globals.createSosHibernateStatelessConnection("addPlannedOrderToDB");
            DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
            session.setAutoCommit(false);
            Globals.beginTransaction(session);

            Map<CycleOrderKey, List<PlannedOrder>> cyclic = new TreeMap<CycleOrderKey, List<PlannedOrder>>();
            for (PlannedOrder plannedOrder : plannedOrders.values()) {
                if (plannedOrder.getPeriod().getSingleStart() != null) {
                    counter.addSingle();
                    DBItemDailyPlanOrder item = dbLayer.getUniqueDailyPlan(plannedOrder);
                    if (settings.isOverwrite() || item == null) {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[store][single]%s", plannedOrder.uniqueOrderkey()));
                        }
                        plannedOrder.setAverageDuration(durations.get(plannedOrder.getSchedule().getWorkflowName()));
                        dbLayer.store(plannedOrder);
                        plannedOrder.setStoredInDb(true);
                        counter.addStoredSingle();
                    } else {
                        counter.addStoreSkippedSingle();
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[store][single][skip][%s][isOverwrite=%s][item=%s]", plannedOrder.uniqueOrderkey(), settings
                                    .isOverwrite(), SOSHibernate.toString(item)));
                        }
                    }
                } else {
                    CycleOrderKey key = new CycleOrderKey();
                    key.setPeriodBegin(plannedOrder.getPeriod().getBegin());
                    key.setPeriodEnd(plannedOrder.getPeriod().getEnd());
                    key.setRepeat(plannedOrder.getPeriod().getRepeat());
                    key.setOrderName(plannedOrder.getOrderName());
                    key.setWorkflowPath(plannedOrder.getSchedule().getWorkflowPath());

                    if (cyclic.get(key) == null) {
                        cyclic.put(key, new ArrayList<PlannedOrder>());
                        counter.addCyclic();
                    }
                    cyclic.get(key).add(plannedOrder);
                    counter.addCyclicTotal();
                }
            }

            for (Entry<CycleOrderKey, List<PlannedOrder>> entry : cyclic.entrySet()) {
                int size = entry.getValue().size();
                int nr = 1;
                String id = Long.valueOf(Instant.now().toEpochMilli()).toString().substring(3);
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[store][cycle]%s", size));
                }
                for (PlannedOrder plannedOrder : entry.getValue()) {
                    DBItemDailyPlanOrder item = dbLayer.getUniqueDailyPlan(plannedOrder);
                    if (settings.isOverwrite() || item == null) {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[store][cycle]%s", plannedOrder.uniqueOrderkey()));
                        }
                        plannedOrder.setAverageDuration(durations.get(plannedOrder.getSchedule().getWorkflowName()));
                        dbLayer.store(plannedOrder, id, nr, size);
                        nr = nr + 1;
                        plannedOrder.setStoredInDb(true);
                        counter.addStoredCyclicTotal();
                    } else {
                        counter.addStoreSkippedCyclicTotal();

                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[store][cycle][skip][%s][isOverwrite=%s][item=%s]", plannedOrder.uniqueOrderkey(), settings
                                    .isOverwrite(), SOSHibernate.toString(item)));
                        }
                    }
                }
            }
            Globals.commit(session);
        } finally {
            Globals.disconnect(session);
        }

        LOGGER.info(String.format("[%s][%s][%s][stored]%s", operation, controllerId, date, counter));
        return counter;
    }

    public void addPlannedOrderToControllerAndDB(String operation, String controllerId, String date, Boolean withSubmit, boolean fromService)
            throws JocConfigurationException, DBConnectionRefusedException, ControllerConnectionResetException, ControllerConnectionRefusedException,
            DBMissingDataException, DBOpenSessionException, DBInvalidDataException, SOSHibernateException, JsonProcessingException, ParseException,
            InterruptedException, ExecutionException, TimeoutException {

        LOGGER.debug("... addPlannedOrderToControllerAndDB");
        calculateDurations();

        if (settings.isOverwrite()) {
            LOGGER.debug("Overwrite orders");
            SOSHibernateSession session = null;
            List<DBItemDailyPlanOrder> orders = new ArrayList<DBItemDailyPlanOrder>();
            try {
                session = Globals.createSosHibernateStatelessConnection("addPlannedOrderToDBOverwrite");
                DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);

                for (PlannedOrder plannedOrder : plannedOrders.values()) {
                    final FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
                    filter.setPlannedStart(new Date(plannedOrder.getFreshOrder().getScheduledFor()));
                    filter.setControllerId(controllerId);
                    filter.addWorkflowName(Paths.get(plannedOrder.getFreshOrder().getWorkflowPath()).getFileName().toString());

                    List<DBItemDailyPlanOrder> l = dbLayer.getDailyPlanList(filter, 0);
                    orders.addAll(l);
                }
            } finally {
                Globals.disconnect(session);
            }
            CompletableFuture<Either<Problem, Void>> c = OrdersHelper.removeFromJobSchedulerController(controllerId, orders);
            c.thenAccept(either -> {
                ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
                if (either.isRight()) {
                    if (fromService) {
                        AJocClusterService.setLogger(ClusterServices.dailyplan.name());
                    }
                    SOSHibernateSession session4delete = null;
                    try {
                        session4delete = Globals.createSosHibernateStatelessConnection("addPlannedOrderToDB");
                        session4delete.setAutoCommit(false);
                        Globals.beginTransaction(session4delete);
                        DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session4delete);
                        for (PlannedOrder plannedOrder : plannedOrders.values()) {
                            final FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
                            filter.setPlannedStart(new Date(plannedOrder.getFreshOrder().getScheduledFor()));
                            filter.setControllerId(controllerId);
                            filter.addWorkflowName(Paths.get(plannedOrder.getFreshOrder().getWorkflowPath()).getFileName().toString());

                            LOGGER.trace("----> Remove: " + plannedOrder.getFreshOrder().getScheduledFor() + ":" + new Date(plannedOrder
                                    .getFreshOrder().getScheduledFor()));

                            dbLayer.deleteCascading(filter);
                        }
                        Globals.commit(session4delete);

                        executeStore(operation, controllerId, date);
                        if (withSubmit == null || withSubmit) {
                            submitOrdersToController(controllerId, fromService);
                        } else {
                            LOGGER.debug("Orders will not be submitted to the controller");
                        }
                    } catch (SOSHibernateException | JocConfigurationException | DBConnectionRefusedException | ParseException
                            | ControllerConnectionResetException | ControllerConnectionRefusedException | DBMissingDataException
                            | DBOpenSessionException | DBInvalidDataException | InterruptedException | ExecutionException | TimeoutException e) {
                        ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), controllerId);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    } finally {
                        Globals.disconnect(session4delete);
                    }
                }
            });

        } else {
            executeStore(operation, controllerId, date);
            if (withSubmit == null || withSubmit) {
                submitOrdersToController(controllerId, fromService);
            } else {
                LOGGER.debug("Orders will not be submitted to the controller");
            }
        }
    }

    public DBItemDailyPlanSubmission getSubmission() {
        return submission;
    }

    public void setSubmission(DBItemDailyPlanSubmission val) {
        submission = val;
    }

    public Map<PlannedOrderKey, PlannedOrder> getPlannedOrders() {
        return plannedOrders;
    }

    public JocError getJocError() {
        return jocError;
    }

    public void setJocError(JocError val) {
        jocError = val;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String val) {
        accessToken = val;
    }
}
