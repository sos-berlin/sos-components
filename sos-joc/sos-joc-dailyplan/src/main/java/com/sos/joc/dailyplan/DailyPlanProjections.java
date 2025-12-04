package com.sos.joc.dailyplan;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.hibernate.ScrollableResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.joc.Globals;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.dailyplan.common.AbsoluteMainPeriod;
import com.sos.joc.dailyplan.common.DailyPlanHelper;
import com.sos.joc.dailyplan.common.DailyPlanSchedule;
import com.sos.joc.dailyplan.common.DailyPlanScheduleWorkflow;
import com.sos.joc.dailyplan.common.DailyPlanSettings;
import com.sos.joc.dailyplan.common.ScheduleOrderCounter;
import com.sos.joc.dailyplan.db.DBBeanReleasedSchedule2DeployedWorkflow;
import com.sos.joc.dailyplan.db.DBLayerDailyPlanProjections;
import com.sos.joc.dailyplan.db.DBLayerDailyPlannedOrders;
import com.sos.joc.dailyplan.db.DBLayerSchedules;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanProjection;
import com.sos.joc.db.dailyplan.DBItemDailyPlanSubmission;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.dailyplan.DailyPlanProjectionEvent;
import com.sos.joc.model.cluster.common.state.JocClusterState;
import com.sos.joc.model.dailyplan.projections.items.meta.ControllerInfoItem;
import com.sos.joc.model.dailyplan.projections.items.meta.MetaItem;
import com.sos.joc.model.dailyplan.projections.items.meta.ScheduleInfoItem;
import com.sos.joc.model.dailyplan.projections.items.meta.WorkflowItem;
import com.sos.joc.model.dailyplan.projections.items.meta.WorkflowsItem;
import com.sos.joc.model.dailyplan.projections.items.year.DateItem;
import com.sos.joc.model.dailyplan.projections.items.year.DatePeriodItem;
import com.sos.joc.model.dailyplan.projections.items.year.MonthItem;
import com.sos.joc.model.dailyplan.projections.items.year.MonthsItem;
import com.sos.joc.model.dailyplan.projections.items.year.YearsItem;

public class DailyPlanProjections {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanProjections.class);

    private static final String IDENTIFIER = "projection";

    private static final ReentrantLock LOCK = new ReentrantLock();
    private static final DailyPlanProjections INSTANCE = new DailyPlanProjections();
    // currently hard-coded – may be variable later...
    private static final boolean ONLY_PLAN_ORDER_AUTOMATICALLY = true;

    private static final String MSG_ABORTED = "process aborted by priority request";

    private Map<String, Long> workflowsAvg = new HashMap<>();
    private Map<String, ScheduleOrderCounter> schedulesOrders = new HashMap<>();

    // Cheap atomic read; negligible unless millions+ calls/sec
    private final AtomicBoolean prioRequested = new AtomicBoolean(false);

    private DailyPlanProjections() {
    }

    public static DailyPlanProjections getInstance() {
        return INSTANCE;
    }

    public JocClusterState process(DailyPlanSettings settings) throws Exception {
        String add = DailyPlanHelper.getCallerForLog(settings);
        String logPrefix = String.format("[%s]%s[projections]", settings.getStartMode(), add);

        if (!LOCK.tryLock()) { // already in progress
            if (settings.isWebservice()) {
                // the current process was called by the recreate projections web service - ignore it
                LOGGER.info(logPrefix + "[skip]process is already in progress - new request ignored");
            } else {
                // the current process was called by the DailyPlanRunner service
                // - handle/cancel the projection generation as it will be restarted by the DailyPlanRunner due to DailyPlanRunner has the latest daily plan
                // -- see DailyPlanRunner.recreateProjectionsByService
                if (prioRequested.get()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(logPrefix + "[prio]process is already in progress - waiting for cancellation to complete before restart");
                    }
                } else {
                    LOGGER.info(logPrefix + "[prio]process is already in progress - requesting cancellation, will restart after completion");
                    prioRequested.set(true);
                }
            }
            return JocClusterState.ALREADY_RUNNING;
        }

        if (settings.getProjectionsMonthAhead() == 0) {
            LOGGER.info(logPrefix + "[skip]projections_month_ahead=0");
            return JocClusterState.MISSING_CONFIGURATION;
        }
        Instant start = Instant.now();
        LOGGER.info(String.format("%s[dailyplan time_zone=%s, period_begin=%s, projections_month_ahead=%s]start...", logPrefix, settings
                .getTimeZone(), settings.getPeriodBegin(), settings.getProjectionsMonthAhead()));

        try {
            // 1 - cleanup instance properties
            cleanup();

            // 2 - delete all projection entries in the database
            dbCleanup(logPrefix);

            if (prioRequested.get()) {
                LOGGER.info(getAbortedMsg(logPrefix, "process"));
                return JocClusterState.UNCOMPLETED;
            }

            // 3 - evaluate already planned daily plan entries
            DailyPlanResult dprPlanned = calculatePlanned(logPrefix, settings);

            if (prioRequested.get()) {
                LOGGER.info(getAbortedMsg(logPrefix, "process"));
                return JocClusterState.UNCOMPLETED;
            }

            // 4 - evaluate projections(after the last planned daily plan entry) and insert merged planned and projections
            if (!calculateProjectionsAndInsertMerged(logPrefix, settings, dprPlanned)) {
                if (prioRequested.get()) {
                    LOGGER.info(getAbortedMsg(logPrefix, "process"));
                    return JocClusterState.UNCOMPLETED;
                }

                // 5 - insert planned only
                dbInsertPlannedOnly(logPrefix, dprPlanned);
            }

            if (prioRequested.get()) {
                return JocClusterState.UNCOMPLETED;
            }

        } catch (Exception e) {
            throw e;
        } finally {
            cleanup();
            LOGGER.info(logPrefix + "[end]" + SOSDate.getDuration(start, Instant.now()));

            LOCK.unlock();
        }
        return JocClusterState.COMPLETED;
    }

    private void cleanup() {
        workflowsAvg.clear();
        schedulesOrders.clear();
        prioRequested.set(false);
    }

    // step 1-cleanup
    private void dbCleanup(String logPrefix) throws Exception {
        Instant start = Instant.now();
        String lp = logPrefix + "[dbCleanup]";

        LOGGER.info(lp + "start...");
        if (prioRequested.get()) {
            LOGGER.info(getAbortedMsg(logPrefix, "dbCleanup"));
            return;
        }
        int deleted = 0;
        try (SOSHibernateSession session = Globals.createSosHibernateStatelessConnection(IDENTIFIER + "-dbCleanup")) {
            boolean autoCommit = session.isAutoCommit();
            // set to false to temporarily disable the JOC autocommit=true
            // - otherwise a deletion without a transaction is not possible
            session.setAutoCommit(false);
            session.beginTransaction();
            try {
                deleted = session.executeUpdate("delete from " + DBLayer.DBITEM_DPL_PROJECTIONS);
                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw e;
            } finally {
                session.setAutoCommit(autoCommit);
            }
        } finally {
            LOGGER.info(lp + "[end][deleted=" + deleted + "]" + SOSDate.getDuration(start, Instant.now()));
        }
    }

    /** step 2- already planned<br/>
     * from - from now - first day of month settings.getProjectionsMonthBefore((configurable value, e.g=2) months ago<br>
     * - e.g. now=2025-08-25, from=2025-06-01(first day of 2025-06)<br/>
     * to - from now - last day of month settings.getProjectionsMonthAhead(configurable value, e.g=6) months ahead<br>
     * - e.g. now=2025-08-25, to=2026-02-28(last day of 2026-02)<br/>
     */
    private DailyPlanResult calculatePlanned(String logPrefix, DailyPlanSettings settings) {
        Instant start = Instant.now();
        String lp = logPrefix + "[calculatePlanned]";

        java.util.Calendar now = DailyPlanHelper.getUTCCalendarNow();
        // from - first day of month settings.getProjectionsMonthBefore() months ago
        java.util.Calendar from = DailyPlanHelper.add2Clone(now, java.util.Calendar.MONTH, -1 * settings.getProjectionsMonthBefore());
        from.set(Calendar.DAY_OF_MONTH, 1);

        // to - last day of month settings.getProjectionsMonthAhead() months ahead
        java.util.Calendar to = DailyPlanHelper.add2Clone(now, java.util.Calendar.MONTH, settings.getProjectionsMonthAhead());
        to.set(Calendar.DAY_OF_MONTH, to.getActualMaximum(Calendar.DAY_OF_MONTH));
        to.set(Calendar.HOUR_OF_DAY, 23);
        to.set(Calendar.MINUTE, 59);
        to.set(Calendar.SECOND, 59);
        to.set(Calendar.MILLISECOND, 999);

        LOGGER.info(String.format("%s[calculatePlanned][start]from=%s, to=%s", logPrefix, SOSDate.tryGetDateTimeAsString(from.getTime()), SOSDate
                .tryGetDateTimeAsString(to.getTime())));

        DailyPlanResult result = new DailyPlanResult();
        result.plannedFrom = from.getTime();
        result.plannedTo = to.getTime();
        result.plannedDates = new HashSet<>();

        if (prioRequested.get()) {
            LOGGER.info(getAbortedMsg(logPrefix, "calculatePlanned"));
            return result;
        }

        MetaItem mi = null;
        YearsItem yi = null;
        DBLayerDailyPlanProjections dbLayer = null;
        try {
            dbLayer = new DBLayerDailyPlanProjections(Globals.createSosHibernateStatelessConnection(IDENTIFIER + "-calculatePlanned"));
            List<DBItemDailyPlanSubmission> l = dbLayer.getSubmissions(result.plannedFrom, result.plannedTo);
            if (l.size() > 0) {
                mi = new MetaItem();
                yi = new YearsItem();

                Map<String, Boolean> submissionHasOrders = new HashMap<>();
                Map<String, Set<String>> scheduleOrders = new HashMap<>();

                for (DBItemDailyPlanSubmission s : l) {
                    if (prioRequested.get()) {
                        LOGGER.info(getAbortedMsg(logPrefix, "calculatePlanned stopped at submission " + getSubmissionInfo(s)));
                        return result;
                    }

                    // database- ordered by submissionFordate, otherwise order with java ...
                    result.plannedLastDate = s.getSubmissionForDate();
                    if (result.plannedFirstDate == null) {
                        result.plannedFirstDate = result.plannedLastDate;
                    }

                    String submissionDateTime = SOSDate.getDateTimeAsString(result.plannedLastDate);
                    int orders = 0;
                    try (ScrollableResults<DBItemDailyPlanOrder> sr = dbLayer.getDailyPlanOrdersBySubmission(s.getId())) {
                        if (prioRequested.get()) {
                            LOGGER.info(getAbortedMsg(logPrefix, "calculatePlanned stopped at submission " + getSubmissionInfo(s) + " orders"));
                            return result;
                        }
                        // TODO manage cyclic jobs more performantly
                        if (sr != null) {
                            Set<String> cyclic = new HashSet<>();
                            while (sr.next()) {
                                orders++;
                                DBItemDailyPlanOrder item = sr.get();

                                if (item.isCyclic()) {
                                    String cyclicMainPart = OrdersHelper.getCyclicOrderIdMainPart(item.getOrderId());
                                    if (cyclic.contains(cyclicMainPart)) {
                                        continue;
                                    }

                                    cyclic.add(cyclicMainPart);
                                }

                                setPlannedMeta(mi, item, scheduleOrders);
                                setPlannedYears(submissionDateTime, result.plannedDates, yi, item);
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.info(lp + e.toString(), e);
                    }

                    // to identify 0 orders submissions
                    Boolean hasOrders = submissionHasOrders.get(submissionDateTime);
                    if (hasOrders == null) {
                        // new entry
                        submissionHasOrders.put(submissionDateTime, orders > 0);
                    } else if (!hasOrders && orders > 0) {
                        // only overwrite entry if previously was 'false'
                        submissionHasOrders.put(submissionDateTime, Boolean.valueOf(true));
                    }
                }
                dbLayer.close();
                dbLayer = null;

                // handle 0 orders submissions
                for (Map.Entry<String, Boolean> e : submissionHasOrders.entrySet()) {
                    if (e.getValue()) {
                        continue;
                    }
                    setPlannedYears(e.getKey(), result.plannedDates, yi, null);
                }
                submissionHasOrders.clear();

            }
        } catch (Exception e) {
            LOGGER.info(lp + e.toString(), e);
        } finally {
            DBLayer.close(dbLayer);
        }
        result.meta = mi;
        result.plannedYears = yi;

        LOGGER.info(lp + "[end][first submission=" + SOSDate.tryGetDateTimeAsString(result.plannedFirstDate) + ", last submission=" + SOSDate
                .tryGetDateTimeAsString(result.plannedLastDate) + "]" + SOSDate.getDuration(start, Instant.now()));
        return result;
    }

    private void setPlannedMeta(MetaItem mi, DBItemDailyPlanOrder item, Map<String, Set<String>> scheduleOrders) {
        ControllerInfoItem cii = mi.getAdditionalProperties().get(item.getControllerId());
        if (cii == null) {
            cii = new ControllerInfoItem();
            mi.getAdditionalProperties().put(item.getControllerId(), cii);
        }

        ScheduleInfoItem sii = cii.getAdditionalProperties().get(item.getSchedulePath());
        if (sii == null) {
            sii = new ScheduleInfoItem();
            cii.getAdditionalProperties().put(item.getSchedulePath(), sii);
        }

        // orders count - is overwritten by projections, but the calculation should still be performed(if a "planned" scheduler was removed and is no longer
        // available for projections)
        String soKey = item.getOrderName() + "DELIMITER" + item.getWorkflowName();
        Set<String> so = scheduleOrders.get(item.getScheduleName());
        if (so == null) {
            so = new HashSet<>();
        }
        if (!so.contains(soKey)) {
            so.add(soKey);
        }
        sii.setTotalOrders(Long.valueOf(so.size()));
        sii.setOrderNames(null);
        scheduleOrders.put(item.getScheduleName(), so);

        WorkflowsItem wsi = sii.getWorkflows();
        if (wsi == null) {
            wsi = new WorkflowsItem();
            sii.setWorkflows(wsi);
        }
        WorkflowItem wi = wsi.getAdditionalProperties().get(item.getWorkflowPath());
        if (wi == null) {
            wi = new WorkflowItem();
            wsi.getAdditionalProperties().put(item.getWorkflowPath(), wi);
        }
        // overwrites with the latest avg
        if (item.getExpectedEnd() != null && item.getPlannedStart() != null) {
            // in seconds
            wi.setAvg((item.getExpectedEnd().getTime() - item.getPlannedStart().getTime()) / 1_000);
        }
    }

    private void setPlannedYears(String submissionDateTime, Set<String> plannedDates, YearsItem yi, DBItemDailyPlanOrder item) throws Exception {
        String[] arr = submissionDateTime.split(" ")[0].split("-");
        String year = arr[0];
        String month = year + "-" + arr[1];
        String date = month + "-" + arr[2];

        plannedDates.add(date);

        MonthsItem msi = yi.getAdditionalProperties().get(year);
        if (msi == null) {
            msi = new MonthsItem();
        }

        MonthItem mi = msi.getAdditionalProperties().get(month);
        if (mi == null) {
            mi = new MonthItem();
            msi.getAdditionalProperties().put(month, mi);
        }

        DateItem di = mi.getAdditionalProperties().get(date);
        if (di == null) {
            di = new DateItem();
            mi.getAdditionalProperties().put(date, di);
        }
        di.setPlanned(true);

        if (item != null) { // can be null if submission exists but all orders removed
            Period p = new Period();
            if (item.getRepeatInterval() == null) {
                String dateTime = SOSDate.getDateTimeAsString(item.getPlannedStart());
                p.setSingleStart(DailyPlanHelper.toZonedUTCDateTime(dateTime));
            } else {
                p.setBegin(DailyPlanHelper.toZonedUTCDateTimeCyclicPeriod(date, item.getPeriodBegin()));
                if (!item.getPeriodBegin().before(item.getPeriodEnd())) {
                    String nextDate = LocalDate.parse(date).plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
                    p.setEnd(DailyPlanHelper.toZonedUTCDateTimeCyclicPeriod(nextDate, item.getPeriodEnd()));
                } else {
                    p.setEnd(DailyPlanHelper.toZonedUTCDateTimeCyclicPeriod(date, item.getPeriodEnd()));
                }
                p.setRepeat(SOSDate.getTimeAsString(item.getRepeatInterval()));
            }
            p.setWhenHoliday(null);// ?

            DatePeriodItem dp = new DatePeriodItem();
            dp.setSchedule(item.getSchedulePath());
            // schedule order
            if (!item.getScheduleName().equals(item.getOrderName())) {
                dp.setScheduleOrderName(item.getOrderName());
            }
            dp.setWorkflow(item.getWorkflowPath());
            dp.setPeriod(p);

            di.getPeriods().add(dp);
        }
        yi.setAdditionalProperty(year, msi);
    }

    // 3 - merge planned and projections
    private boolean calculateProjectionsAndInsertMerged(String logPrefix, DailyPlanSettings settings, DailyPlanResult dprPlanned) throws Exception {
        Instant start = Instant.now();
        String lp = logPrefix + "[calculateProjectionsAndInsertMerged]";
        LOGGER.info(lp + "start...");

        List<DBBeanReleasedSchedule2DeployedWorkflow> schedules2workflows = null;
        try (SOSHibernateSession session = Globals.createSosHibernateStatelessConnection(IDENTIFIER + "-getReleasedSchedule2DeployedWorkflows")) {
            schedules2workflows = new DBLayerSchedules(session).getReleasedSchedule2DeployedWorkflows(null, null);
        }

        boolean calculated = false;
        if (SOSCollection.isEmpty(schedules2workflows)) {
            LOGGER.info(String.format("%s[skip]no released schedules found", lp));
        } else {
            Collection<DailyPlanSchedule> dailyPlanSchedules = convert4projections(logPrefix, schedules2workflows, dprPlanned);

            if (dailyPlanSchedules.size() > 0) {
                calculated = true;
                // TODO insertMeta later ?? - filter unused schedules/workflows ...
                // dbLayer.insertMeta(getMeta(dailyPlanSchedules, dpr));

                java.util.Calendar now = DailyPlanHelper.getUTCCalendarNow();
                java.util.Calendar to = DailyPlanHelper.add2Clone(now, java.util.Calendar.MONTH, settings.getProjectionsMonthAhead());

                int yearFrom = DailyPlanHelper.getYear(now);
                int yearTo = DailyPlanHelper.getYear(to);
                Set<String> plannedDates = dprPlanned.plannedDates;
                YearsItem yearsItem = dprPlanned.plannedYears;

                for (int i = yearFrom; i <= yearTo; i++) {
                    if (prioRequested.get()) {
                        LOGGER.info(getAbortedMsg(logPrefix, "calculateProjectionsAndInsertMerged"));
                        return false;
                    }

                    String dateFrom = i + "-01-01";
                    String dateTo = i + "-12-31";
                    if (i == yearFrom) {
                        dateFrom = DailyPlanHelper.getDate(now);
                    }
                    if (i == yearTo) {
                        dateTo = DailyPlanHelper.getLastDateOfMonth(to);
                    }

                    LOGGER.info(String.format("%s[creating][%s]from %s, to %s", lp, i, dateFrom, dateTo));
                    // yearsItem - planned entries are adjusted/merged with projections entries by getProjectionYear
                    dbInsertMonthly(logPrefix, getProjectionYear(logPrefix, settings, dailyPlanSchedules, String.valueOf(i), dateFrom, dateTo,
                            yearsItem, plannedDates));
                }

                dbInsertMeta(logPrefix, getMeta(logPrefix, dailyPlanSchedules, dprPlanned));
            } else {
                LOGGER.info(String.format("%s[skip][schedules total=%s]no PlanOrderAutomatically=true schedules found", lp, schedules2workflows
                        .size()));
            }
        }
        LOGGER.info(lp + "[end]" + SOSDate.getDuration(start, Instant.now()));

        return calculated;
    }

    private void dbInsertMeta(String logPrefix, MetaItem o) throws Exception {
        if (o == null) {
            return;
        }

        if (prioRequested.get()) {
            LOGGER.info(getAbortedMsg(logPrefix, "dbInsertMeta"));
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            try {
                LOGGER.debug(String.format("%s[dbInsertMeta]%s", logPrefix, Globals.objectMapper.writeValueAsString(o)));
            } catch (Throwable e) {
            }
        }
        // prepare item for quick insertion
        DBItemDailyPlanProjection item = new DBItemDailyPlanProjection();
        item.setId(DBItemDailyPlanProjection.METADATEN_ID);
        item.setContent(Globals.objectMapper.writeValueAsBytes(o));
        item.setCreated(new Date());

        try (SOSHibernateSession session = Globals.createSosHibernateStatelessConnection(IDENTIFIER + "-dbInsertMeta")) {
            session.save(item);
        }
        EventBus.getInstance().post(new DailyPlanProjectionEvent());
    }

    /** store monthly not yearly */
    private void dbInsertMonthly(String logPrefix, YearsItem yearsItem) throws Exception {
        if (yearsItem == null) {
            return;
        }

        if (prioRequested.get()) {
            LOGGER.info(getAbortedMsg(logPrefix, "dbInsertMonthly"));
            return;
        }

        String lp = logPrefix + "[dbInsertMonthly]";
        Date created = new Date();
        List<DBItemDailyPlanProjection> items = new ArrayList<>();
        // 1 - prepare items for quick insertion
        for (Map.Entry<String, MonthsItem> yearEntry : yearsItem.getAdditionalProperties().entrySet()) {
            // String year = yearEntry.getKey();
            m: for (Map.Entry<String, MonthItem> monthEntry : yearEntry.getValue().getAdditionalProperties().entrySet()) {
                if (prioRequested.get()) {
                    LOGGER.info(getAbortedMsg(logPrefix, "dbInsertMonthly stopped at " + monthEntry.getKey()));
                    return;
                }

                MonthItem monthItem = monthEntry.getValue();
                if (monthItem == null || SOSCollection.isEmpty(monthItem.getAdditionalProperties())) {
                    continue m;
                }
                Long yearMonth = Long.valueOf(monthEntry.getKey().replace("-", ""));

                if (LOGGER.isDebugEnabled()) {
                    try {
                        LOGGER.debug(String.format("%s[prepare][%s]%s", lp, yearMonth, Globals.objectMapper.writeValueAsString(monthItem)));
                    } catch (Throwable e) {
                    }
                }

                DBItemDailyPlanProjection item = new DBItemDailyPlanProjection();
                item.setId(yearMonth);
                item.setContent(Globals.objectMapper.writeValueAsBytes(monthItem));
                item.setCreated(created);

                items.add(item);
            }
        }

        // 2 - create session only if necessary
        if (items.size() > 0) {
            try (SOSHibernateSession session = Globals.createSosHibernateStatelessConnection(IDENTIFIER + "-dbInsertMonthly")) {
                for (DBItemDailyPlanProjection item : items) {
                    if (prioRequested.get()) {
                        LOGGER.info(getAbortedMsg(logPrefix, "dbInsertMonthly save stopped at " + item.getId()));
                        return;
                    }
                    session.save(item);
                }
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("%s[inserted]items=%s", lp, items.size()));
        }
    }

    private void dbInsertPlannedOnly(String logPrefix, DailyPlanResult dprPlanned) throws Exception {
        if (dprPlanned == null) {
            return;
        }
        if (dprPlanned.plannedLastDate != null && dprPlanned.meta != null) {
            LOGGER.info(String.format("%s[dbInsertPlannedOnly]from %s to %s", logPrefix, dprPlanned.plannedFirstDate, dprPlanned.plannedLastDate));
            dbInsertMonthly(logPrefix, dprPlanned.plannedYears);
            dbInsertMeta(logPrefix, dprPlanned.meta);
        }
    }

    /** set planned meta schedules as "excludedFromProjection" if a schedule has been removed (or "Plan Order automatically" is deactivated) */
    private MetaItem checkIfPlannedSchedulesExcludedFromProjection(MetaItem mi, Collection<DailyPlanSchedule> dailyPlanSchedules) {
        for (Map.Entry<String, ControllerInfoItem> cii : mi.getAdditionalProperties().entrySet()) {
            for (Map.Entry<String, ScheduleInfoItem> sii : cii.getValue().getAdditionalProperties().entrySet()) {
                DailyPlanSchedule found = dailyPlanSchedules.stream().filter(s -> s.getSchedule().getPath().equals(sii.getKey())).findAny().orElse(
                        null);
                if (found == null) {
                    sii.getValue().setExcludedFromProjection(Boolean.valueOf(true));
                }
            }
        }
        return mi;
    }

    // merge planned and projections
    private MetaItem getMeta(String logPrefix, Collection<DailyPlanSchedule> dailyPlanSchedules, DailyPlanResult dpr) {
        MetaItem mi = dpr != null && dpr.meta != null ? dpr.meta : new MetaItem();
        mi = checkIfPlannedSchedulesExcludedFromProjection(mi, dailyPlanSchedules);

        for (DailyPlanSchedule s : dailyPlanSchedules) {
            Map<String, List<DailyPlanScheduleWorkflow>> perController = s.getWorkflows().stream().collect(Collectors.groupingBy(w -> w
                    .getControllerId()));

            for (Map.Entry<String, List<DailyPlanScheduleWorkflow>> c : perController.entrySet()) {
                ControllerInfoItem cii = mi.getAdditionalProperties().get(c.getKey());
                if (cii == null) {
                    cii = new ControllerInfoItem();
                    mi.getAdditionalProperties().put(c.getKey(), cii);
                }

                ScheduleInfoItem sii = cii.getAdditionalProperties().get(s.getSchedule().getPath());
                if (sii == null) {
                    sii = new ScheduleInfoItem();
                    cii.getAdditionalProperties().put(s.getSchedule().getPath(), sii);
                }
                // overwrite orders (previously (if)set by planned)
                // schedulerOrders * workflows
                ScheduleOrderCounter oc = getScheduleOrderCounter(logPrefix, s);
                sii.setTotalOrders(oc.getTotalAsLong());
                sii.setOrderNames(oc.getOrderNamesNormalized());

                WorkflowsItem wsi = sii.getWorkflows();
                if (wsi == null) {
                    wsi = new WorkflowsItem();
                    sii.setWorkflows(wsi);
                }
                for (DailyPlanScheduleWorkflow w : s.getWorkflows()) {
                    WorkflowItem wi = wsi.getAdditionalProperties().get(w.getPath());
                    if (wi == null) {
                        wi = new WorkflowItem();
                        wsi.getAdditionalProperties().put(w.getPath(), wi);
                    }
                    if (wi.getAvg() == null) {
                        wi.setAvg(w.getAvg());
                    }
                }
            }
        }
        return mi;
    }

    private YearsItem getProjectionYear(String logPrefix, DailyPlanSettings settings, Collection<DailyPlanSchedule> dailyPlanSchedules, String year,
            String dateFrom, String dateTo, YearsItem plannedYears, Set<String> plannedDates) throws Exception {

        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        String lp = logPrefix + "[getProjectionYear]";
        String caller = IDENTIFIER + "-" + dateFrom + "-" + dateTo;

        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s%s", lp, caller));
        }

        MonthsItem msi = null;
        if (plannedYears != null) {
            msi = plannedYears.getAdditionalProperties().get(year);
        }
        if (msi == null) {
            msi = new MonthsItem();
        }

        final DailyPlanRunner runner = new DailyPlanRunner(settings);
        final AtomicReference<MonthsItem> msiRef = new AtomicReference<>(msi);
        SOSDate.getDatesInRange(dateFrom, dateTo).stream().filter(date -> {
            return plannedDates == null || !plannedDates.contains(date);
        }).anyMatch(asDailyPlanSingleDate -> {
            if (prioRequested.get()) {
                LOGGER.info(getAbortedMsg(logPrefix, "getProjectionYear stopped at " + asDailyPlanSingleDate));
                return true; // stop stream
            }

            // SOSDate.getDatesInRange(dateFrom, dateTo).stream().parallel().forEach(asDailyPlanSingleDate -> {
            try {
                settings.setDailyPlanDate(SOSDate.getDate(asDailyPlanSingleDate));

                DBItemDailyPlanSubmission dummySubmission = new DBItemDailyPlanSubmission();
                dummySubmission.setId(-1L);
                dummySubmission.setSubmissionForDate(settings.getDailyPlanDate());

                OrderListSynchronizer synchronizer = runner.calculateAbsoluteMainPeriodsOnlyWithoutIncludeLate(settings.getStartMode(),
                        "controllerId", dailyPlanSchedules, asDailyPlanSingleDate, dummySubmission);

                List<AbsoluteMainPeriod> absPeriods = synchronizer.getAbsoluteMainPeriods();
                if (absPeriods.size() > 0) {
                    String[] arr = asDailyPlanSingleDate.split("-");
                    String m = arr[0] + "-" + arr[1];

                    MonthItem mi = msiRef.get().getAdditionalProperties().get(m);
                    if (mi == null) {
                        mi = new MonthItem();
                        msiRef.get().getAdditionalProperties().put(m, mi);
                    }
                    DateItem di = mi.getAdditionalProperties().get(asDailyPlanSingleDate);
                    if (di == null) {
                        di = new DateItem();
                        di.setPeriods(new ArrayList<>());
                    }
                    di.setPlanned(null);

                    for (AbsoluteMainPeriod ap : absPeriods) {
                        DatePeriodItem dp = new DatePeriodItem();
                        dp.setSchedule(ap.getSchedulePath());
                        dp.setPeriod(ap.getPeriod());
                        di.getPeriods().add(dp);
                    }
                    mi.getAdditionalProperties().put(asDailyPlanSingleDate, di);
                }

            } catch (Exception e) {
                LOGGER.info(lp + "[" + asDailyPlanSingleDate + "]" + e, e);
            }

            return false; // continue stream
        });

        YearsItem result = new YearsItem();
        result.setAdditionalProperty(year, msi);
        return result;
    }

    private Collection<DailyPlanSchedule> convert4projections(String logPrefix, List<DBBeanReleasedSchedule2DeployedWorkflow> items,
            DailyPlanResult dpr) throws Exception {

        String lp = logPrefix + "[convert4projections]";
        boolean isDebugEnabled = LOGGER.isDebugEnabled();

        Map<String, DailyPlanSchedule> releasedSchedules = new HashMap<>();
        for (DBBeanReleasedSchedule2DeployedWorkflow item : items) {
            String key = item.getScheduleName();
            DailyPlanSchedule dps = null;
            if (releasedSchedules.containsKey(key)) {
                dps = releasedSchedules.get(key);
            } else {
                if (SOSString.isEmpty(item.getScheduleContent())) {
                    LOGGER.warn(String.format("%s[skip][content is empty]%s", lp, SOSHibernate.toString(item)));
                    continue;
                }
                Schedule schedule = null;
                try {
                    schedule = Globals.objectMapper.readValue(item.getScheduleContent(), Schedule.class);
                    if (schedule == null) {
                        LOGGER.warn(String.format("%s[skip][schedule is null]%s", lp, SOSHibernate.toString(item)));
                        continue;
                    }
                    schedule.setPath(item.getSchedulePath());
                    if (ONLY_PLAN_ORDER_AUTOMATICALLY && !schedule.getPlanOrderAutomatically()) {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format(
                                    "%s[skip][schedule=%s][ONLY_PLAN_ORDER_AUTOMATICALLY=true]schedule.getPlanOrderAutomatically=false", lp, schedule
                                            .getPath()));
                        }
                        continue;
                    }

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[schedule]%s", lp, schedule.getPath()));
                    }

                    schedule.setPath(item.getSchedulePath());
                    dps = new DailyPlanSchedule(schedule);
                } catch (Throwable e) {
                    LOGGER.error(String.format("%s[%s][exception]%s", lp, SOSHibernate.toString(item), e.toString()), e);
                    continue;
                }
            }
            if (dps != null) {
                DailyPlanScheduleWorkflow w = dps.addWorkflow(new DailyPlanScheduleWorkflow(item.getWorkflowName(), item.getWorkflowPath(), null));
                w.setControllerId(item.getControllerId());
                w.setAvg(getWorkflowAvg(w, dpr, dps.getSchedule()));
                releasedSchedules.put(key, dps);
            }
        }
        return releasedSchedules.entrySet().stream().map(e -> e.getValue()).collect(Collectors.toList());
    }

    // in seconds
    private Long getWorkflowAvg(DailyPlanScheduleWorkflow w, DailyPlanResult dpr, Schedule schedule) throws Exception {
        String key = w.getControllerId() + "DELIMITER" + w.getPath();
        Long result = workflowsAvg.get(key);
        if (result == null) {
            boolean checkDb = true;
            if (dpr != null && dpr.meta != null) {
                ControllerInfoItem cii = dpr.meta.getAdditionalProperties().get(w.getControllerId());
                if (cii != null) {
                    ScheduleInfoItem sii = cii.getAdditionalProperties().get(schedule.getPath());
                    if (sii != null) {
                        if (sii.getWorkflows() != null) {
                            WorkflowItem wi = sii.getWorkflows().getAdditionalProperties().get(w.getPath());
                            if (wi != null) {
                                result = wi.getAvg();// can be null
                                checkDb = false;
                            }
                        }
                    }
                }
            }
            if (checkDb) {
                result = dbGetWorkflowAvg(w);
            }
            workflowsAvg.put(key, result);
        }
        return result;
    }

    private Long dbGetWorkflowAvg(DailyPlanScheduleWorkflow w) throws Exception {
        try (SOSHibernateSession session = Globals.createSosHibernateStatelessConnection(IDENTIFIER + "-dbGetWorkflowAVG-" + w.getPath())) {
            return new DBLayerDailyPlannedOrders(session).getWorkflowAvg(w.getControllerId(), w.getPath());
        }
    }

    // Meta
    private ScheduleOrderCounter getScheduleOrderCounter(String logPrefix, DailyPlanSchedule s) {
        if (s == null || s.getSchedule() == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[getScheduleOrderCounter][orders]total,schedule=0"));
            }
            return new ScheduleOrderCounter(null, 0);
        }
        return getScheduleOrderCounter(logPrefix, s.getSchedule());
    }

    // Projections
    private ScheduleOrderCounter getScheduleOrderCounter(String logPrefix, Schedule s) {
        if (schedulesOrders.containsKey(s.getPath())) {
            return schedulesOrders.get(s.getPath());
        }
        Set<String> orderNames = null;
        if (s.getOrderParameterisations() != null) {
            orderNames = s.getOrderParameterisations().stream().filter(e -> e.getOrderName() != null).map(e -> e.getOrderName()).collect(Collectors
                    .toSet());
        }
        int o = orderNames != null && orderNames.size() > 1 ? orderNames.size() : 1;
        int w = s.getWorkflowNames() != null && s.getWorkflowNames().size() > 1 ? s.getWorkflowNames().size() : 1;
        int t = o * w;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("%s[getScheduleOrderCounter][schedule=%s][orders=%s,workflows=%s]total=%s", logPrefix, s.getPath(), o, w, t));
        }
        ScheduleOrderCounter c = new ScheduleOrderCounter(orderNames, t);
        schedulesOrders.put(s.getPath(), c);
        return c;
    }

    private String getAbortedMsg(String logPrefix, String caller) {
        return String.format("%s[%s]%s", logPrefix, caller, MSG_ABORTED);
    }

    private String getSubmissionInfo(DBItemDailyPlanSubmission s) {
        String sd = SOSDate.tryGetDateTimeAsString(s.getSubmissionForDate());
        String c = SOSDate.tryGetDateTimeAsString(s.getCreated());
        return sd + "(id=" + s.getId() + ", created=" + c + ")";
    }

    private class DailyPlanResult {

        private MetaItem meta;
        private YearsItem plannedYears;

        private Set<String> plannedDates;

        private Date plannedFrom;
        private Date plannedTo;
        private Date plannedFirstDate;
        private Date plannedLastDate;
    }
}
