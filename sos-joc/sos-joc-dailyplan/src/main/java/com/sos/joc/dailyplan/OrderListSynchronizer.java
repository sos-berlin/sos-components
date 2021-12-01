package com.sos.joc.dailyplan;

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
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.cluster.AJocClusterService;
import com.sos.joc.dailyplan.common.CycleOrderKey;
import com.sos.joc.dailyplan.common.DailyPlanSettings;
import com.sos.joc.dailyplan.common.OrderCounter;
import com.sos.joc.dailyplan.common.PlannedOrder;
import com.sos.joc.dailyplan.common.PlannedOrderKey;
import com.sos.joc.dailyplan.db.DBLayerDailyPlannedOrders;
import com.sos.joc.dailyplan.db.FilterDailyPlannedOrders;
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

import io.vavr.control.Either;
import js7.base.problem.Problem;

public class OrderListSynchronizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderListSynchronizer.class);

    private DailyPlanSettings settings;
    private JocError jocError;
    private DBItemDailyPlanSubmission submission;
    private Map<PlannedOrderKey, PlannedOrder> plannedOrders;
    private Map<String, Long> durations;

    private String accessToken;

    public OrderListSynchronizer(DailyPlanSettings settings) {
        this.plannedOrders = new TreeMap<PlannedOrderKey, PlannedOrder>();
        this.settings = settings;
    }

    public boolean add(PlannedOrder o, String controllerId, String date) {
        if (o == null) {
            return false;
        }
        String workflow = null;
        try {
            workflow = o.getSchedule().getWorkflowName();
            String wpath = WorkflowPaths.getPathOrNull(workflow);
            if (wpath == null) {
                LOGGER.info(String.format("[%s][%s][workflow=%s not deployed][skip]%s", controllerId, date, workflow, SOSString.toString(o)));
                return false;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][%s][workflow=%s][added]%s", controllerId, date, workflow, SOSString.toString(o)));
            }
            plannedOrders.put(o.uniqueOrderkey(), o);
            return true;
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][%s][workflow=%s][order %s]%s", controllerId, date, workflow, SOSString.toString(o), e.toString()), e);
            return false;
        }
    }

    private void calculateDurations(String controllerId, String date) throws SOSHibernateException, JocConfigurationException,
            DBConnectionRefusedException, DBOpenSessionException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[calculateDurations][%s]%s", controllerId, date));
        }
        durations = new HashMap<String, Long>();
        for (PlannedOrder plannedOrder : plannedOrders.values()) {
            calculateDuration(plannedOrder, date);
        }
    }

    private void calculateDuration(PlannedOrder plannedOrder, String date) throws SOSHibernateException, JocConfigurationException,
            DBConnectionRefusedException, DBOpenSessionException {

        if (durations.get(plannedOrder.getSchedule().getWorkflowName()) == null) {
            SOSHibernateSession session = null;
            try {
                FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
                filter.setSortMode(null);
                filter.setOrderCriteria(null);
                filter.setControllerId(plannedOrder.getControllerId());
                filter.addWorkflowName(plannedOrder.getSchedule().getWorkflowName());

                session = Globals.createSosHibernateStatelessConnection("calculateDurations-" + date);
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

    public void submitOrdersToController(String controllerId, String submissionForDate, boolean fromService)
            throws ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, InterruptedException, ExecutionException,
            SOSHibernateException, TimeoutException, ParseException {

        String method = "submitOrdersToController";
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        String logSubmissionForDate = SOSString.isEmpty(submissionForDate) ? "" : "[" + submissionForDate + "]";
        Set<PlannedOrder> orders = new HashSet<PlannedOrder>();
        for (PlannedOrder p : plannedOrders.values()) {
            if (p.isStoredInDb() && p.getSchedule().getSubmitOrderToControllerWhenPlanned()) {
                orders.add(p);
            } else {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format(
                            "[%s][%s]%s[skip because planned order !p.isStoredInDb() or !p.getSchedule().getSubmitOrderToControllerWhenPlanned()]%s",
                            method, controllerId, logSubmissionForDate, SOSString.toString(p)));
                }
            }
        }

        SOSHibernateSession session = null;
        try {
            String sessionIdentifier = method;
            if (!SOSString.isEmpty(submissionForDate)) {
                sessionIdentifier += "-" + submissionForDate;
            }

            session = Globals.createSosHibernateStatelessConnection(sessionIdentifier);
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
                LOGGER.info(String.format("[%s][%s]%s[%s of %s orders]history added=%s(%s)", method, controllerId, logSubmissionForDate, orders
                        .size(), plannedOrders.size(), inserted.size(), SOSDate.getDuration(start, end)));

                OrderApi.addOrdersToController(controllerId, submissionForDate, fromService, orders, inserted, jocError, accessToken);

            } catch (Exception e) {
                LOGGER.info(String.format("[%s][%s]%s[%s of %s orders]history added=%s", method, controllerId, logSubmissionForDate, orders.size(),
                        plannedOrders.size(), inserted.size()));

                LOGGER.warn(e.toString(), e);
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
            session = Globals.createSosHibernateStatelessConnection("executeStore-" + date);
            DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
            session.setAutoCommit(false);
            Globals.beginTransaction(session);

            Map<CycleOrderKey, List<PlannedOrder>> cyclic = new TreeMap<CycleOrderKey, List<PlannedOrder>>();
            for (PlannedOrder plannedOrder : plannedOrders.values()) {
                if (plannedOrder.getPeriod().getSingleStart() != null) {
                    counter.addSingle();
                    DBItemDailyPlanOrder item = dbLayer.getUniqueDailyPlan(plannedOrder);
                    if (settings.isOverwrite() || item == null) {
                        plannedOrder.setAverageDuration(durations.get(plannedOrder.getSchedule().getWorkflowName()));
                        dbLayer.store(plannedOrder);
                        plannedOrder.setStoredInDb(true);
                        counter.addStoredSingle();
                    } else {
                        counter.addStoreSkippedSingle();
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[store][%s][%s][single][skip][%s][isOverwrite=%s][item=%s]", controllerId, date, plannedOrder
                                    .uniqueOrderkey(), settings.isOverwrite(), SOSHibernate.toString(item)));
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
                
                String id = OrdersHelper.getUniqueOrderId();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[store][%s][%s][cyclic]size=%s, order id main part=%s, key=%s", controllerId, date, size, id, entry
                            .getKey()));
                }
                for (PlannedOrder plannedOrder : entry.getValue()) {
                    DBItemDailyPlanOrder item = dbLayer.getUniqueDailyPlan(plannedOrder);
                    if (settings.isOverwrite() || item == null) {
                        plannedOrder.setAverageDuration(durations.get(plannedOrder.getSchedule().getWorkflowName()));
                        dbLayer.store(plannedOrder, id, nr, size);
                        nr = nr + 1;
                        plannedOrder.setStoredInDb(true);
                        counter.addStoredCyclicTotal();
                    } else {
                        counter.addStoreSkippedCyclicTotal();

                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[store][%s][%s][cyclic][skip][%s][isOverwrite=%s][item=%s]", controllerId, date, plannedOrder
                                    .uniqueOrderkey(), settings.isOverwrite(), SOSHibernate.toString(item)));
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

        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        String method = "addPlannedOrderToControllerAndDB";

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s][%s]overwrite orders=%s", method, controllerId, date, settings.isOverwrite()));
        }

        calculateDurations(controllerId, date);
        if (settings.isOverwrite()) {
            SOSHibernateSession session = null;
            List<DBItemDailyPlanOrder> orders = new ArrayList<DBItemDailyPlanOrder>();
            try {
                session = Globals.createSosHibernateStatelessConnection(method + "-" + date);
                DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);

                for (PlannedOrder plannedOrder : plannedOrders.values()) {
                    final FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
                    filter.setSortMode(null);
                    filter.setOrderCriteria(null);
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
                        session4delete = Globals.createSosHibernateStatelessConnection(method + "-" + date);
                        session4delete.setAutoCommit(false);
                        Globals.beginTransaction(session4delete);
                        DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session4delete);
                        for (PlannedOrder plannedOrder : plannedOrders.values()) {
                            final FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
                            filter.setSortMode(null);
                            filter.setOrderCriteria(null);
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
                            submitOrdersToController(controllerId, date, fromService);
                        } else {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[%s][%s][%s][skip]withSubmit=%s", method, controllerId, date, withSubmit));
                            }
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
                submitOrdersToController(controllerId, date, fromService);
            } else {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s][skip]withSubmit=%s", method, controllerId, date, withSubmit));
                }
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
