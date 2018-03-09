package com.sos.joc.classes.calendar;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.exception.SOSInvalidDataException;
import com.sos.exception.SOSMissingDataException;
import com.sos.joc.model.calendar.CalendarDatesFilter;
import com.sos.joc.model.calendar.Dates;
import com.sos.joc.model.calendar.Frequencies;
import com.sos.joc.model.calendar.Holidays;
import com.sos.joc.model.calendar.MonthDays;
import com.sos.joc.model.calendar.Months;
import com.sos.joc.model.calendar.Repetition;
import com.sos.joc.model.calendar.RepetitionText;
import com.sos.joc.model.calendar.WeekDays;
import com.sos.joc.model.calendar.WeeklyDay;

public class FrequencyResolver {

    private SortedMap<String, Calendar> dates = new TreeMap<String, Calendar>();
    private SortedMap<String, Calendar> datesWithoutRestrictions = new TreeMap<String, Calendar>();
    private SortedSet<String> restrictions = new TreeSet<String>();
    private SortedSet<String> withExcludes = new TreeSet<String>();
    private Calendar calendarFrom = null;
    private Calendar dateFrom = null;
    private Calendar dateTo = null;
    private Frequencies includes = null;
    private Frequencies excludes = null;
    private DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);

    public FrequencyResolver() {
    }

    public SortedMap<String, Calendar> getDates() {
        return dates;
    }

    public SortedSet<String> getWithExcludes() {
        return withExcludes;
    }

    public Dates resolve(CalendarDatesFilter calendarFilter) throws SOSMissingDataException, SOSInvalidDataException {
        if (calendarFilter != null) {
            return resolve(calendarFilter.getCalendar(), calendarFilter.getDateFrom(), calendarFilter.getDateTo());
        } else {
            Dates d = new Dates();
            d.setDates(new ArrayList<String>());
            d.setWithExcludes(new ArrayList<String>());
            d.setDeliveryDate(Date.from(Instant.now()));
            return d;
        }
    }

    public Dates resolve(String calendarJson, String from, String to) throws SOSMissingDataException, SOSInvalidDataException, JsonParseException,
            JsonMappingException, IOException {
        return resolve(new ObjectMapper().readValue(calendarJson, com.sos.joc.model.calendar.Calendar.class), from, to);
    }

    public Dates resolve(com.sos.joc.model.calendar.Calendar calendar, String from, String to) throws SOSMissingDataException,
            SOSInvalidDataException {
        init(calendar, from, to);
        Dates d = new Dates();
        d.setDates(new ArrayList<String>());
        if (this.dateFrom.compareTo(this.dateTo) <= 0) {
            addDates();
            addHolidays();
            addWeekDays();
            addMonthDays();
            addUltimos();
            addMonths();
            addRepetitions();
            removeDates();
            removeWeekDays();
            removeMonthDays();
            removeUltimos();
            removeMonths();
            removeHolidays();
            removeRepetitions();
            d.getDates().addAll(dates.keySet());
            d.setWithExcludes(new ArrayList<String>(withExcludes));
        } else {
            d.setDates(new ArrayList<String>());
        }
        d.setDeliveryDate(Date.from(Instant.now()));
        return d;
    }

    public Dates resolveFromToday(String calendarJson) throws SOSMissingDataException, SOSInvalidDataException, JsonParseException,
            JsonMappingException, IOException {
        return resolve(calendarJson, df.format(Instant.now()), null);
    }

    public Dates resolveFromToday(com.sos.joc.model.calendar.Calendar calendar) throws SOSMissingDataException, SOSInvalidDataException {
        return resolve(calendar, df.format(Instant.now()), null);
    }

    public Dates resolveRestrictions(String basedCalendarJson, String calendarJson, String from, String to)
            throws SOSMissingDataException, SOSInvalidDataException, JsonParseException, JsonMappingException, IOException {
        return resolveRestrictions(new ObjectMapper().readValue(basedCalendarJson, com.sos.joc.model.calendar.Calendar.class), new ObjectMapper()
                .readValue(calendarJson, com.sos.joc.model.calendar.Calendar.class), from, to);
    }

    public Dates resolveRestrictions(com.sos.joc.model.calendar.Calendar basedCalendar,
            com.sos.joc.model.calendar.Calendar calendar, String from, String to) throws SOSMissingDataException, SOSInvalidDataException {
        init(basedCalendar, null, null);
        Dates d = new Dates();
        d.setDates(new ArrayList<String>());
        if (this.dateFrom.compareTo(this.dateTo) <= 0) {
            addDates();
            addHolidays();
            addWeekDays();
            addMonthDays();
            addUltimos();
            addMonths();
            addRepetitions();
            removeDates();
            removeWeekDays();
            removeMonthDays();
            removeUltimos();
            removeMonths();
            removeHolidays();
            removeRepetitions();
            if (calendar != null && !this.dates.isEmpty()) {
                this.dateFrom = getFrom(from);
                this.dateTo = getTo(to);
                //datesWithoutRestrictions = dates.subMap(df.format(dateFrom.toInstant()), df.format(dateTo.toInstant().plusSeconds(24*60*60)));
                for (Entry<String, Calendar> entry : dates.entrySet()) {
                    if (entry.getValue().before(dateFrom)) {
                        continue;
                    }
                    if (entry.getValue().after(dateTo)) {
                        break;
                    }
                    datesWithoutRestrictions.put(entry.getKey(), entry.getValue());
                }
                if (this.dateFrom.compareTo(this.dateTo) <= 0) {
                    this.includes = calendar.getIncludes();
                    //this.excludes = calendar.getExcludes(); //TODO exists?
                    addDatesRestrictions();
                    addWeekDaysRestrictions();
                    addMonthDaysRestrictions();
                    addUltimosRestrictions();
                    addMonthsRestrictions();
                    addRepetitionsRestrictions();
                }
            }
            restrictions.addAll(datesWithoutRestrictions.keySet());
            d.getDates().addAll(restrictions);
        }
        d.setDeliveryDate(Date.from(Instant.now()));
        return d;
    }

    public Dates resolveRestrictionsFromToday(String basedCalendarJson, String calendarJson) throws SOSMissingDataException,
            SOSInvalidDataException, JsonParseException, JsonMappingException, IOException {
        return resolveRestrictions(basedCalendarJson, calendarJson, df.format(Instant.now()), null);
    }
    
    public Dates resolveRestrictionsFromToday(com.sos.joc.model.calendar.Calendar basedCalendar, String calendarJson) throws SOSMissingDataException,
            SOSInvalidDataException, JsonParseException, JsonMappingException, IOException {
        return resolveRestrictions(basedCalendar, new ObjectMapper().readValue(calendarJson, com.sos.joc.model.calendar.Calendar.class), df.format(
                Instant.now()), null);
    }

    public Dates resolveRestrictionsFromToday(com.sos.joc.model.calendar.Calendar basedCalendar,
            com.sos.joc.model.calendar.Calendar calendar) throws SOSMissingDataException, SOSInvalidDataException {
        return resolveRestrictions(basedCalendar, calendar, df.format(Instant.now()), null);
    }

    public void init(com.sos.joc.model.calendar.Calendar calendar, String from, String to) throws SOSMissingDataException, SOSInvalidDataException {
        if (calendar != null) {
            setDateFrom(from, calendar.getFrom());
            setDateTo(to, calendar.getTo());
            this.includes = calendar.getIncludes();
            this.excludes = calendar.getExcludes();
        } else {
            throw new SOSMissingDataException("calendar object is undefined");
        }
    }

    public void setDateFrom(String dateFrom, String calendarFrom) throws SOSMissingDataException, SOSInvalidDataException {

        if ((dateFrom == null || dateFrom.isEmpty()) && (calendarFrom == null || calendarFrom.isEmpty())) {
            this.dateFrom = getTodayCalendar();
            this.calendarFrom = getTodayCalendar();
        } else {

            Calendar calFrom = getCalendarFromString(calendarFrom, "calendar field 'from' must have the format YYYY-MM-DD.");
            Calendar dFrom = getCalendarFromString(dateFrom, "'dateFrom' parameter must have the format YYYY-MM-DD.");

            if (calFrom == null) {
                this.dateFrom = dFrom;
            } else if (dFrom == null) {
                this.dateFrom = calFrom;
            } else if (calFrom.before(dFrom)) {
                this.dateFrom = dFrom;
            } else {
                this.dateFrom = calFrom;
            }
            if (calFrom == null) {
                this.calendarFrom = (Calendar) dFrom.clone();
            } else {
                this.calendarFrom = (Calendar) calFrom.clone();
            }
        }
    }

    public void setDateTo(String dateTo, String calendarTo) throws SOSMissingDataException, SOSInvalidDataException {

        if ((dateTo == null || dateTo.isEmpty()) && (calendarTo == null || calendarTo.isEmpty())) {
            throw new SOSMissingDataException("'dateTo' parameter and calendar field 'to' are undefined.");
        } else {

            Calendar calTo = getCalendarFromString(calendarTo, "calendar field 'to' must have the format YYYY-MM-DD.");
            Calendar dTo = getCalendarFromString(dateTo, "'dateTo' parameter must have the format YYYY-MM-DD.");

            if (calTo == null) {
                this.dateTo = dTo;
            } else if (dTo == null) {
                this.dateTo = calTo;
            } else if (calTo.after(dTo)) {
                this.dateTo = dTo;
            } else {
                this.dateTo = calTo;
            }
        }
    }

    public void addDates() throws SOSInvalidDataException {
        if (includes != null) {
            addDates(includes.getDates());
        }
    }

    public void removeDates() throws SOSInvalidDataException {
        if (excludes != null && dates.size() > 0) {
            removeDates(excludes.getDates());
        }
    }

    public void addHolidays() throws SOSInvalidDataException {
        if (includes != null) {
            addHolidays(includes.getHolidays());
        }
    }

    public void removeHolidays() throws SOSInvalidDataException {
        if (excludes != null && dates.size() > 0) {
            removeHolidays(excludes.getHolidays());
        }
    }

    public void addWeekDays() throws SOSInvalidDataException {
        if (includes != null) {
            addWeekDays(includes.getWeekdays());
        }
    }

    public void removeWeekDays() throws SOSInvalidDataException {
        if (excludes != null && dates.size() > 0) {
            removeWeekDays(excludes.getWeekdays());
        }
    }

    public void addMonthDays() throws SOSInvalidDataException {
        if (includes != null) {
            addMonthDays(includes.getMonthdays());
        }
    }

    public void removeMonthDays() throws SOSInvalidDataException {
        if (excludes != null && dates.size() > 0) {
            removeMonthDays(excludes.getMonthdays());
        }
    }

    public void addUltimos() throws SOSInvalidDataException {
        if (includes != null) {
            addUltimos(includes.getUltimos());
        }
    }

    public void removeUltimos() throws SOSInvalidDataException {
        if (excludes != null && dates.size() > 0) {
            removeUltimos(excludes.getUltimos());
        }
    }

    public void addRepetitions() throws SOSInvalidDataException {
        if (includes != null) {
            addRepetitions(includes.getRepetitions());
        }
    }

    public void removeRepetitions() throws SOSInvalidDataException {
        if (excludes != null && dates.size() > 0) {
            removeRepetitions(excludes.getRepetitions());
        }
    }

    public void addMonths() throws SOSInvalidDataException {
        if (includes != null) {
            addMonths(includes.getMonths());
        }
    }

    public void removeMonths() throws SOSInvalidDataException {
        if (excludes != null && dates.size() > 0) {
            removeMonths(excludes.getMonths());
        }
    }
    
    public String getToday() {
        return df.format(Instant.now());
    }

    private Calendar getCalendarFromString(String cal) throws SOSInvalidDataException {
        return getCalendarFromString(cal, "dates must have the format YYYY-MM-DD.");
    }

    private Calendar getCalendarFromString(String cal, String msg) throws SOSInvalidDataException {
        Calendar calendar = null;
        if (cal != null && !cal.isEmpty()) {
            if (!cal.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                throw new SOSInvalidDataException(msg);
            }
            calendar = Calendar.getInstance();
            calendar.setTime(Date.from(Instant.parse(cal + "T00:00:00Z")));
        }
        return calendar;
    }

    private void addDates(List<String> list) throws SOSInvalidDataException {
        addAll(resolveDates(list));
    }

    private void removeDates(List<String> list) throws SOSInvalidDataException {
        removeAll(resolveDates(list));
    }

    private void addHolidays(List<Holidays> holidays) throws SOSInvalidDataException {
        if (holidays != null) {
            for (Holidays holiday : holidays) {
                addDates(holiday.getDates());
            }
        }
    }

    private void removeHolidays(List<Holidays> holidays) throws SOSInvalidDataException {
        if (holidays != null) {
            for (Holidays holiday : holidays) {
                removeDates(holiday.getDates());
            }
        }
    }

    private void addWeekDays(List<WeekDays> weekDays) throws SOSInvalidDataException {
        addWeekDays(weekDays, dateFrom, dateTo);
    }

    private void addWeekDays(List<WeekDays> weekDays, Calendar from, Calendar to) throws SOSInvalidDataException {
        if (weekDays != null) {
            for (WeekDays weekDay : weekDays) {
                addAll(resolveWeekDays(weekDay.getDays(), getFrom(weekDay.getFrom(), from), getTo(weekDay.getTo(), to)));
            }
        }
    }

    private void removeWeekDays(List<WeekDays> weekDays) throws SOSInvalidDataException {
        removeWeekDays(weekDays, dateFrom, dateTo);
    }

    private void removeWeekDays(List<WeekDays> weekDays, Calendar from, Calendar to) throws SOSInvalidDataException {
        if (weekDays != null) {
            for (WeekDays weekDay : weekDays) {
                removeAll(resolveWeekDays(weekDay.getDays(), getFrom(weekDay.getFrom(), from), getTo(weekDay.getTo(), to)));
            }
        }
    }

    private void addMonthDays(List<MonthDays> monthDays) throws SOSInvalidDataException {
        addMonthDays(monthDays, dateFrom, dateTo);
    }

    private void addMonthDays(List<MonthDays> monthDays, Calendar from, Calendar to) throws SOSInvalidDataException {
        if (monthDays != null) {
            for (MonthDays monthDay : monthDays) {
                addAll(resolveMonthDays(monthDay.getDays(), monthDay.getWeeklyDays(), getFrom(monthDay.getFrom(), from),
                        getTo(monthDay.getTo(), to)));
            }
        }
    }

    private void removeMonthDays(List<MonthDays> monthDays) throws SOSInvalidDataException {
        removeMonthDays(monthDays, dateFrom, dateTo);
    }

    private void removeMonthDays(List<MonthDays> monthDays, Calendar from, Calendar to) throws SOSInvalidDataException {
        if (monthDays != null) {
            for (MonthDays monthDay : monthDays) {
                removeAll(resolveMonthDays(monthDay.getDays(), monthDay.getWeeklyDays(), getFrom(monthDay.getFrom(), from),
                        getTo(monthDay.getTo(), to)));
            }
        }
    }

    private void addUltimos(List<MonthDays> monthDays) throws SOSInvalidDataException {
        addUltimos(monthDays, dateFrom, dateTo);
    }

    private void addUltimos(List<MonthDays> ultimos, Calendar from, Calendar to) throws SOSInvalidDataException {
        if (ultimos != null) {
            for (MonthDays ultimo : ultimos) {
                addAll(resolveUltimos(ultimo.getDays(), ultimo.getWeeklyDays(), getFrom(ultimo.getFrom(), from), getTo(ultimo
                        .getTo(), to)));
            }
        }
    }

    private void removeUltimos(List<MonthDays> monthDays) throws SOSInvalidDataException {
        removeUltimos(monthDays, dateFrom, dateTo);
    }

    private void removeUltimos(List<MonthDays> ultimos, Calendar from, Calendar to) throws SOSInvalidDataException {
        if (ultimos != null) {
            for (MonthDays ultimo : ultimos) {
                removeAll(resolveUltimos(ultimo.getDays(), ultimo.getWeeklyDays(), getFrom(ultimo.getFrom(), from), getTo(
                        ultimo.getTo(), to)));
            }
        }
    }

    private void addRepetitions(List<Repetition> repetitions) throws SOSInvalidDataException {
        if (repetitions != null) {
            for (Repetition repetition : repetitions) {
                addAll(resolveRepetitions(repetition.getRepetition(), repetition.getStep(), getFrom(repetition.getFrom(), calendarFrom), getFrom(
                        repetition.getFrom()), getTo(repetition.getTo())));
            }
        }
    }

    private void removeRepetitions(List<Repetition> repetitions) throws SOSInvalidDataException {
        if (repetitions != null) {
            for (Repetition repetition : repetitions) {
                removeAll(resolveRepetitions(repetition.getRepetition(), repetition.getStep(), getFrom(repetition.getFrom(), calendarFrom), getFrom(
                        repetition.getFrom()), getTo(repetition.getTo())));
            }
        }
    }

    private void addMonths(List<Months> months) throws SOSInvalidDataException {
        if (months != null) {
            Calendar monthStart = Calendar.getInstance();
            Calendar monthEnd = Calendar.getInstance();
            for (Months month : months) {
                if (month.getMonths() != null) {
                    Calendar from = getFrom(month.getFrom());
                    Calendar to = getTo(month.getTo());
                    while (from.compareTo(to) <= 0) {
                        int lastDayOfMonth = from.getActualMaximum(Calendar.DAY_OF_MONTH);
                        if (month.getMonths().contains(from.get(Calendar.MONTH) + 1)) {
                            Calendar monthFrom = getFromPerMonth(monthStart, from);
                            Calendar monthTo = getFromToMonth(monthEnd, from, to, lastDayOfMonth);
                            addWeekDays(month.getWeekdays(), monthFrom, monthTo);
                            addMonthDays(month.getMonthdays(), monthFrom, monthTo);
                            addUltimos(month.getUltimos(), monthFrom, monthTo);

                        }
                        from.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);
                        from.add(Calendar.DATE, 1);
                    }
                }
            }
        }
    }

    private void removeMonths(List<Months> months) throws SOSInvalidDataException {
        if (months != null) {
            Calendar monthStart = Calendar.getInstance();
            Calendar monthEnd = Calendar.getInstance();
            for (Months month : months) {
                if (month.getMonths() != null) {
                    Calendar from = getFrom(month.getFrom());
                    Calendar to = getTo(month.getTo());
                    while (from.compareTo(to) <= 0) {
                        int lastDayOfMonth = from.getActualMaximum(Calendar.DAY_OF_MONTH);
                        if (month.getMonths().contains(from.get(Calendar.MONTH) + 1)) {
                            Calendar monthFrom = getFromPerMonth(monthStart, from);
                            Calendar monthTo = getFromToMonth(monthEnd, from, to, lastDayOfMonth);
                            removeWeekDays(month.getWeekdays(), monthFrom, monthTo);
                            removeMonthDays(month.getMonthdays(), monthFrom, monthTo);
                            removeUltimos(month.getUltimos(), monthFrom, monthTo);
                        }
                        from.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);
                        from.add(Calendar.DATE, 1);
                    }
                }
            }
        }
    }

    private Calendar getFromPerMonth(Calendar monthStart, Calendar refFrom) throws SOSInvalidDataException {
        monthStart.set(Calendar.YEAR, refFrom.get(Calendar.YEAR));
        monthStart.set(Calendar.MONTH, refFrom.get(Calendar.MONTH));
        monthStart.set(Calendar.DAY_OF_MONTH, 1);
        return getFrom(monthStart, refFrom);
    }

    private Calendar getFromToMonth(Calendar monthEnd, Calendar refFrom, Calendar refTo, int lastDayOfMonth) throws SOSInvalidDataException {
        monthEnd.set(Calendar.YEAR, refFrom.get(Calendar.YEAR));
        monthEnd.set(Calendar.MONTH, refFrom.get(Calendar.MONTH));
        monthEnd.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);
        return getTo(monthEnd, refTo);
    }

    private void addAll(Map<String, Calendar> map) {
        if (map != null) {
            dates.putAll(map);
        }
    }

    private boolean removeAll(Map<String, Calendar> map) {
        boolean removeAffects = false;
        if (map != null) {
            for (String item : map.keySet()) {
                if (dates.remove(item) != null) {
                    withExcludes.add(item);
                    removeAffects = true;
                }
            }
        }
        return removeAffects;
    }
    
    private void addRestrictions(Set<String> set) {
        if (set != null) {
            restrictions.addAll(set);
        }
    }

    private Calendar getFrom(String from) throws SOSInvalidDataException {
        return getFrom(from, dateFrom);
    }

    private Calendar getTo(String to) throws SOSInvalidDataException {
        return getTo(to, dateTo);
    }

    private Calendar getFrom(String from, Calendar fromRef) throws SOSInvalidDataException {
        Calendar cal = Calendar.getInstance();
        if (from == null || from.isEmpty()) {
            return (Calendar) fromRef.clone();
        }
        if (!from.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            throw new SOSInvalidDataException("json field 'from' must have the format YYYY-MM-DD.");
        }
        cal.setTime(Date.from(Instant.parse(from + "T00:00:00Z")));
        if (cal.after(fromRef)) {
            return cal;
        }
        return (Calendar) fromRef.clone();
    }

    private Calendar getFrom(Calendar from, Calendar fromRef) throws SOSInvalidDataException {
        if (from == null) {
            return (Calendar) fromRef.clone();
        }
        if (from.after(fromRef)) {
            return from;
        }
        return (Calendar) fromRef.clone();
    }

    private Calendar getTo(String to, Calendar toRef) throws SOSInvalidDataException {
        if (to == null || to.isEmpty()) {
            return (Calendar) toRef.clone();
        }
        if (!to.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            throw new SOSInvalidDataException("json field 'to' must have the format YYYY-MM-DD.");
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(Date.from(Instant.parse(to + "T00:00:00Z")));
        if (cal.before(toRef)) {
            return cal;
        }
        return (Calendar) toRef.clone();
    }

    private Calendar getTo(Calendar to, Calendar toRef) throws SOSInvalidDataException {
        if (to == null) {
            return (Calendar) toRef.clone();
        }
        if (to.before(toRef)) {
            return to;
        }
        return (Calendar) toRef.clone();
    }

    private boolean isBetweenFromTo(Calendar cal) throws SOSInvalidDataException {
        if (cal != null) {
            if (cal.compareTo(dateTo) <= 0 && cal.compareTo(dateFrom) >= 0) {
                return true;
            }
        }
        return false;
    }

    private int getWeekOfMonthOfWeeklyDay(Calendar currentDay) {
        int dayOfMonth = currentDay.get(Calendar.DAY_OF_MONTH);
        int weekOfMonthOfWeeklyDay = dayOfMonth / 7;
        if (dayOfMonth % 7 > 0) {
            weekOfMonthOfWeeklyDay++;
        }
        return weekOfMonthOfWeeklyDay;
    }

    private int getWeekOfMonthOfUltimoWeeklyDay(Calendar currentDay) {
        int dayOfMonth = currentDay.get(Calendar.DAY_OF_MONTH);
        int maxDaysOfMonth = currentDay.getActualMaximum(Calendar.DAY_OF_MONTH);
        dayOfMonth = maxDaysOfMonth - dayOfMonth + 1;
        int weekOfMonthOfWeeklyDay = dayOfMonth / 7;
        if (dayOfMonth % 7 > 0) {
            weekOfMonthOfWeeklyDay++;
        }
        return weekOfMonthOfWeeklyDay;
    }

    private Map<String, Calendar> resolveDates(List<String> dates) throws SOSInvalidDataException {
        Map<String, Calendar> d = new HashMap<String, Calendar>();
        if (dates != null && !dates.isEmpty()) {
            for (String date : dates) {
                Calendar cal = getCalendarFromString(date);
                if (isBetweenFromTo(cal)) {
                    d.put(date, cal);
                }
            }
        }
        return d;
    }

    private Map<String, Calendar> resolveWeekDays(List<Integer> days, Calendar from, Calendar to) throws SOSInvalidDataException {
        if (days == null || days.isEmpty()) {
            throw new SOSInvalidDataException("json field 'days' in 'weekdays' is undefined.");
        }
        if (days.contains(7) && !days.contains(0)) {
            days.add(0);
        }
        Map<String, Calendar> dates = new HashMap<String, Calendar>();

        while (from.compareTo(to) <= 0) {
            // Calendar.DAY_OF_WEEK: 1=Sunday, 2=Monday, ... -> -1
            if (days.contains(from.get(Calendar.DAY_OF_WEEK) - 1)) {
                dates.put(df.format(from.toInstant()), (Calendar) from.clone());
            }
            from.add(Calendar.DATE, 1);
        }
        return dates;
    }
    
    private Set<String> resolveBasedOnWeekDays(List<Integer> days, Calendar from, Calendar to) throws SOSInvalidDataException {
        if (days == null || days.isEmpty()) {
            throw new SOSInvalidDataException("json field 'days' in 'weekdays' is undefined.");
        }
        if (days.contains(7) && !days.contains(0)) {
            days.add(0);
        }
        Set<String> dates = new HashSet<String>();
        
        for (Entry<String, Calendar> date : this.dates.entrySet()) {
            if (date == null || date.getValue() == null) {
                continue;
            }
            if (date.getValue().after(to)) {
                break;
            }
            if (date.getValue().before(from)) {
                continue;
            }
            if (days.contains(date.getValue().get(Calendar.DAY_OF_WEEK) - 1)) {
                dates.add(date.getKey());
            }
        }
        Map<String, Calendar> tmpDatesWithoutRestrictions = new HashMap<String, Calendar>(datesWithoutRestrictions);
        for (Entry<String, Calendar> entry : tmpDatesWithoutRestrictions.entrySet()) {
            if (entry.getValue().before(from) || entry.getValue().after(to)) {
                continue;
            }
            datesWithoutRestrictions.remove(entry.getKey());
        }

        return dates;
    }

    private Map<String, Calendar> resolveMonthDays(List<Integer> days, List<WeeklyDay> weeklyDays, Calendar from, Calendar to) {

        Map<String, Calendar> dates = new HashMap<String, Calendar>();
        WeeklyDay weeklyDay = new WeeklyDay();

        while (from.compareTo(to) <= 0) {
            if (days != null) {
                if (days.contains(from.get(Calendar.DAY_OF_MONTH))) {
                    dates.put(df.format(from.toInstant()), (Calendar) from.clone());
                }
            }
            if (weeklyDays != null) {
                weeklyDay.setDay(from.get(Calendar.DAY_OF_WEEK) - 1);
                weeklyDay.setWeekOfMonth(getWeekOfMonthOfWeeklyDay(from));
                if (weeklyDays.contains(weeklyDay)) {
                    dates.put(df.format(from.toInstant()), (Calendar) from.clone());
                }
            }
            from.add(Calendar.DATE, 1);
        }
        return dates;
    }

    private Set<String> resolveBasedOnMonthDays(List<Integer> days, List<WeeklyDay> weeklyDays, Calendar from, Calendar to) {

        Set<String> dates = new HashSet<String>();
        WeeklyDay weeklyDay = new WeeklyDay();
        int dayOfMonth = 0;
        int lastMonth = -1;
        int[] weeklyDayOfMonth = {0,0,0,0,0,0,0};
        
        for (Entry<String, Calendar> date : this.dates.entrySet()) {
            if (date == null || date.getValue() == null) {
                continue;
            }
            if (date.getValue().after(to)) {
                break;
            }
            Calendar curDate = date.getValue();
            int curMonth = curDate.get(Calendar.MONTH);
            if (curMonth != lastMonth) {
                dayOfMonth = 0;
                for (int i = 0; i < weeklyDayOfMonth.length; i++) {
                    weeklyDayOfMonth[i] = 0;
                }
                lastMonth = curMonth;
            }
            if (curDate.before(from)) {
                continue;
            }
            if (days != null) {
                dayOfMonth++;
                if (days.contains(dayOfMonth)) {
                    dates.add(date.getKey());
                }
            }
            if (weeklyDays != null) {
                int curDayOfWeek = curDate.get(Calendar.DAY_OF_WEEK) - 1;
                weeklyDay.setDay(curDayOfWeek);
                weeklyDayOfMonth[curDayOfWeek] = weeklyDayOfMonth[curDayOfWeek] + 1;
                weeklyDay.setWeekOfMonth(weeklyDayOfMonth[curDayOfWeek]);
                if (weeklyDays.contains(weeklyDay)) {
                    dates.add(date.getKey());
                }
            }
        }
        Map<String, Calendar> tmpDatesWithoutRestrictions = new HashMap<String, Calendar>(datesWithoutRestrictions);
        for (Entry<String, Calendar> entry : tmpDatesWithoutRestrictions.entrySet()) {
            if (entry.getValue().before(from) || entry.getValue().after(to)) {
                continue;
            }
            datesWithoutRestrictions.remove(entry.getKey());
        }
        return dates;
    }
    
    private Map<String, Calendar> resolveUltimos(List<Integer> days, List<WeeklyDay> weeklyDays, Calendar from,
            Calendar to) throws SOSInvalidDataException {

        Map<String, Calendar> dates = new HashMap<String, Calendar>();
        WeeklyDay weeklyDay = new WeeklyDay();

        while (from.compareTo(to) <= 0) {
            if (days != null) {
                int dayOfUltimo = from.getActualMaximum(Calendar.DAY_OF_MONTH) + 1 - from.get(Calendar.DAY_OF_MONTH);
                if (days.contains(dayOfUltimo)) {
                    dates.put(df.format(from.toInstant()), (Calendar) from.clone());
                }
            }
            if (weeklyDays != null) {
                weeklyDay.setDay(from.get(Calendar.DAY_OF_WEEK) - 1);
                weeklyDay.setWeekOfMonth(getWeekOfMonthOfUltimoWeeklyDay(from));
                if (weeklyDays.contains(weeklyDay)) {
                    dates.put(df.format(from.toInstant()), (Calendar) from.clone());
                }
            }
            from.add(Calendar.DATE, 1);
        }
        return dates;
    }
    
    private Set<String> resolveBasedOnUltimos(List<Integer> days, List<WeeklyDay> weeklyDays, Calendar from, Calendar to) {
        
        SortedMap<String, Calendar> reverseDates = new TreeMap<String, Calendar>(new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                return o2.compareTo(o1);
            }
        });
        reverseDates.putAll(this.dates);
        Set<String> dates = new HashSet<String>();
        WeeklyDay weeklyDay = new WeeklyDay();
        int dayOfMonth = 0;
        int lastMonth = -1;
        int[] weeklyDayOfMonth = {0,0,0,0,0,0,0};
        
        for (Entry<String, Calendar> date : reverseDates.entrySet()) {
            if (date == null || date.getValue() == null) {
                continue;
            }
            if (date.getValue().before(from)) {
                break;
            }
            Calendar curDate = date.getValue();
            int curMonth = curDate.get(Calendar.MONTH);
            if (curMonth != lastMonth) {
                dayOfMonth = 0;
                for (int i = 0; i < weeklyDayOfMonth.length; i++) {
                    weeklyDayOfMonth[i] = 0;
                }
                lastMonth = curMonth;
            }
            if (curDate.after(to)) {
                continue;
            }
            if (days != null) {
                dayOfMonth++;
                if (days.contains(dayOfMonth)) {
                    dates.add(date.getKey());
                }
            }
            if (weeklyDays != null) {
                int curDayOfWeek = curDate.get(Calendar.DAY_OF_WEEK) - 1;
                weeklyDay.setDay(curDayOfWeek);
                weeklyDayOfMonth[curDayOfWeek] = weeklyDayOfMonth[curDayOfWeek] + 1;
                weeklyDay.setWeekOfMonth(weeklyDayOfMonth[curDayOfWeek]);
                if (weeklyDays.contains(weeklyDay)) {
                    dates.add(date.getKey());
                }
            }
        }
        Map<String, Calendar> tmpDatesWithoutRestrictions = new HashMap<String, Calendar>(datesWithoutRestrictions);
        for (Entry<String, Calendar> entry : tmpDatesWithoutRestrictions.entrySet()) {
            if (entry.getValue().before(from) || entry.getValue().after(to)) {
                continue;
            }
            datesWithoutRestrictions.remove(entry.getKey());
        }
        return dates;
    }

    private Map<String, Calendar> resolveRepetitions(RepetitionText repetition, Integer step, Calendar calFrom, Calendar from, Calendar to)
            throws SOSInvalidDataException {
        if (repetition == null) {
            throw new SOSInvalidDataException("json field 'repetition' in 'repetitions' is undefined.");
        }
        if (step == null) {
            step = 1;
        }
        Map<String, Calendar> dates = new HashMap<String, Calendar>();
        int dayOfMonth = calFrom.get(Calendar.DAY_OF_MONTH);

        while (calFrom.compareTo(to) <= 0) {
            if (calFrom.compareTo(from) >= 0) {
                dates.put(df.format(calFrom.toInstant()), (Calendar) calFrom.clone());
            }
            switch (repetition) {
            case DAILY:
                calFrom.add(Calendar.DATE, step);
                break;
            case MONTHLY:
                calFrom.add(Calendar.MONTH, step);
                if (dayOfMonth > calFrom.get(Calendar.DAY_OF_MONTH) && calFrom.getActualMaximum(Calendar.DAY_OF_MONTH) >= dayOfMonth) {
                    calFrom.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                }
                break;
            case WEEKLY:
                calFrom.add(Calendar.DATE, (step * 7));
                break;
            case YEARLY:
                calFrom.add(Calendar.YEAR, step);
                // if original 'from' was 29th of FEB
                if (dayOfMonth > calFrom.get(Calendar.DAY_OF_MONTH) && calFrom.getActualMaximum(Calendar.DAY_OF_MONTH) >= dayOfMonth) {
                    calFrom.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                }
                break;
            }
        }
        return dates;
    }
    
    private Set<String> resolveBasedOnRepetitions(RepetitionText repetition, Integer step, Calendar calFrom, Calendar from, Calendar to)
            throws SOSInvalidDataException {
        if (repetition == null) {
            throw new SOSInvalidDataException("json field 'repetition' in 'repetitions' is undefined.");
        }
        if (step == null) {
            step = 1;
        }
        Set<String> dates = new HashSet<String>();
        int refDayOfMonth = -1;
        int refWeekDay = -1;
        int refMonth = -1;
        int curStep = 0;
        
        
        for (Entry<String, Calendar> date : this.dates.entrySet()) {
            if (date == null || date.getValue() == null) {
                continue;
            }
            if (date.getValue().after(to)) {
                break;
            }
            Calendar curDate = date.getValue();
            if (curDate.before(calFrom)) {
                continue;
            } else if (refDayOfMonth == -1) {
                refDayOfMonth = curDate.get(Calendar.DAY_OF_MONTH);
                refWeekDay = curDate.get(Calendar.DAY_OF_WEEK);
                refMonth = curDate.get(Calendar.MONTH);
            }
            if (curDate.before(from)) {
                continue;
            }
            switch (repetition) {
            case DAILY:
                if (curStep % step == 0) {
                    dates.add(date.getKey());
                }
                curStep++;
                break;
            case MONTHLY:
                if (refDayOfMonth > -1) {
                    int curDayOfMonth = curDate.get(Calendar.DAY_OF_MONTH);
                    if (refDayOfMonth == curDayOfMonth) {
                        if (curStep % step == 0) {
                            dates.add(date.getKey());
                        }
                        curStep++;
                    } else if (refDayOfMonth > curDate.getActualMaximum(Calendar.DAY_OF_MONTH) && curDayOfMonth == curDate.getActualMaximum(
                            Calendar.DAY_OF_MONTH)) {
                        if (curStep % step == 0) {
                            dates.add(date.getKey());
                        }
                        curStep++;
                    }
                }
                break;
            case WEEKLY:
                if (refWeekDay > -1) {
                    int curWeekDay = curDate.get(Calendar.DAY_OF_WEEK);
                    if (refWeekDay == curWeekDay) {
                        if (curStep % step == 0) {
                            dates.add(date.getKey());
                        }
                        curStep++;
                    }
                }
                break;
            case YEARLY:
                if (refDayOfMonth > -1) {
                    int curDayOfMonth = curDate.get(Calendar.DAY_OF_MONTH);
                    int curMonth = curDate.get(Calendar.MONTH);
                    if (curMonth == refMonth) {
                        if (curDayOfMonth == refDayOfMonth) {
                            if (curStep % step == 0) {
                                dates.add(date.getKey());
                            }
                            curStep++;
                        } else if (refMonth == Calendar.FEBRUARY && refDayOfMonth == 29 && curDayOfMonth == 28) {
                            if (curStep % step == 0) {
                                dates.add(date.getKey());
                            }
                            curStep++;
                        }
                    }
                }
                break;
            }
        }
        Map<String, Calendar> tmpDatesWithoutRestrictions = new HashMap<String, Calendar>(datesWithoutRestrictions);
        for (Entry<String, Calendar> entry : tmpDatesWithoutRestrictions.entrySet()) {
            if (entry.getValue().before(from) || entry.getValue().after(to)) {
                continue;
            }
            datesWithoutRestrictions.remove(entry.getKey());
        }
        return dates;
    }

    private Calendar getTodayCalendar() {
        // use today at 00:00:00.000 as default
        Calendar cal = Calendar.getInstance();
        cal.setTime(Date.from(Instant.now()));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }
    
    private void addDatesRestrictions() throws SOSInvalidDataException {
        if (includes != null && includes.getDates() != null && !includes.getDates().isEmpty()) {
            for (Entry<String, Calendar> date : this.dates.entrySet()) {
                if (date == null || date.getValue() == null) {
                    continue;
                }
                if (date.getValue().after(dateTo)) {
                    break;
                }
                if (date.getValue().before(dateFrom)) {
                    continue;
                }
                if (includes.getDates().contains(date.getKey())) {
                    restrictions.add(date.getKey());
                }
            }
            datesWithoutRestrictions.clear();
        }
    }

    private void addWeekDaysRestrictions() throws SOSInvalidDataException {
        if (includes != null) {
            addWeekDaysRestrictions(includes.getWeekdays(), dateFrom, dateTo);
        }
    }
    
    private void addWeekDaysRestrictions(List<WeekDays> weekDays, Calendar from, Calendar to) throws SOSInvalidDataException {
        if (weekDays != null) {
            for (WeekDays weekDay : weekDays) {
                addRestrictions(resolveBasedOnWeekDays(weekDay.getDays(), getFrom(weekDay.getFrom(), from), getTo(weekDay.getTo(), to)));
            }
        }
    }
    
    private void addMonthDaysRestrictions() throws SOSInvalidDataException {
        if (includes != null) {
            addMonthDaysRestrictions(includes.getMonthdays(), dateFrom, dateTo);
        }
    }
    
    private void addMonthDaysRestrictions(List<MonthDays> monthDays, Calendar from, Calendar to) throws SOSInvalidDataException {
        if (monthDays != null) {
            for (MonthDays monthDay : monthDays) {
                addRestrictions(resolveBasedOnMonthDays(monthDay.getDays(), monthDay.getWeeklyDays(), getFrom(monthDay.getFrom(), from), getTo(
                        monthDay.getTo(), to)));
            }
        }
    }
    
    private void addUltimosRestrictions() throws SOSInvalidDataException {
        if (includes != null && includes.getUltimos() != null) {
            addUltimosRestrictions(includes.getUltimos(), dateFrom, dateTo);
        }
    }
    
    private void addUltimosRestrictions(List<MonthDays> ultimos, Calendar from, Calendar to) throws SOSInvalidDataException {
        if (ultimos != null) {
            for (MonthDays ultimo : ultimos) {
                addRestrictions(resolveBasedOnUltimos(ultimo.getDays(), ultimo.getWeeklyDays(), getFrom(ultimo.getFrom(), from), getTo(ultimo.getTo(),
                        to)));
            }
        }
    }
    
    private void addMonthsRestrictions() throws SOSInvalidDataException {
        if (includes != null && includes.getMonths() != null) {
            Calendar monthStart = Calendar.getInstance();
            Calendar monthEnd = Calendar.getInstance();
            for (Months month : includes.getMonths()) {
                if (month.getMonths() != null) {
                    Calendar from = getFrom(month.getFrom());
                    Calendar to = getTo(month.getTo());
                    while (from.compareTo(to) <= 0) {
                        int lastDayOfMonth = from.getActualMaximum(Calendar.DAY_OF_MONTH);
                        if (month.getMonths().contains(from.get(Calendar.MONTH) + 1)) {
                            Calendar monthFrom = getFromPerMonth(monthStart, from);
                            Calendar monthTo = getFromToMonth(monthEnd, from, to, lastDayOfMonth);
                            addWeekDaysRestrictions(month.getWeekdays(), monthFrom, monthTo);
                            addMonthDaysRestrictions(month.getMonthdays(), monthFrom, monthTo);
                            addUltimosRestrictions(month.getUltimos(), monthFrom, monthTo);

                        }
                        from.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);
                        from.add(Calendar.DATE, 1);
                    }
                }
            }
        }
    }
    
    private void addRepetitionsRestrictions() throws SOSInvalidDataException {
        if (includes != null && includes.getRepetitions() != null) {
            for (Repetition repetition : includes.getRepetitions()) {
                addRestrictions(resolveBasedOnRepetitions(repetition.getRepetition(), repetition.getStep(), getFrom(repetition.getFrom(), calendarFrom), getFrom(
                        repetition.getFrom()), getTo(repetition.getTo())));
            }
        }
    }


}
