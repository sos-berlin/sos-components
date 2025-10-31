package com.sos.js7.converter.js1.output.js7;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.commons.xml.SOSXML.SOSXMLXPath;
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
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.js1.common.json.calendars.JS1Calendar;

public class JS7CalendarConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JS7CalendarConverter.class);

    public static List<Calendar> convertToNonWorkingDayCalendars(JS12JS7Converter js7Converter, Path js1CurrentPath, Path js7SchedulePath,
            com.sos.js7.converter.js1.common.runtime.Holidays holidays, List<JS1Calendar> nonWorkingDayCalendars,
            com.sos.js7.converter.js1.common.Include js1Include) {

        List<Calendar> result = new ArrayList<>();

        if (holidays != null) {
            List<String> dates = new ArrayList<>();
            List<String> datesWithoutCalendars = new ArrayList<>();
            List<String> calendars = new ArrayList<>();
            if (holidays.getHolidays() != null) {
                for (com.sos.js7.converter.js1.common.runtime.Holiday h : holidays.getHolidays()) {
                    dates.add(h.getDate());
                    if (SOSString.isEmpty(h.getCalendar())) {
                        datesWithoutCalendars.add(h.getDate());
                    } else {
                        calendars.add(h.getCalendar());
                    }
                }
            }

            List<com.sos.js7.converter.js1.common.json.calendars.WeekDays> weekDays = new ArrayList<>();
            if (holidays.getWeekDays() != null) {
                Set<Integer> set = new HashSet<>();
                for (com.sos.js7.converter.js1.common.runtime.WeekDays js1wd : holidays.getWeekDays()) {
                    com.sos.js7.converter.js1.common.json.calendars.WeekDays wd = new com.sos.js7.converter.js1.common.json.calendars.WeekDays();

                    js1wd.getDays().stream().forEach(d -> {
                        set.addAll(d.getDays());
                    });
                    wd.setDays(set.stream().collect(Collectors.toList()));
                    weekDays.add(wd);
                }
            }

            if (!SOSCollection.isEmpty(datesWithoutCalendars) || !SOSCollection.isEmpty(weekDays)) {
                JS1Calendar js1 = createJS1NonWorkingDayCalendarFromHolidays(js7Converter, js1CurrentPath, js7SchedulePath, datesWithoutCalendars,
                        weekDays, js1Include);
                result.add(convert(js7Converter, js1, js1Include));
            }
            if (!SOSCollection.isEmpty(calendars)) {
                Set<String> cals = new HashSet<>(calendars); // remove duplicates
                for (String c : cals) {
                    JS1Calendar js1 = createJS1NonWorkingDayCalendarFromCalendar(js7Converter, js7SchedulePath, c);
                    result.add(convert(js7Converter, js1, js1Include));
                }
            }

            if (holidays.getIncludes() != null) {
                for (com.sos.js7.converter.js1.common.Include include : holidays.getIncludes()) {
                    Path js1IncludeFile = js7Converter.findIncludeFile(js1CurrentPath, include.getIncludeFile());
                    if (js1IncludeFile == null) {
                        LOGGER.warn("[convertToNonWorkingDayCalendars][" + js1CurrentPath + "][ignored because the include file was not found]"
                                + include.getIncludeFile());
                    } else {
                        SOSXMLXPath xpath = SOSXML.newXPath();
                        try {
                            Node node = JS7ConverterHelper.getDocumentRoot(js1IncludeFile);
                            com.sos.js7.converter.js1.common.runtime.Holidays hol = new com.sos.js7.converter.js1.common.runtime.Holidays(
                                    js1IncludeFile, xpath, node);

                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.info("[convertToNonWorkingDayCalendars][js1IncludeFile]" + js1IncludeFile);
                            }

                            result.addAll(convertToNonWorkingDayCalendars(js7Converter, js1IncludeFile, js7SchedulePath, hol, null, include));
                        } catch (Exception e) {
                            LOGGER.error("[convertToNonWorkingDayCalendars][" + js1CurrentPath + "][on read js1 include file=" + include
                                    .getIncludeFile() + "]" + e, e);
                        }
                    }
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("[convertToNonWorkingDayCalendars][" + js1CurrentPath + "][js1 include file processed][" + include
                                .getIncludeFile() + "]js1 file=" + js1IncludeFile);
                    }
                }
            }
        }

        // TODO check-NonWorking calendar without holidays
        if (!SOSCollection.isEmpty(nonWorkingDayCalendars)) {
            for (com.sos.js7.converter.js1.common.json.calendars.JS1Calendar cal : nonWorkingDayCalendars) {
                String name = SOSPathUtils.getName(cal.getIdentifier()); // identifier in this case
                if (js7Converter.getJS1ToJS7Calendars().containsKey(name)) {
                    Map<String, Calendar> map = js7Converter.getJS1ToJS7Calendars().get(name);
                    if (map.containsKey(cal.getIdentifier())) {
                        Calendar js7 = map.get(cal.getIdentifier());
                        // duplicate entries will be removed later by setExcludes
                        // boolean exists = result.stream().anyMatch(c -> js7.getName().equals(c.getName()));
                        // if (!exists) {
                        result.add(js7);
                        // }
                    }
                }
            }
        }
        return result;
    }

    private static JS1Calendar createJS1NonWorkingDayCalendarFromCalendar(JS12JS7Converter js7Converter, Path js7SchedulePath, String calendar) {
        JS1Calendar js1 = new JS1Calendar();
        js1.setType(com.sos.js7.converter.js1.common.json.calendars.CalendarType.NON_WORKING_DAYS);

        js1.setPath(calendar);

        if (js7Converter.getJS1Calendars().containsKey(js1.getPath())) {
            js1 = js7Converter.getJS1Calendars().get(js1.getPath());
        } else { // not put - if calendar,json exists - will be used, when not - default frequency added
            // js7Converter.getJS1Calendars().put(js1.getPath(), js1);
        }
        return js1;
    }

    private static JS1Calendar createJS1NonWorkingDayCalendarFromHolidays(JS12JS7Converter js7Converter, Path js1CurrentPath, Path js7SchedulePath,
            List<String> dates, List<com.sos.js7.converter.js1.common.json.calendars.WeekDays> weekDays,
            com.sos.js7.converter.js1.common.Include include) {
        JS1Calendar js1 = new JS1Calendar();
        js1.setType(com.sos.js7.converter.js1.common.json.calendars.CalendarType.NON_WORKING_DAYS);

        String name = JS7ConverterHelper.getScheduleName(js7SchedulePath) + "_holidays";
        String parentPath = SOSPathUtils.getUnixStyleParentPath(js7SchedulePath.toString());
        parentPath = SOSPathUtils.getUnixStylePathWithLeadingSeparator(parentPath);

        if (include != null && include.getIncludeFile() != null) {
            name = SOSPathUtils.getBaseName(include.getIncludeFile().toString());
            parentPath = SOSPathUtils.getUnixStyleParentPath(include.getIncludeFile().toString());
            parentPath = SOSPathUtils.getUnixStylePathWithLeadingSeparator(parentPath);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[createJS1NonWorkingDayCalendarFromHolidays][js1IncludeFile=" + include.getIncludeFile() + "]name=" + name
                        + ", parentPath=" + parentPath);
            }
        }

        js1.setPath(SOSPathUtils.appendPath(parentPath, name, SOSPathUtils.PATH_SEPARATOR_UNIX));

        js1.setIncludes(new com.sos.js7.converter.js1.common.json.calendars.Frequencies());
        if (!SOSCollection.isEmpty(dates)) {
            js1.getIncludes().setDates(dates);
        }
        if (!SOSCollection.isEmpty(weekDays)) {
            js1.getIncludes().setWeekdays(weekDays);
        }

        js1.setCategory("Generated from Holidays=" + js1CurrentPath);

        // put as a new js1 calendar
        if (!js7Converter.getJS1Calendars().containsKey(js1.getPath())) {
            js7Converter.getJS1Calendars().put(js1.getPath(), js1);
        }
        return js1;
    }

    public static Calendar convert(JS12JS7Converter js7Converter, JS1Calendar js1, com.sos.js7.converter.js1.common.Include js1Include) {

        String js1NameOrPath = js1.getIdentifier();
        String js1Name = SOSPathUtils.getName(js1NameOrPath);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[convert][js1Name=" + js1Name + "][js1NameOrPath=" + js1NameOrPath + "]jsInclude=" + js1Include);
        }

        if (SOSString.isEmpty(js1Name) || getForcedName(js1) != null) {
            String defaultName = getDefaultName(js1);
            if (js7Converter.getJS1ToJS7Calendars().containsKey(defaultName)) {
                return js7Converter.getJS1ToJS7Calendars().get(defaultName).entrySet().iterator().next().getValue();
            }
            Calendar cal = convert(js1, defaultName);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[convert][js1Name=" + js1Name + ", js1NameOrPath=" + js1NameOrPath + "][js7 defaultName]" + defaultName);
            }

            Map<String, Calendar> map = new HashMap<>();
            map.put(cal.getName(), cal);
            js7Converter.getJS1ToJS7Calendars().put(cal.getName(), map);
            return cal;
        }

        String js7Path = null;
        Map<String, Calendar> map = null;
        if (js7Converter.getJS1ToJS7Calendars().containsKey(js1Name)) {
            map = js7Converter.getJS1ToJS7Calendars().get(js1Name);
            if (map.containsKey(js1NameOrPath)) {
                return map.get(js1NameOrPath);
            }
            int js1NameCounter = js1.incrementAndGetDuplicateNameCounter();
            String parentPath = SOSPathUtils.getUnixStyleParentPath(js1NameOrPath);
            js7Path = SOSPathUtils.appendPath(parentPath, js1Name + "_dup" + js1NameCounter, SOSPathUtils.PATH_SEPARATOR_UNIX);
        } else {
            js7Path = js1NameOrPath;
            map = new HashMap<>();
        }

        if (js7Converter.getJS1Calendars().containsKey(js1NameOrPath)) {
            js1 = js7Converter.getJS1Calendars().get(js1NameOrPath);
        }
        Calendar cal = convert(js1, js7Path);
        map.put(js1NameOrPath, cal);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[convert][js1Name=" + js1Name + ", js1NameOrPath=" + js1NameOrPath + "][js7]" + js7Path);
        }

        js7Converter.getJS1ToJS7Calendars().put(js1Name, map);
        return cal;
    }

    private static String getForcedName(JS1Calendar js1) {
        switch (js1.getType()) {
        case NON_WORKING_DAYS:
            if (!SOSString.isEmpty(JS12JS7Converter.CONFIG.getScheduleConfig().getForcedNonWorkingDayCalendarName())) {
                return JS12JS7Converter.CONFIG.getScheduleConfig().getForcedNonWorkingDayCalendarName();
            }
        default:
            if (!SOSString.isEmpty(JS12JS7Converter.CONFIG.getScheduleConfig().getForcedWorkingDayCalendarName())) {
                return JS12JS7Converter.CONFIG.getScheduleConfig().getForcedWorkingDayCalendarName();
            }
        }
        return null;
    }

    private static String getDefaultName(JS1Calendar js1) {
        switch (js1.getType()) {
        case NON_WORKING_DAYS:
            return getDefaultNonWorkingDayCalendarName();
        default:
            return getDefaultWorkingDayCalendarName();
        }
    }

    public static String getDefaultNonWorkingDayCalendarName() {
        if (SOSString.isEmpty(JS12JS7Converter.CONFIG.getScheduleConfig().getForcedNonWorkingDayCalendarName())) {
            return JS12JS7Converter.CONFIG.getScheduleConfig().getDefaultNonWorkingDayCalendarName();
        }
        return JS12JS7Converter.CONFIG.getScheduleConfig().getForcedNonWorkingDayCalendarName();
    }

    public static String getDefaultWorkingDayCalendarName() {
        if (SOSString.isEmpty(JS12JS7Converter.CONFIG.getScheduleConfig().getForcedWorkingDayCalendarName())) {
            return JS12JS7Converter.CONFIG.getScheduleConfig().getDefaultWorkingDayCalendarName();
        }
        return JS12JS7Converter.CONFIG.getScheduleConfig().getForcedWorkingDayCalendarName();
    }

    private static Calendar convert(JS1Calendar js1, String js7Path) {
        Calendar cal = new Calendar();
        cal.setType(convertType(js1.getType()));

        cal.setPath(js7Path);
        cal.setName(SOSPathUtils.getName(cal.getPath()));
        cal.setTitle(js1.getTitle());
        cal.setFrom(js1.getFrom());
        cal.setTo(js1.getTo());

        cal.setIncludes(convertFrequencies(js1.getIncludes()));
        cal.setExcludes(convertFrequencies(js1.getExcludes()));

        return cal;
    }

    public static List<Period> convertPeriods(List<com.sos.js7.converter.js1.common.json.calendars.Period> js1) {
        if (js1 == null || js1.size() == 0) {
            return null;
        }

        List<Period> result = new ArrayList<>();
        for (com.sos.js7.converter.js1.common.json.calendars.Period js1Child : js1) {
            result.add(convertPeriod(js1Child));
        }
        return result;
    }

    private static CalendarType convertType(com.sos.js7.converter.js1.common.json.calendars.CalendarType js1) {
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

    private static Frequencies convertFrequencies(com.sos.js7.converter.js1.common.json.calendars.Frequencies js1) {
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

    private static List<Repetition> convertRepetitions(List<com.sos.js7.converter.js1.common.json.calendars.Repetition> js1) {
        if (js1 == null || js1.size() == 0) {
            return null;
        }
        List<Repetition> result = new ArrayList<>();
        for (com.sos.js7.converter.js1.common.json.calendars.Repetition js1Child : js1) {
            Repetition o = new Repetition();
            o.setFrom(js1Child.getFrom());
            o.setRepetition(convertRepetition(js1Child.getRepetition()));
            o.setStep(js1Child.getStep());
            o.setTo(js1Child.getTo());
            result.add(o);
        }
        return result;
    }

    private static RepetitionText convertRepetition(com.sos.js7.converter.js1.common.json.calendars.RepetitionText js1) {
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

    private static List<Months> convertMonths(List<com.sos.js7.converter.js1.common.json.calendars.Months> js1) {
        if (js1 == null || js1.size() == 0) {
            return null;
        }

        List<Months> result = new ArrayList<>();
        for (com.sos.js7.converter.js1.common.json.calendars.Months js1Child : js1) {
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

    private static List<WeekDays> convertWeekdays(List<com.sos.js7.converter.js1.common.json.calendars.WeekDays> js1) {
        if (js1 == null || js1.size() == 0) {
            return null;
        }

        List<WeekDays> result = new ArrayList<>();
        for (com.sos.js7.converter.js1.common.json.calendars.WeekDays js1Child : js1) {
            WeekDays o = new WeekDays();
            o.setDays(js1Child.getDays());
            o.setFrom(js1Child.getFrom());
            o.setTo(js1Child.getTo());
            result.add(o);
        }
        return result;
    }

    private static List<MonthDays> convertMonthDays(List<com.sos.js7.converter.js1.common.json.calendars.MonthDays> js1) {
        if (js1 == null || js1.size() == 0) {
            return null;
        }

        List<MonthDays> result = new ArrayList<>();
        for (com.sos.js7.converter.js1.common.json.calendars.MonthDays js1Child : js1) {
            MonthDays o = new MonthDays();
            o.setDays(js1Child.getDays());
            o.setFrom(js1Child.getFrom());
            o.setTo(js1Child.getTo());
            o.setWeeklyDays(convertWeeklyDays(js1Child.getWeeklyDays()));
            result.add(o);
        }
        return result;
    }

    private static List<WeeklyDay> convertWeeklyDays(List<com.sos.js7.converter.js1.common.json.calendars.WeeklyDay> js1) {
        if (js1 == null || js1.size() == 0) {
            return null;
        }

        List<WeeklyDay> result = new ArrayList<>();
        for (com.sos.js7.converter.js1.common.json.calendars.WeeklyDay js1Child : js1) {
            WeeklyDay o = new WeeklyDay();
            o.setDay(js1Child.getDay());
            o.setWeekOfMonth(js1Child.getWeekOfMonth());
            result.add(o);
        }
        return result;
    }

    private static List<Holidays> convertHolidays(List<com.sos.js7.converter.js1.common.json.calendars.Holidays> js1) {
        if (js1 == null || js1.size() == 0) {
            return null;
        }

        List<Holidays> result = new ArrayList<>();
        for (com.sos.js7.converter.js1.common.json.calendars.Holidays js1Child : js1) {
            Holidays o = new Holidays();
            o.setDates(js1Child.getDates());
            o.setNationalCalendar(js1Child.getNationalCalendar());
            result.add(o);
        }
        return result;
    }

    // TODO JS7RunTimeConverter: use common function
    private static Period convertPeriod(com.sos.js7.converter.js1.common.json.calendars.Period js1) {
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
