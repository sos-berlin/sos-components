package com.sos.js7.converter.js1.output.js7;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.AssignedNonWorkingDayCalendars;
import com.sos.inventory.model.calendar.Frequencies;
import com.sos.inventory.model.calendar.MonthDays;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.calendar.WeekDays;
import com.sos.inventory.model.calendar.WeeklyDay;
import com.sos.inventory.model.calendar.WhenHolidayType;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.js7.converter.commons.JS7ConverterHelper;

public class JS7RunTimeConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JS7RunTimeConverter.class);

    private enum DaysType {
        WEEKDAYS, MONTHDAYS, ULTIMOS;
    }

    public static Schedule convert(com.sos.js7.converter.js1.common.runtime.Schedule js1Schedule, String timeZone, List<String> workflowNames) {
        if (js1Schedule == null || !js1Schedule.isConvertable()) {
            return null;
        }

        boolean hasDates = js1Schedule.getDates() != null;
        boolean hasWeekDays = js1Schedule.getWeekDays() != null;
        boolean hasMonthDays = js1Schedule.getMonthDays() != null;
        boolean hasUltimos = js1Schedule.getUltimos() != null;
        boolean hasHolidays = js1Schedule.getHolidays() != null;

        LOGGER.info(String.format("[schedule=%s]hasDates=%s, hasWeekDays=%s,hasMonthDays=%s,hasUltimos=%s,hasHolidays=%s", js1Schedule.getName(),
                hasDates, hasWeekDays, hasMonthDays, hasUltimos, hasHolidays));

        List<AssignedCalendars> working = new ArrayList<>();
        List<AssignedNonWorkingDayCalendars> nonWorking = new ArrayList<>();
        if (hasDates) {
            convertDates(working, js1Schedule.getDates(), timeZone);
        }
        if (hasWeekDays) {
            for (com.sos.js7.converter.js1.common.runtime.WeekDays wd : js1Schedule.getWeekDays()) {
                convertDays(working, wd.getDays(), timeZone, DaysType.WEEKDAYS);
            }
        }
        if (hasMonthDays) {
            for (com.sos.js7.converter.js1.common.runtime.MonthDays wd : js1Schedule.getMonthDays()) {
                convertDays(working, wd.getDays(), timeZone, DaysType.MONTHDAYS);
                convertWeekDays(working, wd.getWeekDays(), timeZone);
            }
        }

        if (hasUltimos) {
            for (com.sos.js7.converter.js1.common.runtime.Ultimos wd : js1Schedule.getUltimos()) {
                convertDays(working, wd.getDays(), timeZone, DaysType.ULTIMOS);
            }
        }

        if (hasHolidays) {
            AssignedNonWorkingDayCalendars nwc = new AssignedNonWorkingDayCalendars();
            nwc.setCalendarName(JS7Converter.CONFIG.getScheduleConfig().getDefaultNonWorkingCalendarName());
            nonWorking.add(nwc);
        }

        Schedule schedule = null;
        if (working.size() > 0 || nonWorking.size() > 0) {
            schedule = new Schedule();
            schedule.setCalendars(working.size() == 0 ? null : working);
            schedule.setNonWorkingDayCalendars(nonWorking.size() == 0 ? null : nonWorking);
            schedule.setWorkflowNames(workflowNames);
            schedule.setPlanOrderAutomatically(JS7Converter.CONFIG.getScheduleConfig().planOrders());
            schedule.setSubmitOrderToControllerWhenPlanned(JS7Converter.CONFIG.getScheduleConfig().submitOrders());
        }
        return schedule;
    }

    private static void convertDates(List<AssignedCalendars> working, List<com.sos.js7.converter.js1.common.runtime.Date> runTimeDates,
            String timeZone) {
        if (runTimeDates == null) {
            return;
        }

        List<com.sos.js7.converter.js1.common.runtime.Period> lastPeriods = null;
        boolean equals = true;
        List<String> dates = new ArrayList<>();
        for (com.sos.js7.converter.js1.common.runtime.Date d : runTimeDates) {
            String date = d.getDate();
            if (!SOSString.isEmpty(date)) {
                dates.add(date);

                if (lastPeriods != null) {
                    equals = periodsEquals(lastPeriods, d.getPeriods());
                }

                if (!equals) {
                    break;
                }
                lastPeriods = d.getPeriods();
            }
        }
        if (equals) {
            AssignedCalendars c = createWorkingCalendar(timeZone);
            c.getIncludes().setDates(dates);
            c.setPeriods(convertPeriods(lastPeriods));

            working.add(c);
        } else {
            for (com.sos.js7.converter.js1.common.runtime.Date d : runTimeDates) {
                String date = d.getDate();
                if (!SOSString.isEmpty(date)) {
                    AssignedCalendars c = createWorkingCalendar(timeZone);
                    c.getIncludes().setDates(Collections.singletonList(date));
                    c.setPeriods(convertPeriods(d.getPeriods()));
                    working.add(c);
                }
            }
        }
    }

    private static AssignedCalendars createWorkingCalendar(String timeZone) {
        AssignedCalendars c = new AssignedCalendars();
        c.setCalendarName(JS7Converter.CONFIG.getScheduleConfig().getDefaultWorkingCalendarName());
        c.setTimeZone(timeZone);
        if (c.getTimeZone() == null) {
            c.setTimeZone(JS7Converter.CONFIG.getScheduleConfig().getDefaultTimeZone());
        }
        c.setIncludes(new Frequencies());
        return c;
    }

    private static void convertDays(List<AssignedCalendars> working, List<com.sos.js7.converter.js1.common.runtime.Day> runTimeDays, String timeZone,
            DaysType daysType) {
        if (runTimeDays == null) {
            return;
        }
        for (com.sos.js7.converter.js1.common.runtime.Day d : runTimeDays) {
            List<Integer> days = d.getDays();
            if (days != null && days.size() > 0) {
                AssignedCalendars c = createWorkingCalendar(timeZone);
                switch (daysType) {
                case WEEKDAYS:
                    WeekDays wds = new WeekDays();
                    wds.setDays(convertWeekDays(days));

                    c.getIncludes().setWeekdays(Collections.singletonList(wds));
                    break;
                case MONTHDAYS:
                    MonthDays md = new MonthDays();
                    md.setDays(days);

                    c.getIncludes().setMonthdays(Collections.singletonList(md));
                    break;
                case ULTIMOS:
                    MonthDays umd = new MonthDays();
                    umd.setDays(days);

                    c.getIncludes().setUltimos(Collections.singletonList(umd));
                    break;
                }

                c.setPeriods(convertPeriods(d.getPeriods()));
                working.add(c);
            }
        }
    }

    private static void convertWeekDays(List<AssignedCalendars> working, List<com.sos.js7.converter.js1.common.runtime.WeekDay> runTimeDays,
            String timeZone) {
        if (runTimeDays == null) {
            return;
        }
        for (com.sos.js7.converter.js1.common.runtime.WeekDay d : runTimeDays) {
            if (!SOSString.isEmpty(d.getDay()) && d.getWhich() != null) {
                AssignedCalendars c = createWorkingCalendar(timeZone);

                WeeklyDay wDay = new WeeklyDay();
                wDay.setDay(JS7ConverterHelper.getDay(d.getDay()));
                wDay.setWeekOfMonth(d.getWhich());

                MonthDays md = new MonthDays();
                md.setWeeklyDays(Collections.singletonList(wDay));
                c.getIncludes().setMonthdays(Collections.singletonList(md));
                c.setPeriods(convertPeriods(d.getPeriods()));
                working.add(c);
            }
        }
    }

    private static boolean periodsEquals(List<com.sos.js7.converter.js1.common.runtime.Period> js1Periods1,
            List<com.sos.js7.converter.js1.common.runtime.Period> js1Periods2) {
        try {
            if (js1Periods1 == null && js1Periods2 == null) {
                return true;
            }
            if (js1Periods1 != null && js1Periods2 == null) {
                return false;
            }
            if (js1Periods1 == null && js1Periods2 != null) {
                return false;
            }
            if (js1Periods1.size() != js1Periods2.size()) {
                return false;
            }

            List<String> p1 = js1Periods1.stream().map(e -> e.toString()).sorted().collect(Collectors.toList());
            List<String> p2 = js1Periods2.stream().map(e -> e.toString()).sorted().collect(Collectors.toList());

            for (int i = 0; i < p1.size(); i++) {
                if (!p1.get(i).equals(p2.get(i))) {
                    return false;
                }
            }

            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    private static List<Period> convertPeriods(List<com.sos.js7.converter.js1.common.runtime.Period> js1Periods) {
        List<Period> periods = new ArrayList<>();
        if (js1Periods != null) {
            for (com.sos.js7.converter.js1.common.runtime.Period p : js1Periods) {
                if (!SOSString.isEmpty(p.getSingleStart())) {
                    Period period = new Period();
                    period.setWhenHoliday(getWhenHolidayType(p.getWhenHoliday()));
                    period.setSingleStart(normalizeTime(p.getSingleStart()));
                    periods.add(period);
                } else if (!SOSString.isEmpty(p.getAbsoluteRepeat())) {
                    Period period = new Period();
                    period.setWhenHoliday(getWhenHolidayType(p.getWhenHoliday()));
                    period.setRepeat(normalizeTime(p.getAbsoluteRepeat()));
                    period.setBegin(normalizeTime(p.getBegin()));
                    period.setEnd(normalizeTime(p.getEnd()));
                    periods.add(period);
                }
            }
        }
        if (periods.size() == 0) {
            Period period = new Period();
            period.setWhenHoliday(WhenHolidayType.SUPPRESS);
            period.setSingleStart("00:00:00");
            periods.add(period);
        }
        return periods;
    }

    private static List<Integer> convertWeekDays(List<Integer> l) {
        if (l == null) {
            return l;
        }
        return l.stream().map(e -> {
            if (e != null && e.intValue() == 7) {
                return Integer.valueOf(0);
            }
            return e;
        }).sorted().collect(Collectors.toList());
    }

    private static WhenHolidayType getWhenHolidayType(String whenHoliday) {
        if (SOSString.isEmpty(whenHoliday)) {
            return WhenHolidayType.SUPPRESS;
        }
        switch (whenHoliday.toLowerCase()) {
        case "ignore_holiday":
            return WhenHolidayType.IGNORE;
        case "previous_non_holiday":
            return WhenHolidayType.PREVIOUSNONWORKINGDAY;
        case "next_non_holiday":
            return WhenHolidayType.NEXTNONWORKINGDAY;
        default:
            return WhenHolidayType.SUPPRESS;
        }
    }

    // TODO
    private static String normalizeTime(String val) {
        String[] arr = val.split(":");
        switch (arr.length) {
        case 1:
            return val + ":00:00";
        case 2:
            return val + ":00";
        default:
            return val;
        }
    }

}
