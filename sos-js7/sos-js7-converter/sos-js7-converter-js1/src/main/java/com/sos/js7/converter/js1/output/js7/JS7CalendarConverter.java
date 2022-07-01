package com.sos.js7.converter.js1.output.js7;

import java.util.ArrayList;
import java.util.List;

import com.sos.commons.util.SOSString;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.CalendarType;
import com.sos.inventory.model.calendar.Frequencies;
import com.sos.inventory.model.calendar.Holidays;
import com.sos.inventory.model.calendar.MonthDays;
import com.sos.inventory.model.calendar.Months;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.calendar.Repetition;
import com.sos.inventory.model.calendar.RepetitionText;
import com.sos.inventory.model.calendar.WeekDays;
import com.sos.inventory.model.calendar.WeeklyDay;
import com.sos.inventory.model.calendar.WhenHolidayType;
import com.sos.js7.converter.commons.JS7ConverterConfig;
import com.sos.js7.converter.js1.common.json.calendar.JS1Calendar;

public class JS7CalendarConverter {

    public static Calendar convert(JS7ConverterConfig config, JS1Calendar js1) {
        Calendar cal = new Calendar();
        cal.setType(convertType(js1.getType()));
        cal.setName(getCalendarName(config, cal, js1));
        cal.setTitle(js1.getTitle());
        cal.setFrom(js1.getFrom());
        cal.setTo(js1.getTo());

        cal.setIncludes(convertFrequencies(js1.getIncludes()));
        cal.setExcludes(convertFrequencies(js1.getExcludes()));

        return cal;
    }

    public static List<Period> convertPeriods(List<com.sos.js7.converter.js1.common.json.calendar.Period> js1) {
        if (js1 == null || js1.size() == 0) {
            return null;
        }

        List<Period> result = new ArrayList<>();
        for (com.sos.js7.converter.js1.common.json.calendar.Period js1Child : js1) {
            result.add(convertPeriod(js1Child));
        }
        return result;
    }

    private static String getCalendarName(JS7ConverterConfig config, Calendar cal, JS1Calendar js1) {
        String defaultName = null;
        switch (cal.getType()) {
        case NONWORKINGDAYSCALENDAR:
            if (!SOSString.isEmpty(config.getScheduleConfig().getForcedNonWorkingCalendarName())) {
                return config.getScheduleConfig().getForcedNonWorkingCalendarName();
            }
            defaultName = config.getScheduleConfig().getDefaultNonWorkingCalendarName();
            break;
        case WORKINGDAYSCALENDAR:
            if (!SOSString.isEmpty(config.getScheduleConfig().getForcedWorkingCalendarName())) {
                return config.getScheduleConfig().getForcedWorkingCalendarName();
            }
            defaultName = config.getScheduleConfig().getDefaultWorkingCalendarName();
            break;
        }

        String name = js1.getName();
        if (SOSString.isEmpty(name)) {
            name = js1.getBasedOn();
        }
        if (SOSString.isEmpty(name)) {
            name = defaultName;
        } else if (name.startsWith("/")) {
            name = name.substring(1);
        }
        return name;
    }

    private static CalendarType convertType(com.sos.js7.converter.js1.common.json.calendar.CalendarType js1) {
        switch (js1) {
        case NON_WORKING_DAYS:
            return CalendarType.NONWORKINGDAYSCALENDAR;
        // TODO ORDER,SCHEDULE etc ...
        case ORDER:
        case SCHEDULE:
        case JOB:
        case WORKING_DAYS:
        default:
            return CalendarType.WORKINGDAYSCALENDAR;
        }
    }

    private static Frequencies convertFrequencies(com.sos.js7.converter.js1.common.json.calendar.Frequencies js1) {
        if (js1 == null) {
            return null;
        }
        Frequencies f = new Frequencies();
        f.setDates(js1.getDates());
        f.setHolidays(convertHolidays(js1.getHolidays()));
        f.setMonthdays(convertMonthDays(js1.getMonthdays()));
        f.setMonths(convertMonths(js1.getMonths()));
        f.setRepetitions(convertRepetitions(js1.getRepetitions()));
        f.setUltimos(convertMonthDays(js1.getUltimos()));
        f.setWeekdays(convertWeekdays(js1.getWeekdays()));
        return f;
    }

    private static List<Repetition> convertRepetitions(List<com.sos.js7.converter.js1.common.json.calendar.Repetition> js1) {
        if (js1 == null || js1.size() == 0) {
            return null;
        }
        List<Repetition> result = new ArrayList<>();
        for (com.sos.js7.converter.js1.common.json.calendar.Repetition js1Child : js1) {
            Repetition o = new Repetition();
            o.setFrom(js1Child.getFrom());
            o.setRepetition(convertRepetition(js1Child.getRepetition()));
            o.setStep(js1Child.getStep());
            o.setTo(js1Child.getTo());
            result.add(o);
        }
        return result;
    }

    private static RepetitionText convertRepetition(com.sos.js7.converter.js1.common.json.calendar.RepetitionText js1) {
        if (js1 != null) {
            switch (js1) {
            case DAILY:
                return RepetitionText.DAILY;
            case MONTHLY:
                return RepetitionText.MONTHLY;
            case WEEKLY:
                return RepetitionText.WEEKLY;
            case YEARLY:
                return RepetitionText.YEARLY;
            }
        }
        return null;
    }

    private static List<Months> convertMonths(List<com.sos.js7.converter.js1.common.json.calendar.Months> js1) {
        if (js1 == null || js1.size() == 0) {
            return null;
        }

        List<Months> result = new ArrayList<>();
        for (com.sos.js7.converter.js1.common.json.calendar.Months js1Child : js1) {
            Months o = new Months();
            o.setFrom(js1Child.getFrom());
            o.setMonthdays(convertMonthDays(js1Child.getMonthdays()));
            o.setMonths(js1Child.getMonths());
            o.setTo(js1Child.getTo());
            o.setUltimos(convertMonthDays(js1Child.getUltimos()));
            o.setWeekdays(convertWeekdays(js1Child.getWeekdays()));
            result.add(o);
        }
        return result;
    }

    private static List<WeekDays> convertWeekdays(List<com.sos.js7.converter.js1.common.json.calendar.WeekDays> js1) {
        if (js1 == null || js1.size() == 0) {
            return null;
        }

        List<WeekDays> result = new ArrayList<>();
        for (com.sos.js7.converter.js1.common.json.calendar.WeekDays js1Child : js1) {
            WeekDays o = new WeekDays();
            o.setDays(js1Child.getDays());
            o.setFrom(js1Child.getFrom());
            o.setTo(js1Child.getTo());
            result.add(o);
        }
        return result;
    }

    private static List<MonthDays> convertMonthDays(List<com.sos.js7.converter.js1.common.json.calendar.MonthDays> js1) {
        if (js1 == null || js1.size() == 0) {
            return null;
        }

        List<MonthDays> result = new ArrayList<>();
        for (com.sos.js7.converter.js1.common.json.calendar.MonthDays js1Child : js1) {
            MonthDays o = new MonthDays();
            o.setDays(js1Child.getDays());
            o.setFrom(js1Child.getFrom());
            o.setTo(js1Child.getTo());
            o.setWeeklyDays(convertWeeklyDays(js1Child.getWeeklyDays()));
            result.add(o);
        }
        return result;
    }

    private static List<WeeklyDay> convertWeeklyDays(List<com.sos.js7.converter.js1.common.json.calendar.WeeklyDay> js1) {
        if (js1 == null || js1.size() == 0) {
            return null;
        }

        List<WeeklyDay> result = new ArrayList<>();
        for (com.sos.js7.converter.js1.common.json.calendar.WeeklyDay js1Child : js1) {
            WeeklyDay o = new WeeklyDay();
            o.setDay(js1Child.getDay());
            o.setWeekOfMonth(js1Child.getWeekOfMonth());
            result.add(o);
        }
        return result;
    }

    private static List<Holidays> convertHolidays(List<com.sos.js7.converter.js1.common.json.calendar.Holidays> js1) {
        if (js1 == null || js1.size() == 0) {
            return null;
        }

        List<Holidays> result = new ArrayList<>();
        for (com.sos.js7.converter.js1.common.json.calendar.Holidays js1Child : js1) {
            Holidays o = new Holidays();
            o.setDates(js1Child.getDates());
            o.setNationalCalendar(js1Child.getNationalCalendar());
            result.add(o);
        }
        return result;
    }

    // TODO JS7RunTimeConverter: use common function
    private static Period convertPeriod(com.sos.js7.converter.js1.common.json.calendar.Period js1) {
        Period p = new Period();
        p.setBegin(js1.getBegin());
        p.setEnd(js1.getEnd());
        p.setRepeat(js1.getRepeat());
        p.setSingleStart(js1.getSingleStart());

        switch (js1.getWhenHoliday().toLowerCase()) {
        case "suppress":
            p.setWhenHoliday(WhenHolidayType.SUPPRESS);
            break;
        case "ignore":
            p.setWhenHoliday(WhenHolidayType.IGNORE);
            break;
        case "next_non_holiday":
            p.setWhenHoliday(WhenHolidayType.NEXTNONWORKINGDAY);
            break;
        case "previous_non_holiday":
            p.setWhenHoliday(WhenHolidayType.PREVIOUSNONWORKINGDAY);
            break;
        }
        return p;
    }

}
