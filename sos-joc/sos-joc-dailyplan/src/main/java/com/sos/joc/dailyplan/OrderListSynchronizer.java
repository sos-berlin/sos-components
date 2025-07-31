package com.sos.joc.dailyplan;

import java.nio.file.Paths;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.joc.Globals;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.order.OrderTags;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.service.JocClusterServiceLogger;
import com.sos.joc.dailyplan.common.AbsoluteMainPeriod;
import com.sos.joc.dailyplan.common.DailyPlanHelper;
import com.sos.joc.dailyplan.common.DailyPlanSettings;
import com.sos.joc.dailyplan.common.MainCyclicOrderKey;
import com.sos.joc.dailyplan.common.OrderCounter;
import com.sos.joc.dailyplan.common.PlannedOrder;
import com.sos.joc.dailyplan.common.PlannedOrderKey;
import com.sos.joc.dailyplan.db.DBLayerDailyPlannedOrders;
import com.sos.joc.db.dailyplan.DBItemDailyPlanHistory;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanSubmission;
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

    private final DailyPlanSettings settings;
    private final Map<PlannedOrderKey, PlannedOrder> plannedOrders;
    private final String callerForLog;

    private JocError jocError = null;
    private DBItemDailyPlanSubmission submission;
    private String accessToken = null;

    private List<AbsoluteMainPeriod> absoluteMainPeriods = null;
    private Map<MainCyclicOrderKey, Map<Long, Period>> absoluteMainCyclicPeriodsHelper = null;

    public OrderListSynchronizer(DailyPlanSettings settings) {
        this.plannedOrders = new TreeMap<PlannedOrderKey, PlannedOrder>();
        this.settings = settings;
        this.callerForLog = DailyPlanHelper.getCallerForLog(this.settings);
    }
    
    public OrderListSynchronizer(DailyPlanSettings settings, boolean calculateAbsoluteMainPeriodsOnly) {
        this.plannedOrders = new TreeMap<PlannedOrderKey, PlannedOrder>();
        this.settings = settings;
        this.callerForLog = DailyPlanHelper.getCallerForLog(this.settings);
        if (calculateAbsoluteMainPeriodsOnly) {
            this.absoluteMainPeriods = new ArrayList<>();
            this.absoluteMainCyclicPeriodsHelper = new TreeMap<>();
        }
    }

    protected void addAbsoluteMainPeriod(String controllerId, Schedule schedule, Entry<Long, Period> periodEntry) {
        if (periodEntry == null) {
            return;
        }
        if (periodEntry.getValue().getSingleStart() == null) {
            MainCyclicOrderKey key = new MainCyclicOrderKey(controllerId, schedule, periodEntry.getValue());
            absoluteMainCyclicPeriodsHelper.putIfAbsent(key, new HashMap<>());
            absoluteMainCyclicPeriodsHelper.get(key).put(periodEntry.getKey(), periodEntry.getValue());
        } else {
            Period p = new Period();
            p.setWhenHoliday(null);
            p.setSingleStart(DailyPlanHelper.toZonedUTCDateTime(periodEntry.getKey()));
            absoluteMainPeriods.add(new AbsoluteMainPeriod(schedule.getPath(), p));
        }
    }

    public List<AbsoluteMainPeriod> getAbsoluteMainPeriods() {
        if (!absoluteMainCyclicPeriodsHelper.isEmpty()) {
            for (Entry<MainCyclicOrderKey, Map<Long, Period>> entry : absoluteMainCyclicPeriodsHelper.entrySet()) {
                Optional<Long> min = entry.getValue().keySet().stream().min(Long::compareTo);
                Optional<Long> max = entry.getValue().keySet().stream().max(Long::compareTo);
                if (min.isPresent() && max.isPresent()) {
                    Period p = new Period();
                    p.setWhenHoliday(null);
                    p.setRepeat(entry.getValue().values().iterator().next().getRepeat());
                    p.setBegin(DailyPlanHelper.toZonedUTCDateTime(min.get()));
                    p.setEnd(DailyPlanHelper.toZonedUTCDateTime(max.get()));
                    absoluteMainPeriods.add(new AbsoluteMainPeriod(entry.getKey(), p));
                }
            }
            absoluteMainCyclicPeriodsHelper.clear();
        }
        absoluteMainPeriods.sort(Comparator.comparing(period -> {
            String start = period.getPeriod().getSingleStart();
            if (start != null && !start.isEmpty()) {
                return Instant.parse(start);
            }
            String begin = period.getPeriod().getBegin();
            return Instant.parse(begin);
        }));
        return absoluteMainPeriods;
    }

    protected boolean add(StartupMode startupMode, PlannedOrder o, String controllerId, String dailyPlanDate) {
        if (o == null) {
            return false;
        }
        String workflow = null;
        String dateLog = SOSString.isEmpty(dailyPlanDate) ? "" : "[" + dailyPlanDate + "]";
        final String lp = String.format("[%s]%s[%s]%s", startupMode, callerForLog, controllerId, dateLog);
        try {
            workflow = o.getWorkflowName();
            String wpath = WorkflowPaths.getPathOrNull(workflow);
            if (wpath == null) {
                LOGGER.info(String.format("%s[workflow=%s not deployed][skip]%s", lp, workflow, SOSString.toString(o)));
                return false;
            }
            PlannedOrderKey key = o.uniqueOrderKey();
            plannedOrders.put(key, o);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("%s[added][key=%s]%s", lp, key, SOSString.toString(o)));
            }
            return true;
        } catch (Throwable e) {
            LOGGER.error(String.format("%s[workflow=%s][order %s]%s", lp, workflow, SOSString.toString(o), e.toString()), e);
            return false;
        }
    }

    private Map<String, DBItemDailyPlanHistory> insertHistory(SOSHibernateSession session, Set<PlannedOrder> addedOrders)
            throws SOSHibernateException {

        Date submissionTime = settings.getSubmissionTime() == null ? new Date() : settings.getSubmissionTime();

        Map<String, DBItemDailyPlanHistory> result = new HashMap<>();
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
            item.setWorkflowPath(order.getWorkflowPath());
            if (item.getWorkflowPath() != null) {
                String folderName = Paths.get(item.getWorkflowPath()).getParent().toString().replace('\\', '/');
                item.setWorkflowFolder(folderName);
            }
            item.setSubmissionTime(submissionTime);
            item.setUserAccount(settings.getUserAccount());

            session.save(item);
            result.put(item.getOrderId(), item);
        }
        return result;
    }

    public void submitOrdersToController(StartupMode startupMode, String controllerId, String dailyPlanDate) throws SOSHibernateException {

        JocClusterServiceLogger.setLogger(ClusterServices.dailyplan.name());
        String method = "submitOrdersToController";
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        String logDailyPlanDate = SOSString.isEmpty(dailyPlanDate) ? "" : "[" + dailyPlanDate + "]";
        final String lp = String.format("[%s]%s[%s][%s]%s", startupMode, callerForLog, method, controllerId, logDailyPlanDate);
        Set<PlannedOrder> orders = new HashSet<PlannedOrder>();
        for (PlannedOrder p : plannedOrders.values()) {
            if (p.isStoredInDb() && p.getSubmitOrderToControllerWhenPlanned()) {
                orders.add(p);
            } else {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format(
                            "%s[skip because planned order !p.isStoredInDb() or !p.getSchedule().getSubmitOrderToControllerWhenPlanned()]%s", lp,
                            SOSString.toString(p)));
                }
            }
        }

        SOSHibernateSession session = null;
        try {
            String sessionIdentifier = method;
            if (!SOSString.isEmpty(dailyPlanDate)) {
                sessionIdentifier += "-" + dailyPlanDate;
            }

            session = Globals.createSosHibernateStatelessConnection(sessionIdentifier);
            session.setAutoCommit(false);

            Map<String, DBItemDailyPlanHistory> inserted = Collections.emptyMap();
            try {
                Instant start = Instant.now();
                Globals.beginTransaction(session);
                inserted = insertHistory(session, orders);
                Globals.commit(session);
                Globals.disconnect(session); // disconnect here, not wait for the controller operations
                session = null;

                Instant end = Instant.now();
                LOGGER.info(String.format("%s[%s of %s orders]history added=%s(%s)", lp, orders.size(), plannedOrders.size(), inserted.size(), SOSDate
                        .getDuration(start, end)));

                if (orders.size() > 0) {
                    OrderApi.addOrdersToController(startupMode, callerForLog, controllerId, dailyPlanDate, orders, inserted, jocError, accessToken);
                } else {
                    LOGGER.info(String.format("%s[%s of %s orders][skip addOrdersToController]", lp, orders.size(), plannedOrders.size()));
                }

            } catch (Exception e) {
                LOGGER.info(String.format("%s[%s of %s orders]history added=%s", lp, orders.size(), plannedOrders.size(), inserted.size()));
                LOGGER.warn(e.toString(), e);
            }
        } finally {
            Globals.disconnect(session);
        }

    }

    public void substituteOrderIds() {

        ZoneId zoneId = ZoneId.of(settings.getTimeZone());

        Map<MainCyclicOrderKey, List<PlannedOrder>> cyclics = new TreeMap<MainCyclicOrderKey, List<PlannedOrder>>();
        for (PlannedOrder plannedOrder : plannedOrders.values()) {
            if (plannedOrder.getPeriod().getSingleStart() != null) {
                substituteOrderId(plannedOrder, OrdersHelper.getUniqueOrderId(zoneId), 0, 0);
            } else {
                MainCyclicOrderKey key = new MainCyclicOrderKey(plannedOrder);
                cyclics.putIfAbsent(key, new ArrayList<PlannedOrder>());
                cyclics.get(key).add(plannedOrder);
            }
        }

        for (Entry<MainCyclicOrderKey, List<PlannedOrder>> entry : cyclics.entrySet()) {
            int size = entry.getValue().size();
            int nr = 1;
            String id = OrdersHelper.getUniqueOrderId(zoneId);
            for (PlannedOrder plannedOrder : entry.getValue()) {
                substituteOrderId(plannedOrder, id, nr, size);
                nr = nr + 1;
            }
        }
    }

    private void substituteOrderId(PlannedOrder plannedOrder, String id, Integer nr, Integer size) {
        String orderId = plannedOrder.getFreshOrder().getId();
        if (nr != 0) { // cyclic
            String nrAsString = "00000" + String.valueOf(nr);
            nrAsString = nrAsString.substring(nrAsString.length() - 5);

            String sizeAsString = String.valueOf(size);
            orderId = orderId.replaceFirst("<nr.....>", nrAsString).replaceFirst("<size>", sizeAsString);
        }
        plannedOrder.getFreshOrder().setId(orderId.replaceFirst("<id.*>", id));
    }

    private OrderCounter executeStore(StartupMode startupMode, String operation, String controllerId, String date, Map<String, Long> durations)
            throws JocConfigurationException, DBConnectionRefusedException, SOSHibernateException, ParseException, JsonProcessingException {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();

        final String lp = String.format("[%s]%s[%s][%s][%s]", startupMode, callerForLog, operation, controllerId, date);

        SOSHibernateSession session = null;
        OrderCounter counter = new OrderCounter();
        try {
            session = Globals.createSosHibernateStatelessConnection("executeStore-" + date);
            DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
            session.setAutoCommit(false);
            Globals.beginTransaction(session);
            ZoneId zoneId = ZoneId.of(settings.getTimeZone());
            Map<DBItemDailyPlanOrder, Set<String>> orderTags = new HashMap<>();

            Map<MainCyclicOrderKey, List<PlannedOrder>> cyclics = new TreeMap<MainCyclicOrderKey, List<PlannedOrder>>();
            for (PlannedOrder plannedOrder : plannedOrders.values()) {
                if (plannedOrder.getPeriod().getSingleStart() != null) {
                    counter.addSingle();
                    DBItemDailyPlanOrder item = dbLayer.getUniqueDailyPlan(plannedOrder);
                    if (settings.isOverwrite() || item == null) {
                        plannedOrder.setAverageDuration(durations.get(plannedOrder.getWorkflowPath()));
                        item = dbLayer.store(plannedOrder, OrdersHelper.getUniqueOrderId(zoneId), 0, 0);

                        if (plannedOrder.hasTags()) {
                            orderTags.put(item, plannedOrder.getTags());
                        }

                        plannedOrder.setStoredInDb(true);
                        counter.addStoredSingle();
                    } else {
                        counter.addStoreSkippedSingle();
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("%s[store][single][skip][key=%s][isOverwrite=%s][item=%s]", lp, plannedOrder.uniqueOrderKey(),
                                    settings.isOverwrite(), SOSHibernate.toString(item)));
                        }
                    }
                } else {
                    MainCyclicOrderKey key = new MainCyclicOrderKey(plannedOrder);
                    if (cyclics.get(key) == null) {
                        cyclics.put(key, new ArrayList<PlannedOrder>());
                        counter.addCyclic();
                    }
                    cyclics.get(key).add(plannedOrder);
                    counter.addCyclicTotal();
                }
            }

            for (Entry<MainCyclicOrderKey, List<PlannedOrder>> entry : cyclics.entrySet()) {
                int size = entry.getValue().size();
                int nr = 1;
                String id = OrdersHelper.getUniqueOrderId(zoneId);
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[store][cyclic]size=%s, order id main part=%s, key=%s", lp, size, id, entry.getKey()));
                }
                for (PlannedOrder plannedOrder : entry.getValue()) {
                    DBItemDailyPlanOrder item = dbLayer.getUniqueDailyPlan(plannedOrder);
                    if (settings.isOverwrite() || item == null) {
                        plannedOrder.setAverageDuration(durations.get(plannedOrder.getWorkflowPath()));
                        item = dbLayer.store(plannedOrder, id, nr, size);

                        if (plannedOrder.hasTags()) {
                            orderTags.put(item, plannedOrder.getTags());
                        }

                        nr = nr + 1;
                        plannedOrder.setStoredInDb(true);
                        counter.addStoredCyclicTotal();
                    } else {
                        counter.addStoreSkippedCyclicTotal();

                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("%s[store][cyclic][skip][%s][isOverwrite=%s][item=%s]", lp, plannedOrder.uniqueOrderKey(),
                                    settings.isOverwrite(), SOSHibernate.toString(item)));
                        }
                    }
                }
            }

            OrderTags.addDailyPlanOrderTags(controllerId, orderTags);

            if (!counter.hasStored() && submission != null && submission.getId() != null) {
                Long count = dbLayer.getCountOrdersBySubmissionId(controllerId, submission.getId());
                if (count == null || count.equals(0L)) {
                    dbLayer.deleteSubmission(submission.getId());
                }
            }

            Globals.commit(session);
        } finally {
            Globals.disconnect(session);
        }

        LOGGER.info(String.format("%s[stored]%s", lp, counter));
        return counter;
    }

    public void addPlannedOrderToControllerAndDB(StartupMode startupMode, String operation, String controllerId, String date, Boolean withSubmit,
            Map<String, Long> durations) throws JocConfigurationException, DBConnectionRefusedException, SOSHibernateException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException, DBOpenSessionException,
            DBInvalidDataException, ExecutionException, JsonProcessingException, ParseException {

        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        String method = "addPlannedOrderToControllerAndDB";
        final String lp = String.format("[%s]%s[%s][%s][%s]", startupMode, callerForLog, method, controllerId, date);
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%soverwrite orders=%s", lp, settings.isOverwrite()));
        }

        if (settings.isOverwrite()) {
            SOSHibernateSession session = null;
            List<DBItemDailyPlanOrder> orders = new ArrayList<DBItemDailyPlanOrder>();
            try {
                session = Globals.createSosHibernateStatelessConnection(method + "-" + date);
                DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);

                for (PlannedOrder plannedOrder : plannedOrders.values()) {
                    DBItemDailyPlanOrder item = dbLayer.getUniqueDailyPlan(plannedOrder);
                    if (item != null) {
                        orders.add(item);
                    }
                }
            } finally {
                Globals.disconnect(session);
            }
            final boolean log2serviceFile = true;// !StartupMode.manual.equals(startupMode);
            CompletableFuture<Either<Problem, Void>> c = OrdersHelper.removeFromJobSchedulerController(controllerId, orders);
            c.thenAccept(either -> {
                ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
                if (either.isRight()) {
                    if (log2serviceFile) {
                        JocClusterServiceLogger.setLogger(ClusterServices.dailyplan.name());
                    }
                    SOSHibernateSession session4delete = null;
                    try {
                        session4delete = Globals.createSosHibernateStatelessConnection(method + "-" + date);
                        session4delete.setAutoCommit(false);
                        DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session4delete);

                        Set<Long> oldSubmissionIds = new HashSet<>();
                        Set<String> cyclicMainParts = new HashSet<>();
                        for (DBItemDailyPlanOrder item : orders) {
                            if (!oldSubmissionIds.contains(item.getSubmissionHistoryId())) {
                                oldSubmissionIds.add(item.getSubmissionHistoryId());
                            }
                            if (item.getStartMode().equals(DBLayerDailyPlannedOrders.START_MODE_SINGLE)) {
                                Globals.beginTransaction(session4delete);
                                try {
                                    dbLayer.deleteSingleCascading(item);
                                    OrderTags.deleteTagsOfOrder(controllerId, item.getOrderId(), session4delete);
                                    Globals.commit(session4delete);
                                } catch (SOSHibernateException | DBConnectionRefusedException | DBInvalidDataException e1) {
                                    Globals.rollback(session4delete);
                                    throw e1;
                                }
                            } else {
                                String mainPart = OrdersHelper.getCyclicOrderIdMainPart(item.getOrderId());
                                if (!cyclicMainParts.contains(mainPart)) {
                                    cyclicMainParts.add(mainPart);
                                }
                                Globals.beginTransaction(session4delete);
                                try {
                                    dbLayer.delete(item.getId());
                                    OrderTags.deleteTagsOfOrder(controllerId, item.getOrderId(), session4delete);
                                    Globals.commit(session4delete);
                                } catch (SOSHibernateException | DBConnectionRefusedException | DBInvalidDataException e1) {
                                    Globals.rollback(session4delete);
                                    throw e1;
                                }
                            }
                        }
                        
                        Globals.beginTransaction(session4delete);
                        // delete cyclic variables when all cyclic orders deleted
                        for (String cyclicMainPart : cyclicMainParts) {
                            Long count = dbLayer.getCountCyclicOrdersByMainPart(controllerId, cyclicMainPart);
                            if (count == null || count.equals(0L)) {
                                dbLayer.deleteVariablesByCyclicMainPart(controllerId, cyclicMainPart);
                            }
                        }
                        // delete submissions with 0 orders
                        for (Long submissionId : oldSubmissionIds) {
                            Long count = dbLayer.getCountOrdersBySubmissionId(controllerId, submissionId);
                            if (count == null || count.equals(0L)) {
                                dbLayer.deleteSubmission(submissionId);
                            }
                        }
                        Globals.commit(session4delete);

                        executeStore(startupMode, operation, controllerId, date, durations);
                        if (withSubmit == null || withSubmit) {
                            submitOrdersToController(startupMode, controllerId, date);
                        } else {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("%s[skip]withSubmit=%s", lp, withSubmit));
                            }
                        }
                    } catch (SOSHibernateException | JocConfigurationException | DBConnectionRefusedException | ParseException
                            | ControllerConnectionResetException | ControllerConnectionRefusedException | DBMissingDataException
                            | DBOpenSessionException | DBInvalidDataException e) {
                        Globals.rollback(session4delete);
                        ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), controllerId);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    } finally {
                        Globals.disconnect(session4delete);
                    }
                }
            });

        } else {
            executeStore(startupMode, operation, controllerId, date, durations);
            if (withSubmit == null || withSubmit) {
                submitOrdersToController(startupMode, controllerId, date);
            } else {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[skip]withSubmit=%s", lp, withSubmit));
                }
            }
        }
    }

    public void storeAndSubmitPlannedOrder(StartupMode startupMode, String operation, String controllerId, String date, Boolean withSubmit,
            Map<String, Long> durations) throws JocConfigurationException, DBConnectionRefusedException, SOSHibernateException,
            DBMissingDataException, DBOpenSessionException, DBInvalidDataException, ExecutionException, JsonProcessingException, ParseException {

        executeStore(startupMode, operation, controllerId, date, durations);
        if (withSubmit == null || withSubmit) {
            submitOrdersToController(startupMode, controllerId, date);
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
