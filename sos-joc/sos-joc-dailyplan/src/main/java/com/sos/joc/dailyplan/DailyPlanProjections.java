package com.sos.joc.dailyplan;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.hibernate.ScrollableResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.joc.Globals;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
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
import com.sos.joc.db.dailyplan.DBItemDailyPlanSubmission;
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

    // in months - TODO read from the settings?
    private static final int PLANNED_ENTRIES_AGE = 2;

    private Map<String, Long> workflowsAvg = new HashMap<>();
    private Map<String, ScheduleOrderCounter> totalOrders = new HashMap<>();
    private boolean onlyPlanOrderAutomatically = true;

    private String logPrefix;

    public void process(DailyPlanSettings settings) throws Exception {
        String add = DailyPlanHelper.getCallerForLog(settings);
        logPrefix = String.format("[%s]%s[projections]", settings.getStartMode(), add);
        if (settings.getProjectionsMonthAhead() == 0) {
            LOGGER.info(logPrefix + "[skip]projections_month_ahead=0");
            return;
        }
        Instant start = Instant.now();
        LOGGER.info(String.format("%s[dailyplan time_zone=%s, period_begin=%s, projections_month_ahead=%s]start...", logPrefix, settings
                .getTimeZone(), settings.getPeriodBegin(), settings.getProjectionsMonthAhead()));

        DBLayerDailyPlanProjections dbLayer = null;
        try {
            dbLayer = new DBLayerDailyPlanProjections(Globals.createSosHibernateStatelessConnection(IDENTIFIER));

            // 1- delete all entries
            cleanup(dbLayer);

            // 2- evaluate already planned daily plan entries
            DailyPlanResult dpr = calculatePlanned(settings, dbLayer);

            // 3 - evaluate projections(after the last planned daily plan entry) and insert merged planned and projections
            if (!calculateProjectionsAndInsertMerged(settings, dbLayer, dpr)) {
                // 4 - insert planned only
                insertPlannedOnly(dbLayer, dpr);
            }

            dbLayer.close();
            dbLayer = null;
        } catch (Throwable e) {
            throw e;
        } finally {
            DBLayer.close(dbLayer);
            LOGGER.info(logPrefix + "[end]" + SOSDate.getDuration(start, Instant.now()));
        }
    }

    // 1-cleanup
    private void cleanup(DBLayerDailyPlanProjections dbLayer) throws Exception {
        Instant start = Instant.now();
        String lp = logPrefix + "[cleanup]";
        boolean autoCommit = dbLayer.getSession().getFactory().getAutoCommit();
        try {
            LOGGER.info(lp + "start...");
            dbLayer.getSession().setAutoCommit(false);
            dbLayer.beginTransaction();
            dbLayer.cleanup();
            dbLayer.commit();
        } catch (Throwable e) {
            dbLayer.rollback();
            throw e;
        } finally {
            dbLayer.getSession().setAutoCommit(autoCommit);
            LOGGER.info(lp + "[end]" + SOSDate.getDuration(start, Instant.now()));
        }
    }

    // 2- already planned - calculates only for the "currentYear"
    private DailyPlanResult calculatePlanned(DailyPlanSettings settings, DBLayerDailyPlanProjections dbLayer) {
        Instant start = Instant.now();
        String lp = logPrefix + "[calculatePlanned]";
        LOGGER.info(lp + "start...");

        java.util.Calendar now = DailyPlanHelper.getUTCCalendarNow();
        java.util.Calendar from = DailyPlanHelper.add2Clone(now, java.util.Calendar.MONTH, -1 * PLANNED_ENTRIES_AGE);

        int currentYear = DailyPlanHelper.getYear(now);
        if (DailyPlanHelper.getYear(from) < currentYear) {
            // set to <currentYear>-01-01
            from = DailyPlanHelper.getFirstDayOfYearCalendar(from, currentYear);
        }

        DailyPlanResult result = new DailyPlanResult();
        result.plannedFrom = from.getTime();
        MetaItem mi = null;
        YearsItem yi = null;
        try {
            List<DBItemDailyPlanSubmission> l = dbLayer.getSubmissions(result.plannedFrom);
            if (l.size() > 0) {
                mi = new MetaItem();
                yi = new YearsItem();

                Map<String, Set<String>> scheduleOrders = new HashMap<>();

                for (DBItemDailyPlanSubmission s : l) {
                    // database- ordered by submissionFordate, otherwise order with java ...
                    result.plannedLastDate = s.getSubmissionForDate();
                    if (result.plannedFirstDate == null) {
                        result.plannedFirstDate = result.plannedLastDate;
                    }

                    ScrollableResults<DBItemDailyPlanOrder> sr = null;
                    try {
                        // TODO manage cyclic jobs more performantly
                        sr = dbLayer.getDailyPlanOrdersBySubmission(s.getId());
                        if (sr != null) {
                            Set<String> cyclic = new HashSet<>();
                            while (sr.next()) {
                                DBItemDailyPlanOrder item = sr.get();

                                if (item.isCyclic()) {
                                    String cyclicMainPart = OrdersHelper.getCyclicOrderIdMainPart(item.getOrderId());
                                    if (cyclic.contains(cyclicMainPart)) {
                                        continue;
                                    }

                                    cyclic.add(cyclicMainPart);
                                }

                                setPlannedMeta(mi, item, scheduleOrders);
                                setPlannedYears(yi, item, result.plannedLastDate);
                            }
                        }
                    } catch (Throwable e) {
                        LOGGER.info(lp + e.toString(), e);
                    } finally {
                        if (sr != null) {
                            sr.close();
                        }
                    }
                }
            }
        } catch (Throwable e) {
            LOGGER.info(lp + e.toString(), e);
        }
        result.meta = mi;
        result.plannedYear = yi;
        result.currentYear = currentYear;

        LOGGER.info(lp + "[end][plannedFrom=" + result.plannedFrom + ", plannedLastDate=" + result.plannedLastDate + "]" + SOSDate.getDuration(start,
                Instant.now()));
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

    private void setPlannedYears(YearsItem yi, DBItemDailyPlanOrder item, Date submissionDate) throws Exception {
        String dateTime = SOSDate.getDateTimeAsString(item.getPlannedStart());
        String submissionTime = SOSDate.getDateTimeAsString(submissionDate);
        String[] arr = submissionTime.split(" ")[0].split("-");
        String year = arr[0];
        String month = year + "-" + arr[1];
        String date = month + "-" + arr[2];

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

        Period p = new Period();
        if (item.getRepeatInterval() == null) {
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

        yi.setAdditionalProperty(year, msi);
    }

    // 3 - merge planned and year projections
    private boolean calculateProjectionsAndInsertMerged(DailyPlanSettings settings, DBLayerDailyPlanProjections dbLayer, DailyPlanResult dpr)
            throws Exception {

        Instant start = Instant.now();
        String lp = logPrefix + "[calculateProjections]";
        LOGGER.info(lp + "start...");

        List<DBBeanReleasedSchedule2DeployedWorkflow> schedules2workflows = new DBLayerSchedules(dbLayer.getSession())
                .getReleasedSchedule2DeployedWorkflows(null, null);

        boolean calculated = false;
        if (schedules2workflows.size() > 0) {
            DBLayerDailyPlannedOrders dbLayerPlannedOrders = new DBLayerDailyPlannedOrders(dbLayer.getSession());
            Collection<DailyPlanSchedule> dailyPlanSchedules = convert4projections(dbLayerPlannedOrders, schedules2workflows,
                    onlyPlanOrderAutomatically, dpr);

            if (dailyPlanSchedules.size() > 0) {
                calculated = true;
                // TODO insertMeta later ?? - filter unused schedules/workflows ...
                // dbLayer.insertMeta(getMeta(dailyPlanSchedules, dpr));

                // current implementation - calculates "from/to" from the current date
                // alternative (not implemented) - calculates "from/to" from the last planned date (dpr.lastDate) if set
                java.util.Calendar fromNow = DailyPlanHelper.getUTCCalendarNow();
                java.util.Calendar to = DailyPlanHelper.add2Clone(fromNow, java.util.Calendar.MONTH, settings.getProjectionsMonthAhead());

                int yearFrom = DailyPlanHelper.getYear(fromNow);
                int yearTo = DailyPlanHelper.getYear(to);

                for (int i = yearFrom; i <= yearTo; i++) {
                    // java.util.Calendar reallyDateFrom = null;
                    YearsItem plannedLastYear = null;

                    String dateFrom = i + "-01-01";
                    String dateTo = i + "-12-31";
                    if (i == yearFrom) {
                        plannedLastYear = dpr.plannedYear;
                        if (dpr.plannedLastDate == null) {
                            dateFrom = DailyPlanHelper.getDate(fromNow);
                        } else {
                            // next day after last planned date
                            dateFrom = DailyPlanHelper.getDate(DailyPlanHelper.getNextDateUTCCalendar(dpr.plannedLastDate));
                        }
                        // dateFrom - <year>-<moth>-01 for day of month etc calculations
                        // reallyDayFrom will be used later to filter entries before it
                        // dateFrom = DailyPlanHelper.getFirstDateOfMonth(reallyDateFrom);
                    }

                    if (i == yearTo) {
                        dateTo = DailyPlanHelper.getLastDateOfMonth(to);
                    }

                    LOGGER.info(String.format("%s[projection][creating]from %s to %s", lp, dateFrom, dateTo));
                    dbLayer.insert(i, getProjectionYear(settings, dbLayer, dailyPlanSchedules, String.valueOf(i), dateFrom, dateTo, plannedLastYear));
                }

                dbLayer.insertMeta(getMeta(dailyPlanSchedules, dpr));
            }
        }
        LOGGER.info(lp + "[end]" + SOSDate.getDuration(start, Instant.now()));
        return calculated;
    }

    private void insertPlannedOnly(DBLayerDailyPlanProjections dbLayer, DailyPlanResult dpr) throws Exception {
        if (dpr == null) {
            return;
        }
        if (dpr.plannedLastDate != null && dpr.meta != null) {
            LOGGER.info(String.format("%s[planned][creating]from %s to %s", logPrefix, dpr.plannedFirstDate, dpr.plannedLastDate));
            dbLayer.insert(dpr.currentYear, dpr.plannedYear);
            dbLayer.insertMeta(dpr.meta);
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
    private MetaItem getMeta(Collection<DailyPlanSchedule> dailyPlanSchedules, DailyPlanResult dpr) {
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
                ScheduleOrderCounter oc = getScheduleOrderCounter(s);
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

    private YearsItem getProjectionYear(DailyPlanSettings settings, DBLayerDailyPlanProjections dbLayer,
            Collection<DailyPlanSchedule> dailyPlanSchedules, String year, String dateFrom, String dateTo, YearsItem plannedLastYear)
            throws Exception {

        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        String lp = logPrefix + "[getProjectionYear]";
        String caller = IDENTIFIER + "-" + dateFrom + "-" + dateTo;

        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s%s", lp, caller));
        }

        MonthsItem msi = null;
        if (plannedLastYear != null) {
            msi = plannedLastYear.getAdditionalProperties().get(year);
        }
        if (msi == null) {
            msi = new MonthsItem();
        }

        settings.setStartMode(StartupMode.automatic);
        final DailyPlanRunner runner = new DailyPlanRunner(settings);
        final AtomicReference<MonthsItem> msiRef = new AtomicReference<>(msi);
        SOSDate.getDatesInRange(dateFrom, dateTo).stream().forEach(asDailyPlanSingleDate -> {
            // SOSDate.getDatesInRange(dateFrom, dateTo).stream().parallel().forEach(asDailyPlanSingleDate -> {
            try {
                settings.setDailyPlanDate(SOSDate.getDate(asDailyPlanSingleDate));

                DBItemDailyPlanSubmission dummySubmission = new DBItemDailyPlanSubmission();
                dummySubmission.setId(-1L);
                dummySubmission.setSubmissionForDate(settings.getDailyPlanDate());

                OrderListSynchronizer synchronizer = runner.calculateAbsoluteMainPeriods(settings.getStartMode(), "controllerId", dailyPlanSchedules,
                        asDailyPlanSingleDate, dummySubmission);

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
        });

        YearsItem result = new YearsItem();
        result.setAdditionalProperty(year, msi);
        return result;
    }

    private Collection<DailyPlanSchedule> convert4projections(DBLayerDailyPlannedOrders dbLayerPlannedOrders,
            List<DBBeanReleasedSchedule2DeployedWorkflow> items, boolean onlyPlanOrderAutomatically, DailyPlanResult dpr) throws Exception {

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
                    if (onlyPlanOrderAutomatically && !schedule.getPlanOrderAutomatically()) {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format(
                                    "%s[skip][schedule=%s][onlyPlanOrderAutomatically=true]schedule.getPlanOrderAutomatically=false", lp, schedule
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
                w.setAvg(getWorkflowAvg(dbLayerPlannedOrders, w, dpr, dps.getSchedule()));
                releasedSchedules.put(key, dps);
            }
        }
        return releasedSchedules.entrySet().stream().map(e -> e.getValue()).collect(Collectors.toList());
    }

    // in seconds
    private Long getWorkflowAvg(DBLayerDailyPlannedOrders dbLayer, DailyPlanScheduleWorkflow w, DailyPlanResult dpr, Schedule schedule)
            throws SOSHibernateException {
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
                result = dbLayer.getWorkflowAvg(w.getControllerId(), w.getPath());
            }
            workflowsAvg.put(key, result);
        }
        return result;
    }

    // Meta
    private ScheduleOrderCounter getScheduleOrderCounter(DailyPlanSchedule s) {
        if (s == null || s.getSchedule() == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[getScheduleOrderCounter][orders]total,schedule=0"));
            }
            return new ScheduleOrderCounter(null, 0);
        }
        return getScheduleOrderCounter(s.getSchedule());
    }

    // Projections
    private ScheduleOrderCounter getScheduleOrderCounter(Schedule s) {
        if (totalOrders.containsKey(s.getPath())) {
            return totalOrders.get(s.getPath());
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
            LOGGER.debug(String.format("%s[getTotalOrders][schedule=%s][orders=%s,workflows=%s]total=%s", logPrefix, s.getPath(), o, w, t));
        }
        ScheduleOrderCounter c = new ScheduleOrderCounter(orderNames, t);
        totalOrders.put(s.getPath(), c);
        return c;
    }

    private class DailyPlanResult {

        private MetaItem meta;
        private YearsItem plannedYear;

        private int currentYear;
        private Date plannedFrom;
        private Date plannedFirstDate;
        private Date plannedLastDate;
    }
}
