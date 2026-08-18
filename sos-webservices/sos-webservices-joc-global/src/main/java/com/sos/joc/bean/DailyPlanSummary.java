package com.sos.joc.bean;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.db.dailyplan.DBItemDailyPlanWithHistory;
import com.sos.joc.db.dailyplan.DailyPlanHistoryDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.dailyplan.DailyPlanCalendarEvent;
import com.sos.joc.event.bean.dailyplan.DailyPlanEvent;
import com.sos.joc.event.bean.history.HistoryOrderEvent;
import com.sos.joc.event.bean.history.HistoryOrderStarted;
import com.sos.joc.event.bean.history.HistoryOrderTerminated;
import com.sos.joc.model.order.OrderStateText;

public class DailyPlanSummary implements DailyPlanSummaryMBean, IJocMBean {

    private final String controllerId;
    private double finishedOrders = 0;
    private double plannedOrders = 0;
    private double plannedLateOrders = 0;
    private double submittedOrders = 0;
    private double submittedLateOrders = 0;

    private AtomicBoolean hasOrderEvent = new AtomicBoolean(false);
    private AtomicBoolean initialised = new AtomicBoolean(false);
    private ZoneId zoneId = ZoneOffset.UTC;
    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanSummaryMBean.class);
    private Predicate<String> isMainOrder = oId -> oId == null || !oId.contains("|");
    private Predicate<String> isDailyPlanOrder = oId -> oId == null || oId.contains(".*#[PC][0-9]+-.*");
    private Predicate<DBItemDailyPlanWithHistory> isCancelled = item -> OrderStateText.CANCELLED.intValue() == item.getState();
    private Predicate<DBItemDailyPlanWithHistory> isFinished = item -> OrderStateText.FINISHED.intValue() == item.getState();
    private Predicate<DBItemDailyPlanWithHistory> isComplete = isCancelled.or(isFinished);
    private Predicate<DBItemDailyPlanWithHistory> startTimeIsLate = item -> item.getStartTime().toInstant().isAfter(item.getPlannedStart().toInstant()
            .plusSeconds(DBItemDailyPlanWithHistory.DAILY_PLAN_LATE_TOLERANCE));
    private final static Comparator<DBItemDailyPlanWithHistory> comparator = Comparator.comparing(DBItemDailyPlanWithHistory::getPlannedStart);
    private final static Supplier<TreeSet<DBItemDailyPlanWithHistory>> supplier = () -> new TreeSet<>(comparator);

    public DailyPlanSummary(String controllerId) {
        this.controllerId = controllerId;
        if (Globals.getConfigurationGlobals() != null) {
            init();
        }
        EventBus.getInstance().register(this);
    }

    @Subscribe({ DailyPlanCalendarEvent.class })
    public void init(DailyPlanCalendarEvent evt) {
        init();
    }

    @Subscribe({ HistoryOrderTerminated.class, HistoryOrderStarted.class })
    public void update(HistoryOrderEvent evt) {
        if (initialised.get() && controllerId.equals(evt.getControllerId()) && isMainOrder.and(isDailyPlanOrder).test(evt.getOrderId())) {
            if (!hasOrderEvent.getAndSet(true)) {
                Executors.newScheduledThreadPool(1).schedule(() -> {
                    setDailyPlanSummary();
                    hasOrderEvent.set(false);
                }, 5, TimeUnit.SECONDS);
            }
        }
    }

    @Subscribe({ DailyPlanEvent.class })
    public void update(DailyPlanEvent evt) {
        if (initialised.get() && "DailyPlanUpdated".equals(evt.getKey()) && controllerId.equals(evt.getControllerId()) && !SOSString.isEmpty(evt
                .getDailyPlanDate())) {
            if (LocalDate.now(zoneId).atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE).equals(evt.getDailyPlanDate())) {
                if (!hasOrderEvent.getAndSet(true)) {
                    Executors.newScheduledThreadPool(1).schedule(() -> {
                        setDailyPlanSummary();
                        hasOrderEvent.set(false);
                    }, 5, TimeUnit.SECONDS);
                }
            }
        }
    }

    @Override
    public String objectName() {
        // return getClass().getSimpleName();
        return "dailyplan";
    }
    
    private void setTimezone() {
        zoneId = ZoneId.of(Globals.getConfigurationGlobalsDailyPlan().getTimeZone().getValue());
    }
    
    private void init() {
        setTimezone();
        if (!initialised.get()) {
            setDailyPlanSummary();
            initialised.set(true);
        }
    }

    private void setDailyPlanSummary() {
        SOSHibernateSession connection = null;
        try {
            AtomicInteger finished = new AtomicInteger(0);
            AtomicInteger planned = new AtomicInteger(0);
            AtomicInteger plannedLate = new AtomicInteger(0);
            AtomicInteger submitted = new AtomicInteger(0);
            AtomicInteger submittedLate = new AtomicInteger(0);

            Date submissionDate = Date.from(LocalDate.now(zoneId).atStartOfDay().toInstant(ZoneOffset.UTC));
            Date nowDate = Date.from(Instant.now());
            
            connection = Globals.createSosHibernateStatelessConnection(DailyPlanSummaryMBean.class.getSimpleName());
            DailyPlanHistoryDBLayer jobHistoryDBLayer = new DailyPlanHistoryDBLayer(connection);
            List<DBItemDailyPlanWithHistory> result = jobHistoryDBLayer.getOrdersWithHistoryState(controllerId, submissionDate);
            Globals.disconnect(connection);
            
            result.stream().collect(Collectors.groupingBy(item -> OrdersHelper.getOrderIdMainPart(item.getOrderId()), Collectors.toCollection(
                    supplier))).values().forEach(l -> {
                        if (l.size() == 1) { // single order
                            if (l.first().getState() == null) { // order is not started
                                if (l.first().getPlannedStart().before(nowDate)) {
                                    if (l.first().isSubmitted()) {
                                        submittedLate.getAndIncrement();
                                    } else {
                                        plannedLate.getAndIncrement();
                                    }
                                } else {
                                    if (l.first().isSubmitted()) {
                                        submitted.getAndIncrement();
                                    } else {
                                        planned.getAndIncrement();
                                    }
                                }
                            } else { // order is started
                                if (isComplete.test(l.first())) {
                                    finished.getAndIncrement();
                                } else if (startTimeIsLate.test(l.first())) {
                                    submittedLate.getAndIncrement();
                                } else {
                                    submitted.getAndIncrement();
                                }
                            }
                        } else { // cyclic order
                            if (l.first().getState() == null) { // no cyclic order is started
                                if (l.first().getPlannedStart().before(nowDate)) {
                                    if (l.first().isSubmitted()) {
                                        submittedLate.getAndIncrement();
                                    } else {
                                        plannedLate.getAndIncrement();
                                    }
                                } else {
                                    if (l.first().isSubmitted()) {
                                        submitted.getAndIncrement();
                                    } else {
                                        planned.getAndIncrement();
                                    }
                                }
                            } else if (l.last().getState() == null) { // not all of cyclic order is started
                                l.removeIf(item -> item.getState() != null);
                                if (l.first().getPlannedStart().before(nowDate)) {
                                    submittedLate.getAndIncrement();
                                } else {
                                    submitted.getAndIncrement();
                                }
                            } else if (l.stream().anyMatch(isComplete.negate())) { // not all of cyclic order is complete
                                if (startTimeIsLate.test(l.last())) {
                                    submittedLate.getAndIncrement();
                                } else {
                                    submitted.getAndIncrement();
                                }
                            } else {
                                finished.getAndIncrement();
                            }
                        }
                    });

            int all = finished.get() + planned.get() + plannedLate.get() + submitted.get() + submittedLate.get();
            if (all == 0) {
                finishedOrders = 0;
                plannedLateOrders = 0;
                plannedOrders = 0;
                submittedLateOrders = 0;
                submittedOrders = 0;
            } else {
                finishedOrders = finished.get() * 100 / all;
                plannedLateOrders = plannedLate.get() * 100 / all;
                plannedOrders = planned.get() * 100 / all;
                submittedLateOrders = submittedLate.get() * 100 / all;
                submittedOrders = submitted.get() * 100 / all;
            }

        } catch (Exception e) {
            LOGGER.warn("Error at updating " + objectName() + " metrics: ", e);
        } finally {
            Globals.disconnect(connection);
        }
    }

    @Override
    public double getfinished_orders() {
        return finishedOrders;
    }

    @Override
    public double getplanned_late_orders() {
        return plannedLateOrders;
    }

    @Override
    public double getplanned_orders() {
        return plannedOrders;
    }

    @Override
    public double getsubmitted_late_orders() {
        return submittedLateOrders;
    }

    @Override
    public double getsubmitted_orders() {
        return submittedOrders;
    }

}
