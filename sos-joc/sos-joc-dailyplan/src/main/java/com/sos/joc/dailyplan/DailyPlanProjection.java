package com.sos.joc.dailyplan;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.joc.Globals;
import com.sos.joc.classes.calendar.FrequencyResolver;
import com.sos.joc.dailyplan.common.DailyPlanHelper;
import com.sos.joc.dailyplan.common.DailyPlanSchedule;
import com.sos.joc.dailyplan.common.DailyPlanScheduleWorkflow;
import com.sos.joc.dailyplan.common.DailyPlanSettings;
import com.sos.joc.dailyplan.common.PeriodResolver;
import com.sos.joc.dailyplan.db.DBBeanReleasedSchedule2DeployedWorkflow;
import com.sos.joc.dailyplan.db.DBLayerDailyPlanProjection;
import com.sos.joc.dailyplan.db.DBLayerSchedules;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.model.dailyplan.projection.items.DateItem;
import com.sos.joc.model.dailyplan.projection.items.DatePeriodItem;
import com.sos.joc.model.dailyplan.projection.items.MonthItem;
import com.sos.joc.model.dailyplan.projection.items.MonthsItem;
import com.sos.joc.model.dailyplan.projection.items.YearItem;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class DailyPlanProjection {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanProjection.class);

    private static final String UTC = "Etc/UTC";

    private static final String IDENTIFIER = "projection";

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC);
    private static DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_INSTANT;

    private boolean onlyPlanOrderAutomatically = true;

    public void process(DailyPlanSettings settings) throws Exception {

        DBLayerDailyPlanProjection dbLayer = null;

        try {
            dbLayer = new DBLayerDailyPlanProjection(Globals.createSosHibernateStatelessConnection(IDENTIFIER));

            boolean autoCommit = dbLayer.getSession().getFactory().getAutoCommit();
            try {
                dbLayer.getSession().setAutoCommit(false);

                dbLayer.beginTransaction();
                dbLayer.cleanup();
                dbLayer.commit();
            } catch (Throwable e) {
                dbLayer.rollback();
                throw e;
            } finally {
                dbLayer.getSession().setAutoCommit(autoCommit);
            }

            PlannedResult pr = planned(dbLayer);

            List<DBBeanReleasedSchedule2DeployedWorkflow> schedule2workflow = new DBLayerSchedules(dbLayer.getSession())
                    .getReleasedSchedule2DeployedWorkflows(null, null);

            if (schedule2workflow.size() > 0) {
                Collection<DailyPlanSchedule> dailyPlanSchedules = convert(schedule2workflow, onlyPlanOrderAutomatically, true);

                if (dailyPlanSchedules.size() > 0) {
                    java.util.Calendar now = DailyPlanHelper.getCalendar(null, UTC);
                    java.util.Calendar to = DailyPlanHelper.getCalendar(null, UTC);
                    to.add(java.util.Calendar.YEAR, 2);// from settings
                    int yearFrom = now.get(java.util.Calendar.YEAR);
                    int yearTo = to.get(java.util.Calendar.YEAR);

                    LOGGER.info(yearFrom + "-" + yearTo);

                    for (int i = yearFrom; i < yearTo; i++) {
                        YearItem plannedYearItem = null;
                        String dateFrom = i + "-01-01";
                        java.util.Calendar reallyDayFrom = null;
                        if (i == yearFrom) {
                            if (pr.lastDate == null) {
                                reallyDayFrom = now;
                            } else {
                                reallyDayFrom = pr.getNextDateAfterLastDate();
                            }
                            plannedYearItem = pr.year;
                            dateFrom = getFirstDayOfMonth(reallyDayFrom); // <year>-<moth>-01 for day of month etc calculation
                        }
                        String dateTo = i + "-12-31";

                        dbLayer.insert(i, yearProjection(settings, dbLayer, dailyPlanSchedules, String.valueOf(i), dateFrom, dateTo, reallyDayFrom,
                                plannedYearItem));
                    }
                }
            }

            dbLayer.close();
            dbLayer = null;
        } catch (Throwable e) {
            throw e;
        } finally {
            DBLayer.close(dbLayer);
        }
    }

    private String getFirstDayOfMonth(java.util.Calendar cal) {
        java.util.Calendar copy = java.util.Calendar.getInstance(TimeZone.getTimeZone(UTC));
        copy.setTime(cal.getTime());
        copy.set(java.util.Calendar.DAY_OF_MONTH, 1);
        return dateFormatter.format(copy.toInstant());
    }

    private java.util.Calendar getNextDayCalendar(String dateISO) throws Exception {
        java.util.Calendar cal = FrequencyResolver.getCalendarFromString(dateISO);
        cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
        return cal;
    }

    // already planned
    private PlannedResult planned(DBLayerDailyPlanProjection dbLayer) {
        PlannedResult r = new PlannedResult();

        // TODO read submissions etc

        return r;
    }

    private YearItem yearProjection(DailyPlanSettings settings, DBLayerDailyPlanProjection dbLayer, Collection<DailyPlanSchedule> dailyPlanSchedules,
            String year, String dateFrom, String dateTo, java.util.Calendar reallyDayFrom, YearItem plannedYearItem) throws Exception {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        String caller = IDENTIFIER + "-" + dateFrom + "-" + dateTo;

        if (isDebugEnabled) {
            LOGGER.debug(caller);
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
                    assignedCalendar.setTimeZone(UTC);
                }
                final ZoneId timezone = ZoneId.of(assignedCalendar.getTimeZone());

                String calendarsKey = assignedCalendar.getCalendarName() + "#" + schedule.getPath();
                Calendar calendar = workingCalendars.get(calendarsKey);
                if (calendar == null) {
                    try {
                        calendar = getWorkingDaysCalendar(dbLayerInventory, assignedCalendar.getCalendarName());
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[WorkingDaysCalendar=%s][db]%s", assignedCalendar.getCalendarName(), SOSString.toString(
                                    calendar)));
                        }
                    } catch (DBMissingDataException e) {
                        LOGGER.warn(String.format("[WorkingDaysCalendar=%s][skip]not found", assignedCalendar.getCalendarName()));
                        continue;
                    }
                    workingCalendars.put(calendarsKey, calendar);
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[WorkingDaysCalendar=%s][cache]%s", assignedCalendar.getCalendarName(), SOSString.toString(
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

                    List<Period> pl = getPeriods(assignedCalendar.getPeriods(), nonWorkingDays.stream().collect(Collectors.toList()), date, timezone)
                            .sorted(Comparator.comparing(p -> p.getSingleStart() == null ? p.getBegin() : p.getSingleStart())).collect(Collectors
                                    .toList());
                    for (Period p : pl) {
                        DatePeriodItem dp = new DatePeriodItem();
                        dp.setSchedule(schedule.getPath());
                        dp.setPeriod(p);

                        di.getPeriods().add(dp);
                    }
                    // PeriodResolver pr = createPeriodResolver(settings, assignedCalendar.getPeriods(), date, assignedCalendar.getTimeZone());

                    // pr.getStartTimes(date, dateTo, caller)
                }
            }
        }

        YearItem result = new YearItem();
        result.setAdditionalProperty(year, msi);
        return result;
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

    private Collection<DailyPlanSchedule> convert(List<DBBeanReleasedSchedule2DeployedWorkflow> items, boolean onlyPlanOrderAutomatically,
            boolean infoOnMissingDeployedWorkflow) {

        String method = "convert";
        boolean isDebugEnabled = LOGGER.isDebugEnabled();

        Map<String, DailyPlanSchedule> releasedSchedules = new HashMap<>();
        for (DBBeanReleasedSchedule2DeployedWorkflow item : items) {
            String key = item.getScheduleName();
            DailyPlanSchedule dps = null;
            if (releasedSchedules.containsKey(key)) {
                dps = releasedSchedules.get(key);
            } else {
                if (SOSString.isEmpty(item.getScheduleContent())) {
                    LOGGER.warn(String.format("[%s][skip][content is empty]%s", method, SOSHibernate.toString(item)));
                    continue;
                }
                Schedule schedule = null;
                try {
                    schedule = Globals.objectMapper.readValue(item.getScheduleContent(), Schedule.class);
                    if (schedule == null) {
                        LOGGER.warn(String.format("[%s][skip][schedule is null]%s", method, SOSHibernate.toString(item)));
                        continue;
                    }
                    schedule.setPath(item.getSchedulePath());
                    if (onlyPlanOrderAutomatically && !schedule.getPlanOrderAutomatically()) {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format(
                                    "[%s][skip][schedule=%s][onlyPlanOrderAutomatically=true]schedule.getPlanOrderAutomatically=false", method,
                                    schedule.getPath()));
                        }
                        continue;
                    }

                    schedule.setPath(item.getSchedulePath());
                    dps = new DailyPlanSchedule(schedule);
                } catch (Throwable e) {
                    LOGGER.error(String.format("[%s][%s][exception]%s", method, SOSHibernate.toString(item), e.toString()), e);
                    continue;
                }
            }
            if (dps != null) {
                dps.addWorkflow(new DailyPlanScheduleWorkflow(item.getWorkflowName(), item.getWorkflowPath(), null));
                releasedSchedules.put(key, dps);
            }
        }
        return releasedSchedules.entrySet().stream().map(e -> e.getValue()).collect(Collectors.toList());
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

    private static Stream<Period> getPeriods(List<Period> periods, List<String> holidays, String date, ZoneId timezone) {
        if (periods == null) {
            return Stream.empty();
        }
        return periods.stream().map(p -> getPeriod(p, holidays, date, timezone)).filter(Objects::nonNull);
    }

    private static Period getPeriod(Period period, List<String> holidays, String date, ZoneId timezone) {
        Period p = new Period();

        if (holidays.contains(date)) {
            if (period.getWhenHoliday() != null) {
                switch (period.getWhenHoliday()) {
                case SUPPRESS:
                    return null;
                case NEXTNONWORKINGDAY:
                    try {
                        java.util.Calendar dateCal = FrequencyResolver.getCalendarFromString(date);
                        dateCal.add(java.util.Calendar.DATE, 1);
                        date = dateFormatter.format(dateCal.toInstant());
                        while (holidays.contains(date)) {
                            dateCal.add(java.util.Calendar.DATE, 1);
                            date = dateFormatter.format(dateCal.toInstant());
                        }
                    } catch (SOSInvalidDataException e) {
                        LOGGER.error(String.format("[%s] %s", period.toString(), e.toString()));
                        return null;
                    }
                    break;
                case PREVIOUSNONWORKINGDAY:
                    try {
                        java.util.Calendar dateCal = FrequencyResolver.getCalendarFromString(date);
                        dateCal.add(java.util.Calendar.DATE, -1);
                        date = dateFormatter.format(dateCal.toInstant());
                        while (holidays.contains(date)) {
                            dateCal.add(java.util.Calendar.DATE, -1);
                            date = dateFormatter.format(dateCal.toInstant());
                        }
                    } catch (SOSInvalidDataException e) {
                        LOGGER.error(String.format("[%s] %s", period.toString(), e.toString()));
                        return null;
                    }
                    break;
                case IGNORE:
                    break;
                }
            } else {
                return null;
            }
        }

        if (period.getSingleStart() != null) {
            p.setSingleStart(isoFormatter.format(ZonedDateTime.of(LocalDateTime.parse(date + "T" + normalizeTime(period.getSingleStart()),
                    dateTimeFormatter), timezone)));
            return p;
        }
        if (period.getRepeat() != null && !period.getRepeat().isEmpty()) {
            p.setRepeat(period.getRepeat());
            String begin = period.getBegin();
            if (begin == null || begin.isEmpty()) {
                begin = "00:00:00";
            } else {
                begin = normalizeTime(begin);
            }

            p.setBegin(isoFormatter.format(ZonedDateTime.of(LocalDateTime.parse(date + "T" + begin, dateTimeFormatter), timezone)));
            String end = period.getEnd();
            if (end == null || end.isEmpty()) {
                end = "24:00:00";
            } else {
                end = normalizeTime(end);
            }
            if (end.startsWith("24:00")) {
                p.setEnd(isoFormatter.format(ZonedDateTime.of(LocalDateTime.parse(date + "T23:59:59", dateTimeFormatter).plusSeconds(1L), timezone)));
            } else {
                p.setEnd(isoFormatter.format(ZonedDateTime.of(LocalDateTime.parse(date + "T" + end, dateTimeFormatter), timezone)));
            }
            return p;
        }
        return null;
    }

    private static String normalizeTime(String time) {
        String[] ss = (time + ":00:00:00").split(":", 3);
        ss[2] = ss[2].substring(0, 2);
        return String.format("%2s:%2s:%2s", ss[0], ss[1], ss[2]).replace(' ', '0');
    }

    private class PlannedResult {

        private String lastDate;
        private YearItem year;

        public java.util.Calendar getNextDateAfterLastDate() throws Exception {
            if (lastDate == null) {
                return null;
            }
            return getNextDayCalendar(lastDate);
        }
    }

}
