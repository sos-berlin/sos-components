package com.sos.js7.converter.js1.output;

import java.util.ArrayList;
import java.util.List;

import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.CalendarType;
import com.sos.inventory.model.calendar.Frequencies;
import com.sos.inventory.model.calendar.Holidays;
import com.sos.inventory.model.calendar.MonthDays;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.calendar.WhenHolidayType;
import com.sos.js7.converter.js1.common.json.calendar.JS1Calendar;

public class JS12JS7CalendarConverter {

    public static Calendar convert(JS1Calendar js1) {
        Calendar cal = new Calendar();
        cal.setType(CalendarType.fromValue(js1.getType().value()));
        cal.setName(js1.getName());
        cal.setTitle(js1.getTitle());
        cal.setFrom(js1.getFrom());
        cal.setTo(js1.getTo());

        if (js1.getPeriods() != null && js1.getPeriods().size() > 0) {
            for (com.sos.js7.converter.js1.common.json.calendar.Period js1p : js1.getPeriods()) {
                // convertPeriod(js1p);
            }
        }
        if (js1.getIncludes() != null) {
            cal.setIncludes(convertFrequencies(js1.getIncludes()));
        }
        if (js1.getExcludes() != null) {
            cal.setExcludes(convertFrequencies(js1.getExcludes()));
        }
        return cal;
    }

    private static Frequencies convertFrequencies(com.sos.js7.converter.js1.common.json.calendar.Frequencies js1) {
        Frequencies f = new Frequencies();
        f.setDates(js1.getDates());
        f.setHolidays(convertHolidays(js1.getHolidays()));
        f.setMonthdays(convertMonthDays(js1.getMonthdays()));
        f.setMonths(null);
        f.setRepetitions(null);
        f.setUltimos(null);
        f.setWeekdays(null);
        return f;
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
            // o.setWeeklyDays(js1Child.getWeeklyDays());
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
