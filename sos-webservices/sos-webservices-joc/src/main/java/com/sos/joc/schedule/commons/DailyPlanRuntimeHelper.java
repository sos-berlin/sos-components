package com.sos.joc.schedule.commons;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.AssignedNonWorkingDayCalendars;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.calendar.WhenHolidayType;
import com.sos.joc.Globals;
import com.sos.joc.classes.calendar.FrequencyResolver;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;

public class DailyPlanRuntimeHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanRuntimeHelper.class);

    public static List<Calendar> getNonWorkingDayCalendars(InventoryDBLayer dbLayer, List<AssignedNonWorkingDayCalendars> nonWorkingDayCalendars)
            throws Exception {
        List<Calendar> r = new ArrayList<>();
        if (!SOSCollection.isEmpty(nonWorkingDayCalendars)) {
            List<DBItemInventoryReleasedConfiguration> items = dbLayer.getReleasedCalendarsByNames(nonWorkingDayCalendars.stream().map(
                    AssignedNonWorkingDayCalendars::getCalendarName).distinct().collect(Collectors.toList()));
            if (!SOSCollection.isEmpty(items)) {
                Map<String, String> nameContentMap = items.stream().collect(Collectors.toMap(DBItemInventoryReleasedConfiguration::getName,
                        DBItemInventoryReleasedConfiguration::getContent));
                for (AssignedNonWorkingDayCalendars c : nonWorkingDayCalendars) {
                    if (!nameContentMap.containsKey(c.getCalendarName())) {
                        continue;
                    }
                    r.add(Globals.objectMapper.readValue(nameContentMap.get(c.getCalendarName()), Calendar.class));
                }
            }
        }
        return r;
    }

    public static void applyAdjustmentForNonWorkingDates(AssignedCalendars workingDayCalendar, List<Calendar> nonWorkingDayCalendars, String date,
            List<String> resolvedWorkingDates, List<String> nonWorkingDates, Set<String> workingDatesExtendedWithNonWorkingPrevNext)
            throws Exception {
        if (workingDayCalendar.getPeriods() == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format(
                        "[applyAdjustmentForNonWorkingDates][date=%s][skip][calendar=%s][resolvedWorkingDates=%s][periods=null]clear resolvedWorkingDates...",
                        date, workingDayCalendar.getCalendarName(), resolvedWorkingDates));
            }
            resolvedWorkingDates.clear();
            return;
        }
        if (SOSCollection.isEmpty(nonWorkingDayCalendars)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[applyAdjustmentForNonWorkingDates][date=%s][skip][calendar=%s]no nonWorkingDayCalendars found", date,
                        workingDayCalendar.getCalendarName()));
            }
            return;
        }
        List<Period> nonEmptyWhenHolidays = workingDayCalendar.getPeriods().stream().filter(p -> p.getWhenHoliday() != null).collect(Collectors
                .toList());
        if (nonEmptyWhenHolidays.size() == 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[applyAdjustmentForNonWorkingDates][date=%s][skip][calendar=%s]no nonEmptyWhenHolidays found", date,
                        workingDayCalendar.getCalendarName()));
            }
            return;
        }

        if (SOSCollection.isEmpty(resolvedWorkingDates)) {
            // date is not in the working calendar/restrictions
            // but date can be a before/after non-working day ...
            x: for (Period p : nonEmptyWhenHolidays) {
                switch (p.getWhenHoliday()) {
                case PREVIOUSNONWORKINGDAY: // before non-working day
                    // date can be a PREV from a non-working date+1day
                    nonWorkingDates.clear();
                    workingDatesExtendedWithNonWorkingPrevNext.clear();
                    nonWorkingDates.addAll(getNonWorkingDates(nonWorkingDayCalendars, date, SOSDate.getNextDate(date)));
                    switch (nonWorkingDates.size()) {
                    case 1:
                        // if date is a non-working day - it can't be PREV
                        if (!nonWorkingDates.contains(date)) {
                            resolvedWorkingDates.add(date);
                            workingDatesExtendedWithNonWorkingPrevNext.add(date);
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug(String.format(
                                        "[applyAdjustmentForNonWorkingDates][date=%s][calendar=%s][resolvedWorkingDates][added][%s]%s", date,
                                        workingDayCalendar.getCalendarName(), p.getWhenHoliday(), date));
                            }
                            break x;
                        }
                        break;
                    default: // skip
                        // 0 - the both dates are not non-working days
                        // 2(>1) - the both dates are non-working days
                        break;
                    }
                    break;
                case NEXTNONWORKINGDAY: // after non-working day
                    // date can be a NEXT from a non-working date-1day
                    nonWorkingDates.clear();
                    workingDatesExtendedWithNonWorkingPrevNext.clear();
                    nonWorkingDates.addAll(getNonWorkingDates(nonWorkingDayCalendars, SOSDate.getPreviousDate(date), date));
                    switch (nonWorkingDates.size()) {
                    case 1:
                        // if date is a non-working day - it can't be NEXT
                        if (!nonWorkingDates.contains(date)) {
                            resolvedWorkingDates.add(date);
                            workingDatesExtendedWithNonWorkingPrevNext.add(date);
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug(String.format(
                                        "[applyAdjustmentForNonWorkingDates][date=%s][calendar=%s][resolvedWorkingDates][added][%s]%s", date,
                                        workingDayCalendar.getCalendarName(), p.getWhenHoliday(), date));
                            }

                            break x;
                        }
                        break;
                    default: // skip
                        // 0 - the both dates are not non-working days
                        // 2(>1) - the both dates are non-working days
                        break;
                    }
                    break;
                case IGNORE: // ignore this - because it's not a working day anyway ...
                case SUPPRESS:
                default:
                    break;
                }
            }
        } else {
            nonWorkingDates.addAll(getNonWorkingDates(nonWorkingDayCalendars, date, date));
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[applyAdjustmentForNonWorkingDates][date=%s][calendar=%s][resolvedWorkingDates=%s]nonWorkingDates=%s", date,
                    workingDayCalendar.getCalendarName(), resolvedWorkingDates, nonWorkingDates));
        }
    }

    public static Stream<Period> getSingleDatePeriods(String date, List<Period> periods, List<String> nonWorkingDates,
            Set<String> workingDatesExtendedWithNonWorkingPrevNext, ZoneId timezone) {
        if (periods == null) {
            return Stream.empty();
        }
        return periods.stream().map(p -> getSingleDatePeriod(date, p, nonWorkingDates, workingDatesExtendedWithNonWorkingPrevNext, timezone)).filter(
                Objects::nonNull);
    }

    // should be called after applyAdjustmentForNonWorkingDates ...
    public static Period getSingleDatePeriod(String date, Period period, List<String> nonWorkingDates,
            Set<String> workingDatesExtendedWithNonWorkingPrevNext, ZoneId timezone) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[getSingleDatePeriod][" + date + "][" + SOSString.toString(period, true) + "][nonWorkingDates=" + nonWorkingDates
                    + "]workingDatesExtendedWithNonWorkingPrevNext=" + workingDatesExtendedWithNonWorkingPrevNext);
        }

        // 1) Check WhenHoliday
        if (period.getWhenHoliday() == null) {
            period.setWhenHoliday(WhenHolidayType.SUPPRESS); // ???
        }
        switch (period.getWhenHoliday()) {
        case SUPPRESS:
            // is a non-working day or date was added because multiple periods with different WhenHoliday
            if (nonWorkingDates.contains(date) || workingDatesExtendedWithNonWorkingPrevNext.contains(date)) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("   [skip][" + period.getWhenHoliday() + "][" + date
                            + "]is in nonWorkingDates or workingDatesExtendedWithNonWorkingPrevNext");
                }

                return null;
            }
            break;
        case NEXTNONWORKINGDAY:
        case PREVIOUSNONWORKINGDAY:
            if (nonWorkingDates.contains(date)) { // the date can't be NEXT/PREV because the date is a non-working day
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("   [skip][" + period.getWhenHoliday() + "][" + date + "]is in nonWorkingDates");
                }
                return null;
            }
            break;
        default:// IGNORE
            // is a non-working day and date was added because multiple periods with different WhenHoliday
            if (workingDatesExtendedWithNonWorkingPrevNext.contains(date)) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("    [skip][" + period.getWhenHoliday() + "][" + date + "]is in workingDatesExtendedWithNonWorkingPrevNext");
                }
                return null;
            }
            break;
        }

        // 2) Create an absolute Period (with the date)
        Period p = new Period();
        p.setWhenHoliday(null);

        if (period.getSingleStart() != null) {
            p.setSingleStart(toUTCDateTime(date, normalizeTime(period.getSingleStart()), timezone));
            return p;
        }

        if (SOSString.isEmpty(period.getRepeat())) {
            return null;
        }

        p.setRepeat(period.getRepeat());
        String begin = period.getBegin();
        if (begin == null || begin.isEmpty()) {
            begin = "00:00:00";
        } else {
            begin = normalizeTime(begin);
        }

        p.setBegin(toUTCDateTime(date, begin, timezone));
        String end = period.getEnd();
        if (end == null || end.isEmpty()) {
            end = "24:00:00";
        } else {
            end = normalizeTime(end);
        }
        if (end.startsWith("24:00")) {
            p.setEnd(formatDateTime(toLocalDateTime(date, "23:59:59").plusSeconds(1L), timezone));
        } else {
            p.setEnd(toUTCDateTime(date, end, timezone));
        }
        return p;
    }

    // returns 2025-06-03T07:00:00Z
    private static String toUTCDateTime(String date, String time, ZoneId timezone) {
        return formatDateTime(toLocalDateTime(date, time), timezone);
    }

    private static String formatDateTime(LocalDateTime dateTime, ZoneId timezone) {
        return DateTimeFormatter.ISO_INSTANT.format(ZonedDateTime.of(dateTime, timezone));
    }

    private static LocalDateTime toLocalDateTime(String date, String time) {
        return LocalDateTime.parse(date + "T" + time, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private static List<String> getNonWorkingDates(List<Calendar> nonWorkingDayCalendars, String from, String to) throws Exception {
        List<String> r = new ArrayList<>();
        if (!SOSCollection.isEmpty(nonWorkingDayCalendars)) {
            FrequencyResolver fr = new FrequencyResolver();
            for (Calendar basedOn : nonWorkingDayCalendars) {
                r.addAll(fr.resolveCalendar(basedOn, from, to).getDates());
            }
        }
        return r;
    }

    private static String normalizeTime(String time) {
        String[] ss = (time + ":00:00:00").split(":", 3);
        ss[2] = ss[2].substring(0, 2);
        return String.format("%2s:%2s:%2s", ss[0], ss[1], ss[2]).replace(' ', '0');
    }

}
