package com.sos.joc.dailyplan;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.ScrollableResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.joc.Globals;
import com.sos.joc.classes.calendar.FrequencyResolver;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.dailyplan.common.DailyPlanHelper;
import com.sos.joc.dailyplan.common.DailyPlanSchedule;
import com.sos.joc.dailyplan.common.DailyPlanScheduleWorkflow;
import com.sos.joc.dailyplan.common.DailyPlanSettings;
import com.sos.joc.dailyplan.common.PeriodHelper;
import com.sos.joc.dailyplan.common.PeriodResolver;
import com.sos.joc.dailyplan.db.DBBeanReleasedSchedule2DeployedWorkflow;
import com.sos.joc.dailyplan.db.DBLayerDailyPlanProjections;
import com.sos.joc.dailyplan.db.DBLayerDailyPlannedOrders;
import com.sos.joc.dailyplan.db.DBLayerSchedules;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanSubmission;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.DBMissingDataException;
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
import com.sos.joc.model.inventory.common.ConfigurationType;

public class DailyPlanProjections {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanProjections.class);

    private static final String IDENTIFIER = "projection";

    // in months - TODO read from the settings?
    private static final int PLANNED_ENTRIES_AGE = 2;

    private Map<String, Long> workflowsAvg = new HashMap<>();
    private boolean onlyPlanOrderAutomatically = true;

    private String logPrefix;

    public void process(DailyPlanSettings settings) throws Exception {
        logPrefix = String.format("[%s][projections]", settings.getStartMode() == null ? StartupMode.manual : settings.getStartMode());
        if (settings.getProjectionsMonthsAhead() == 0) {
            LOGGER.info(logPrefix + "[skip]getProjectionsMonthsAhead=0");
            return;
        }
        Instant start = Instant.now();
        LOGGER.info(logPrefix + "start...");

        DBLayerDailyPlanProjections dbLayer = null;
        try {
            dbLayer = new DBLayerDailyPlanProjections(Globals.createSosHibernateStatelessConnection(IDENTIFIER));

            // 1- delete all entries
            cleanup(dbLayer);

            // 2- evaluate already planned daily plan entries
            DailyPlanResult dpr = calculatePlanned(settings, dbLayer);

            // 3 - evaluate projections(after the last planned daily plan entry)
            calculateProjections(settings, dbLayer, dpr);

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

    // 2- already planned
    private DailyPlanResult calculatePlanned(DailyPlanSettings settings, DBLayerDailyPlanProjections dbLayer) {
        Instant start = Instant.now();
        String lp = logPrefix + "[calculatePlanned]";
        LOGGER.info(lp + "start...");

        java.util.Calendar now = DailyPlanHelper.getUTCCalendarNow();
        java.util.Calendar from = DailyPlanHelper.add2Clone(now, java.util.Calendar.MONTH, -1 * PLANNED_ENTRIES_AGE);

        // only for current year ???
        if (DailyPlanHelper.getYear(from) < DailyPlanHelper.getYear(now)) {
            // set to <currentYear>-01-01
            from = DailyPlanHelper.getFirstDayOfYearCalendar(from, DailyPlanHelper.getYear(now));
        }

        DailyPlanResult result = new DailyPlanResult();
        MetaItem mi = null;
        YearsItem yi = null;
        try {
            List<DBItemDailyPlanSubmission> l = dbLayer.getSubmissions(from.getTime());
            if (l.size() > 0) {
                mi = new MetaItem();
                yi = new YearsItem();

                Map<String, Set<String>> scheduleOrders = new HashMap<>();

                for (DBItemDailyPlanSubmission s : l) {
                    // database- ordered by submissionFordate, otherwise order with java ...
                    result.lastDate = s.getSubmissionForDate();

                    ScrollableResults sr = null;
                    try {
                        // TODO manage cyclic jobs more performantly
                        sr = dbLayer.getDailyPlanOrdersBySubmission(s.getId());
                        if (sr != null) {
                            Set<String> cyclic = new HashSet<>();
                            while (sr.next()) {
                                DBItemDailyPlanOrder item = (DBItemDailyPlanOrder) sr.get(0);

                                if (item.isCyclic()) {
                                    String cyclicMainPart = OrdersHelper.getCyclicOrderIdMainPart(item.getOrderId());
                                    if (cyclic.contains(cyclicMainPart)) {
                                        continue;
                                    }

                                    cyclic.add(cyclicMainPart);
                                }

                                setPlannedMeta(mi, item, scheduleOrders);
                                setPlannedYears(yi, item, result.lastDate);
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
        result.year = yi;

        LOGGER.info(lp + "[end][" + result.lastDate + "]" + SOSDate.getDuration(start, Instant.now()));
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
        dp.setPeriod(p);
        di.getPeriods().add(dp);

        yi.setAdditionalProperty(year, msi);
    }

    // 3 - merge planned and year projections
    private void calculateProjections(DailyPlanSettings settings, DBLayerDailyPlanProjections dbLayer, DailyPlanResult dpr) throws Exception {

        Instant start = Instant.now();
        String lp = logPrefix + "[calculateProjections]";
        LOGGER.info(lp + "start...");

        List<DBBeanReleasedSchedule2DeployedWorkflow> schedules2workflows = new DBLayerSchedules(dbLayer.getSession())
                .getReleasedSchedule2DeployedWorkflows(null, null);

        // tmp log
        LOGGER.info(lp + "[schedules2workflows]" + SOSDate.getDuration(start, Instant.now()));

        if (schedules2workflows.size() > 0) {
            DBLayerDailyPlannedOrders dbLayerPlannedOrders = new DBLayerDailyPlannedOrders(dbLayer.getSession());
            Collection<DailyPlanSchedule> dailyPlanSchedules = convert4projections(dbLayerPlannedOrders, schedules2workflows,
                    onlyPlanOrderAutomatically, dpr);

            if (dailyPlanSchedules.size() > 0) {
                // TODO insertMeta later ?? - filter unused schedules/workflows ...
                // dbLayer.insertMeta(getMeta(dailyPlanSchedules, dpr));

                // current implementation - calculates "from/to" from the current date
                // alternative (not implemented) - calculates "from/to" from the last planned date (dpr.lastDate) if set
                java.util.Calendar from = DailyPlanHelper.getUTCCalendarNow();
                java.util.Calendar to = DailyPlanHelper.add2Clone(from, java.util.Calendar.MONTH, settings.getProjectionsMonthsAhead());

                int yearFrom = DailyPlanHelper.getYear(from);
                int yearTo = DailyPlanHelper.getYear(to);

                for (int i = yearFrom; i <= yearTo; i++) {
                    java.util.Calendar reallyDateFrom = null;
                    YearsItem plannedYearsItem = null;

                    String dateFrom = i + "-01-01";
                    String dateTo = i + "-12-31";
                    if (i == yearFrom) {
                        if (dpr == null || dpr.lastDate == null) {
                            reallyDateFrom = from;
                        } else {
                            reallyDateFrom = DailyPlanHelper.getNextDateUTCCalendar(dpr.lastDate);
                        }
                        plannedYearsItem = dpr.year;

                        // dateFrom - <year>-<moth>-01 for day of month etc calculations
                        // reallyDayFrom will be used later to filter entries before it
                        dateFrom = DailyPlanHelper.getFirstDateOfMonth(reallyDateFrom);
                    }

                    // use the last date of year if the projections_ahead settings defined in years (instead of getLastDateOfMonth)
                    // if (!settings.isProjectionsAheadConfiguredAsYears() && i == yearTo) {
                    // dateTo = DailyPlanHelper.getLastDateOfMonth(to);
                    // }

                    if (i == yearTo) {
                        dateTo = DailyPlanHelper.getLastDateOfMonth(to);
                    }

                    String logDateFrom = reallyDateFrom == null ? dateFrom : DailyPlanHelper.getDate(reallyDateFrom);
                    LOGGER.info(String.format("%s[projection][creating]from %s to %s", lp, logDateFrom, dateTo));
                    dbLayer.insert(i, getProjectionYear(settings, dbLayer, dailyPlanSchedules, String.valueOf(i), dateFrom, dateTo, reallyDateFrom,
                            plannedYearsItem));
                }

                dbLayer.insertMeta(getMeta(dailyPlanSchedules, dpr));
            }
        }
        LOGGER.info(lp + "[end]" + SOSDate.getDuration(start, Instant.now()));
    }

    // merge planned and projections
    private MetaItem getMeta(Collection<DailyPlanSchedule> dailyPlanSchedules, DailyPlanResult dpr) {
        MetaItem mi = dpr != null && dpr.meta != null ? dpr.meta : new MetaItem();

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
                sii.setTotalOrders(Long.valueOf(getTotalOrders(s)));

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

    // merge planned and projections
    private YearsItem getProjectionYear(DailyPlanSettings settings, DBLayerDailyPlanProjections dbLayer,
            Collection<DailyPlanSchedule> dailyPlanSchedules, String year, String dateFrom, String dateTo, java.util.Calendar reallyDayFrom,
            YearsItem plannedYearItem) throws Exception {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        String lp = logPrefix + "[getProjectionYear]";
        String caller = IDENTIFIER + "-" + dateFrom + "-" + dateTo;

        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s%s", lp, caller));
        }

        Map<String, Calendar> workingCalendars = new HashMap<String, Calendar>();
        Map<String, Calendar> nonWorkingCalendars = new HashMap<String, Calendar>();

        MonthsItem msi = null;
        if (plannedYearItem != null) {
            msi = plannedYearItem.getAdditionalProperties().get(year);
        }
        if (msi == null) {
            msi = new MonthsItem();
        }

        InventoryDBLayer dbLayerInventory = new InventoryDBLayer(dbLayer.getSession());
        for (DailyPlanSchedule dailyPlanSchedule : dailyPlanSchedules) {
            Schedule schedule = dailyPlanSchedule.getSchedule();

            final Set<String> nonWorkingDays = DailyPlanHelper.getNonWorkingDays(caller, dbLayerInventory, schedule.getNonWorkingDayCalendars(),
                    dateFrom, dateTo, nonWorkingCalendars);

            for (AssignedCalendars assignedCalendar : schedule.getCalendars()) {
                if (assignedCalendar.getTimeZone() == null) {
                    assignedCalendar.setTimeZone(DailyPlanHelper.UTC);
                }
                final ZoneId timezone = ZoneId.of(assignedCalendar.getTimeZone());

                String calendarsKey = assignedCalendar.getCalendarName();// + "#" + schedule.getPath();
                Calendar calendar = workingCalendars.get(calendarsKey);
                if (calendar == null) {
                    try {
                        calendar = getWorkingDaysCalendar(dbLayerInventory, assignedCalendar.getCalendarName());
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("%s[WorkingDaysCalendar=%s][db]%s", lp, assignedCalendar.getCalendarName(), SOSString.toString(
                                    calendar)));
                        }
                    } catch (DBMissingDataException e) {
                        LOGGER.warn(String.format("%s[WorkingDaysCalendar=%s][skip]not found", lp, assignedCalendar.getCalendarName()));
                        continue;
                    }
                    workingCalendars.put(calendarsKey, calendar);
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[WorkingDaysCalendar=%s][cache]%s", lp, assignedCalendar.getCalendarName(), SOSString.toString(
                                calendar)));
                    }
                }

                Calendar restrictions = new Calendar();
                restrictions.setIncludes(assignedCalendar.getIncludes());

                List<String> dates = new FrequencyResolver().resolveRestrictions(calendar, restrictions, dateFrom, dateTo).getDates();
                boolean checkDate = true;
                for (String date : dates) {
                    String[] arr = date.split("-");
                    String m = arr[0] + "-" + arr[1];
                    if (checkDate) {
                        if (reallyDayFrom == null) {
                            checkDate = false;
                        } else {
                            java.util.Calendar dateCal = FrequencyResolver.getCalendarFromString(date);
                            if (dateCal.after(reallyDayFrom)) {
                                checkDate = false;
                            } else {
                                continue;
                            }
                        }
                    }

                    MonthItem mi = msi.getAdditionalProperties().get(m);
                    if (mi == null) {
                        mi = new MonthItem();
                        msi.getAdditionalProperties().put(m, mi);
                    }

                    DateItem di = mi.getAdditionalProperties().get(date);
                    if (di == null) {
                        di = new DateItem();
                        mi.getAdditionalProperties().put(date, di);
                    }
                    di.setPlanned(null);

                    List<Period> pl = PeriodHelper.getPeriods(assignedCalendar.getPeriods(), nonWorkingDays.stream().collect(Collectors.toList()),
                            date, timezone);

                    int total = getTotalOrders(schedule);
                    for (Period p : pl) {
                        for (int t = 0; t < total; t++) {
                            DatePeriodItem dp = new DatePeriodItem();
                            dp.setSchedule(schedule.getPath());
                            dp.setPeriod(p);

                            di.getPeriods().add(dp);
                        }
                    }
                    // PeriodResolver pr = createPeriodResolver(settings, assignedCalendar.getPeriods(), date, assignedCalendar.getTimeZone());
                    // pr.getStartTimes(date, dateTo, caller)
                }
            }
        }

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

    private Calendar getWorkingDaysCalendar(InventoryDBLayer dbLayer, String calendarName) throws Exception {
        DBItemInventoryReleasedConfiguration config = dbLayer.getReleasedConfiguration(calendarName, ConfigurationType.WORKINGDAYSCALENDAR);
        if (config == null) {
            throw new DBMissingDataException(String.format("calendar '%s' not found", calendarName));
        }
        Calendar calendar = Globals.objectMapper.readValue(config.getContent(), Calendar.class);
        calendar.setId(config.getId());
        calendar.setPath(config.getPath());
        calendar.setName(config.getName());
        return calendar;
    }

    // schedule orders*workflows
    private int getTotalOrders(DailyPlanSchedule s) {
        if (s == null || s.getSchedule() == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[getTotalOrders][orders]total=0"));
            }
            return 0;
        }
        return getTotalOrders(s.getSchedule());
    }

    private int getTotalOrders(Schedule s) {
        int o = s.getOrderParameterisations() != null && s.getOrderParameterisations().size() > 1 ? s.getOrderParameterisations().size() : 1;
        int w = s.getWorkflowNames() != null && s.getWorkflowNames().size() > 1 ? s.getWorkflowNames().size() : 1;
        int t = o * w;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("%s[getTotalOrders][schedule=%s][orders=%s,workflows=%s]total=%s", logPrefix, s.getPath(), o, w, t));
        }
        return t;
    }

    @SuppressWarnings("unused")
    private PeriodResolver createPeriodResolver(DailyPlanSettings settings, List<Period> periods, String date, String timeZone) throws Exception {
        PeriodResolver pr = new PeriodResolver(settings);
        for (Period p : periods) {
            Period period = new Period();
            period.setBegin(p.getBegin());
            period.setEnd(p.getEnd());
            period.setRepeat(p.getRepeat());
            period.setSingleStart(p.getSingleStart());
            period.setWhenHoliday(p.getWhenHoliday());
            try {
                pr.addStartTimes(period, date, timeZone);
            } catch (Throwable e) {
                throw new Exception(String.format("[%s][timeZone=%s][%s]%s", date, timeZone, DailyPlanHelper.toString(period), e.toString()), e);
            }
        }
        return pr;
    }

    private class DailyPlanResult {

        private MetaItem meta;
        private YearsItem year;

        private Date lastDate;

    }
}
