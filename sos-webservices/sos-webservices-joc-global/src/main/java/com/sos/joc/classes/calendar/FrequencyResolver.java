package com.sos.joc.classes.calendar;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.calendar.Frequencies;
import com.sos.inventory.model.calendar.Holidays;
import com.sos.inventory.model.calendar.MonthDays;
import com.sos.inventory.model.calendar.Repetition;
import com.sos.inventory.model.calendar.WeekDays;
import com.sos.inventory.model.calendar.WeeklyDay;
import com.sos.joc.model.calendar.CalendarDatesFilter;
import com.sos.joc.model.calendar.Dates;

public class FrequencyResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrequencyResolver.class);
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final DateTimeFormatter DF = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC);

    private SortedMap<String, Calendar> dates = new TreeMap<String, Calendar>();
    private SortedMap<String, Calendar> datesWithoutRestrictions = new TreeMap<String, Calendar>();
    private SortedSet<String> restrictions = new TreeSet<String>();
    private SortedSet<String> withExcludes = new TreeSet<String>();

    private Calendar calendarFrom = null;
    private Calendar dateFrom = null;
    private Calendar dateTo = null;

    private Frequencies baseCalendarIncludes = null;
    private Frequencies baseCalendarExcludes = null;
    private Frequencies includes = null;
    private Frequencies excludes = null;

    public FrequencyResolver() {
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

    public Dates resolve(com.sos.inventory.model.calendar.Calendar calendar, String from, String to) throws SOSMissingDataException,
            SOSInvalidDataException {
        init(calendar, from, to);
        Dates d = new Dates();
        d.setDates(new ArrayList<String>());
        datesWithoutRestrictions.clear();
        restrictions.clear();
        withExcludes.clear();
        dates.clear();
        if (this.dateFrom.compareTo(this.dateTo) <= 0) {
            // includes
            if (includes != null) {
                addDates(includes.getDates());
                addHolidays(includes.getHolidays());
                addWeekDays(includes.getWeekdays());
                addMonthDays(includes.getMonthdays());
                addUltimos(includes.getUltimos());
                addRepetitions(includes.getRepetitions());
            }
            // excludes
            if (excludes != null) {
                if (dates.size() > 0) {
                    removeDates(excludes.getDates());
                    if (dates.size() > 0) {
                        removeWeekDays(excludes.getWeekdays());
                        if (dates.size() > 0) {
                            removeMonthDays(excludes.getMonthdays());
                            if (dates.size() > 0) {
                                removeUltimos(excludes.getUltimos());
                                if (dates.size() > 0) {
                                    removeHolidays(excludes.getHolidays());
                                    if (dates.size() > 0) {
                                        removeRepetitions(excludes.getRepetitions());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // cleanup
            removeRepetitionsBasedOnCalendarFrom();

            d.getDates().addAll(dates.keySet());
            d.setWithExcludes(new ArrayList<String>(withExcludes));
        } else {
            d.setDates(new ArrayList<String>());
        }
        d.setDeliveryDate(Date.from(Instant.now()));
        return d;
    }

    private void removeRepetitionsBasedOnCalendarFrom() {
        if (calendarFrom == null || dates.size() == 0) {
            return;
        }
        try {
            Calendar minFrom = getMinRepetitionFrom();
            if (minFrom != null && minFrom.before(calendarFrom)) {
                List<String> r = dates.entrySet().stream().filter(e -> {
                    return e.getValue().before(calendarFrom);
                }).map(e -> {
                    return e.getKey();
                }).collect(Collectors.toList());

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[removeRepetitionsBasedOnCalendarFrom][minRepetitionFrom=%s][calendarFrom=%s][to remove][size=%s]%s",
                            SOSDate.getDateTimeAsString(minFrom), SOSDate.getDateTimeAsString(calendarFrom), r.size(), stringJoin(r)));
                }
                for (String key : r) {
                    dates.remove(key);
                }
            }
        } catch (SOSInvalidDataException e) {
            LOGGER.error(e.toString(), e);
        }
    }

    private String stringJoin(List<String> l) {
        if (l == null) {
            return "";
        }
        try {
            int max = 356;
            return l.size() > max ? new StringBuilder(l.get(0) + ",...,").append(String.join(",", l.subList(max, l.size()))).toString() : String.join(
                    ",", l);
        } catch (Throwable e) {
            return l.toString();
        }
    }

    private Calendar getMinRepetitionFrom() throws SOSInvalidDataException {
        if (includes.getRepetitions() == null || includes.getRepetitions().size() == 0) {
            return null;
        }
        Calendar c = null;
        for (Repetition r : includes.getRepetitions()) {
            Calendar rc = getCalendar(r.getFrom());
            if (c == null) {
                c = rc;
            } else if (rc.before(c)) {
                c = rc;
            }
        }
        return c;
    }

    public Dates resolveRestrictions(com.sos.inventory.model.calendar.Calendar baseCalendar,
            com.sos.inventory.model.calendar.Calendar restrictionsCalendar, String from, String to) throws SOSMissingDataException,
            SOSInvalidDataException {
        init(baseCalendar, from, to);
        Dates d = new Dates();
        d.setDates(new ArrayList<String>());
        datesWithoutRestrictions.clear();
        restrictions.clear();
        withExcludes.clear();
        dates.clear();

        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        String method = "resolveRestrictions";
        int diff = this.dateFrom.compareTo(this.dateTo);
        if (diff <= 0) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][start][from=%s, to=%s]dateFrom=%s compareTo dateTo=%s <= 0 (%s)", method, from, to, SOSDate
                        .getDateTimeAsString(this.dateFrom), SOSDate.getDateTimeAsString(this.dateTo), diff));
            }
            if (includes != null) {
                addDates(includes.getDates());
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][after][addDates]dates=%s", method, dates.keySet()));
                }

                addHolidays(includes.getHolidays());
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][after][addHolidays]dates=%s", method, dates.keySet()));
                }

                addWeekDays(includes.getWeekdays());
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][after][addWeekDays]dates=%s", method, dates.keySet()));
                }

                addMonthDays(includes.getMonthdays());
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][after][addMonthDays]dates=%s", method, dates.keySet()));
                }

                addUltimos(includes.getUltimos());
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][after][addUltimos]dates=%s", method, dates.keySet()));
                }

                addRepetitions(includes.getRepetitions());
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][after][addRepetitions]dates=%s", method, dates.keySet()));
                }
            }

            if (excludes != null) {
                if (dates.size() > 0) {
                    removeDates(excludes.getDates());
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][after][removeDates]dates=%s", method, dates.keySet()));
                    }
                    if (dates.size() > 0) {
                        removeWeekDays(excludes.getWeekdays());
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][after][removeWeekDays]dates=%s", method, dates.keySet()));
                        }
                        if (dates.size() > 0) {
                            removeMonthDays(excludes.getMonthdays());
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[%s][after][removeMonthDays]dates=%s", method, dates.keySet()));
                            }
                            if (dates.size() > 0) {
                                removeUltimos(excludes.getUltimos());
                                if (isDebugEnabled) {
                                    LOGGER.debug(String.format("[%s][after][removeUltimos]dates=%s", method, dates.keySet()));
                                }
                                if (dates.size() > 0) {
                                    removeHolidays(excludes.getHolidays());
                                    if (isDebugEnabled) {
                                        LOGGER.debug(String.format("[%s][after][removeHolidays]dates=%s", method, dates.keySet()));
                                    }
                                    if (dates.size() > 0) {
                                        removeRepetitions(excludes.getRepetitions());
                                        if (isDebugEnabled) {
                                            LOGGER.debug(String.format("[%s][after][removeRepetitions]dates=%s", method, dates.keySet()));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isDebugEnabled) {
                List<String> dc = this.dates.entrySet().stream().map(e -> {
                    return e.getKey();
                }).collect(Collectors.toList());
                LOGGER.debug(String.format("[%s][provisional]dates=%s, restrictionsCalendar is null=%s", method, String.join(",", dc),
                        restrictionsCalendar == null));
            }

            if (restrictionsCalendar != null && !this.dates.isEmpty()) {
                this.dateFrom = getFrom(from);
                this.dateTo = getTo(to);
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][restrictions]dateFrom=%s, dateTo=%s", method, SOSDate.getDateTimeAsString(this.dateFrom), SOSDate
                            .getDateTimeAsString(this.dateTo)));
                }
                for (Entry<String, Calendar> entry : dates.entrySet()) {
                    if (entry.getValue().before(dateFrom)) {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][restrictions][datesWithoutRestrictions][skip]%s is before dateFrom=%s", method, SOSDate
                                    .getDateTimeAsString(entry.getValue()), SOSDate.getDateTimeAsString(dateFrom)));
                        }
                        continue;
                    }
                    if (entry.getValue().after(dateTo)) {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][restrictions][datesWithoutRestrictions][skip]%s is after dateTo=%s", method, SOSDate
                                    .getDateTimeAsString(entry.getValue()), SOSDate.getDateTimeAsString(dateTo)));
                        }
                        break;
                    }
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][restrictions][datesWithoutRestrictions][put]%s", method, entry.getKey()));
                    }
                    datesWithoutRestrictions.put(entry.getKey(), entry.getValue());
                }

                diff = this.dateFrom.compareTo(this.dateTo);
                if (diff <= 0) {
                    Calendar dateFromOrig = (Calendar) this.dateFrom.clone();
                    this.dateFrom = getFirstDayOfMonthCalendar(this.dateFrom);
                    this.includes = restrictionsCalendar.getIncludes();

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][restrictions][from=%s, to=%s]dateFrom=%s compareTo dateTo=%s <= 0 (%s)", method, from, to,
                                SOSDate.getDateTimeAsString(this.dateFrom), SOSDate.getDateTimeAsString(this.dateTo), diff));
                        LOGGER.debug(String.format("[%s][restrictions]dateFrom=%s (getFirstDayOfMonthCalendar)", method, SOSDate.getDateTimeAsString(
                                this.dateFrom)));
                    }
                    // this.excludes = calendar.getExcludes(); //TODO exists?
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][restrictions][start]datesWithoutRestrictions=%s", method, datesWithoutRestrictions
                                .keySet()));
                    }
                    addDatesRestrictions(dateFromOrig);
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][restrictions][after][addDatesRestrictions]datesWithoutRestrictions=%s", method,
                                datesWithoutRestrictions.keySet()));
                    }

                    addWeekDaysRestrictions(dateFromOrig);
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][restrictions][after][addWeekDaysRestrictions]datesWithoutRestrictions=%s", method,
                                datesWithoutRestrictions.keySet()));
                    }

                    addMonthDaysRestrictions(dateFromOrig);
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][restrictions][after][addMonthDaysRestrictions]datesWithoutRestrictions=%s", method,
                                datesWithoutRestrictions.keySet()));
                    }

                    addUltimosRestrictions(dateFromOrig);
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][restrictions][after][addUltimosRestrictions]datesWithoutRestrictions=%s", method,
                                datesWithoutRestrictions.keySet()));
                    }

                    addRepetitionsRestrictions(dateFromOrig);
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][restrictions][after][addRepetitionsRestrictions]datesWithoutRestrictions=%s", method,
                                datesWithoutRestrictions.keySet()));
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][restrictions][skip][from=%s, to=%s]dateFrom=%s compareTo dateTo=%s > 0 (%s)", method, from,
                                to, SOSDate.getDateTimeAsString(this.dateFrom), SOSDate.getDateTimeAsString(this.dateTo), diff));
                    }
                }
            }
            restrictions.addAll(datesWithoutRestrictions.keySet());
            d.getDates().addAll(restrictions);
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][skip][from=%s, to=%s]dateFrom=%s compareTo dateTo=%s > 0 (%s)", method, from, to, SOSDate
                        .getDateTimeAsString(this.dateFrom), SOSDate.getDateTimeAsString(this.dateTo), diff));
            }
        }
        d.setDeliveryDate(Date.from(Instant.now()));
        return d;
    }

    private void init(com.sos.inventory.model.calendar.Calendar baseCalendar, String from, String to) throws SOSMissingDataException,
            SOSInvalidDataException {
        if (baseCalendar != null) {
            setDateFrom(from, baseCalendar.getFrom());
            setDateTo(to, baseCalendar.getTo());
            this.baseCalendarIncludes = baseCalendar.getIncludes();
            this.baseCalendarExcludes = baseCalendar.getExcludes();
            this.includes = baseCalendar.getIncludes();
            this.excludes = baseCalendar.getExcludes();
        } else {
            throw new SOSMissingDataException("calendar object is undefined");
        }
    }

    private void setDateFrom(String dateFrom, String calendarFrom) throws SOSMissingDataException, SOSInvalidDataException {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[setDateFrom][start]dateFrom=%s,calendarFrom=%s", dateFrom, calendarFrom));
        }
        if (calendarFrom == null || calendarFrom.isEmpty()) {// TODO will be overwritten when dateFrom is not empty - move to the dateFrom==null section ?
            this.calendarFrom = getTodayCalendar();
            // calendarFrom = df.format(this.calendarFrom.toInstant());

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[setDateFrom]set calendarFrom=%s from getTodayCalendar because calendarFrom is null", SOSDate
                        .getDateTimeAsString(this.calendarFrom)));
            }
        }

        if ((dateFrom == null || dateFrom.isEmpty())) {
            this.dateFrom = getTodayCalendar();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[setDateFrom]set dateFrom=%s from getTodayCalendar because dateFrom is null", SOSDate.getDateTimeAsString(
                        this.dateFrom)));
            }
        } else {

            Calendar calFrom = getCalendarFromString(calendarFrom, "calendar field 'from' must have the format YYYY-MM-DD.");
            Calendar dFrom = getCalendarFromString(dateFrom, "'dateFrom' parameter must have the format YYYY-MM-DD.");

            if (calFrom == null) {
                LOGGER.debug("[setDateFrom]set dateFrom from dFrom because calFrom is null");
                this.dateFrom = dFrom;
            } else if (dFrom == null) {
                LOGGER.debug("[setDateFrom]set dateFrom from calFrom because dFrom is null");
                this.dateFrom = calFrom;
            } else if (calFrom.before(dFrom)) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[setDateFrom]set dateFrom from dFrom because calFrom %s is before dFrom %s ", SOSDate
                            .getDateTimeAsString(calFrom), SOSDate.getDateTimeAsString(dFrom)));
                }
                this.dateFrom = dFrom;
            } else {
                LOGGER.debug("[setDateFrom]set dateFrom from calFrom");
                this.dateFrom = calFrom;
            }
            if (calFrom == null) {
                this.calendarFrom = (Calendar) dFrom.clone();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[setDateFrom]set calendarFrom=%s from dFrom because calFrom is null", SOSDate.getDateTimeAsString(
                            this.calendarFrom)));
                }
            } else {
                this.calendarFrom = (Calendar) calFrom.clone();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[setDateFrom]set calendarFrom=%s from calFrom", SOSDate.getDateTimeAsString(this.calendarFrom)));
                }
            }
        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[setDateFrom]dateFrom=%s, calendarFrom=%s", SOSDate.getDateTimeAsString(this.dateFrom), SOSDate
                    .getDateTimeAsString(this.calendarFrom)));
        }
    }

    private void setDateTo(String dateTo, String calendarTo) throws SOSMissingDataException, SOSInvalidDataException {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        if ((dateTo == null || dateTo.isEmpty()) && (calendarTo == null || calendarTo.isEmpty())) {
            // throw new SOSMissingDataException("'dateTo' parameter and calendar field 'to' are undefined.");
            this.dateTo = getLastDayOfCurrentYearCalendar();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[setDateTo]set dateTo=%s from getLastDayOfCurrentYearCalendar because dateTo & calendarTo are empty",
                        SOSDate.getDateTimeAsString(this.dateTo)));
            }
        } else {

            Calendar calTo = getCalendarFromString(calendarTo, "calendar field 'to' must have the format YYYY-MM-DD.");
            Calendar dTo = getCalendarFromString(dateTo, "'dateTo' parameter must have the format YYYY-MM-DD.");
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[setDateTo]calTo=%s, dTo=%s", SOSDate.getDateTimeAsString(calTo), SOSDate.getDateTimeAsString(dTo)));
            }

            if (calTo == null) {
                LOGGER.debug("[setDateTo]set dateTo from dTo because calTo is null");
                this.dateTo = dTo;
            } else if (dTo == null) {
                LOGGER.debug("[setDateTo]set dateTo from calTo because dTo is null");
                this.dateTo = calTo;
            } else if (calTo.after(dTo)) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[setDateTo]set dateTo from dTo because calTo %s is after dTo %s ", SOSDate.getDateTimeAsString(calTo),
                            SOSDate.getDateTimeAsString(dTo)));
                }
                this.dateTo = dTo;
            } else {
                LOGGER.debug("[setDateTo]set dateTo from calTo");
                this.dateTo = calTo;
            }
        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[setDateTo]dateTo=%s", SOSDate.getDateTimeAsString(this.dateTo)));
        }
    }

    public String getToday() {
        return DF.format(Instant.now());
    }

    public String getLastDayOfCurrentYear() throws SOSInvalidDataException {
        return DF.format(getLastDayOfCurrentYearCalendar().toInstant());
    }

    private Calendar getLastDayOfCurrentYearCalendar() throws SOSInvalidDataException {
        Calendar calendar = Calendar.getInstance(UTC);
        calendar.set(calendar.get(Calendar.YEAR), 11, 31, 12, 0, 0);
        return calendar;
    }

    public static Calendar getCalendarFromString(String cal) throws SOSInvalidDataException {
        return getCalendarFromString(cal, "dates must have the format YYYY-MM-DD.");
    }

    private static Calendar getCalendarFromString(String cal, String msg) throws SOSInvalidDataException {
        Calendar calendar = null;
        if (cal != null && !cal.isEmpty()) {
            if (!cal.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                throw new SOSInvalidDataException(msg);
            }
            calendar = Calendar.getInstance(UTC);
            calendar.setTime(Date.from(Instant.parse(cal + "T12:00:00Z")));
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
                addAll(resolveMonthDays(monthDay.getDays(), monthDay.getWeeklyDays(), getFrom(monthDay.getFrom(), from), getTo(monthDay.getTo(),
                        to)));
            }
        }
    }

    private void removeMonthDays(List<MonthDays> monthDays) throws SOSInvalidDataException {
        removeMonthDays(monthDays, dateFrom, dateTo);
    }

    private void removeMonthDays(List<MonthDays> monthDays, Calendar from, Calendar to) throws SOSInvalidDataException {
        if (monthDays != null) {
            for (MonthDays monthDay : monthDays) {
                removeAll(resolveMonthDays(monthDay.getDays(), monthDay.getWeeklyDays(), getFrom(monthDay.getFrom(), from), getTo(monthDay.getTo(),
                        to)));
            }
        }
    }

    private void addUltimos(List<MonthDays> monthDays) throws SOSInvalidDataException {
        addUltimos(monthDays, dateFrom, dateTo);
    }

    private void addUltimos(List<MonthDays> ultimos, Calendar from, Calendar to) throws SOSInvalidDataException {
        if (ultimos != null) {
            for (MonthDays ultimo : ultimos) {
                addAll(resolveUltimos(ultimo.getDays(), ultimo.getWeeklyDays(), getFrom(ultimo.getFrom(), from), getTo(ultimo.getTo(), to)));
            }
        }
    }

    private void removeUltimos(List<MonthDays> monthDays) throws SOSInvalidDataException {
        removeUltimos(monthDays, dateFrom, dateTo);
    }

    private void removeUltimos(List<MonthDays> ultimos, Calendar from, Calendar to) throws SOSInvalidDataException {
        if (ultimos != null) {
            for (MonthDays ultimo : ultimos) {
                removeAll(resolveUltimos(ultimo.getDays(), ultimo.getWeeklyDays(), getFrom(ultimo.getFrom(), from), getTo(ultimo.getTo(), to)));
            }
        }
    }

    private void addRepetitions(List<Repetition> repetitions) throws SOSInvalidDataException {
        if (repetitions != null) {
            for (Repetition repetition : repetitions) {
                addAll(resolveRepetitions(repetition));
            }
        }
    }

    private void removeRepetitions(List<Repetition> repetitions) throws SOSInvalidDataException {
        if (repetitions != null) {
            for (Repetition repetition : repetitions) {
                removeAll(resolveRepetitions(repetition));
            }
        }
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

    private Calendar getCalendar(String from) throws SOSInvalidDataException {
        Calendar cal = Calendar.getInstance(UTC);
        if (SOSString.isEmpty(from) || !from.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            throw new SOSInvalidDataException("json field 'from' must have the format YYYY-MM-DD.");
        }
        cal.setTime(Date.from(Instant.parse(from + "T12:00:00Z")));
        return cal;
    }

    private Calendar getFrom(String from, Calendar fromRef) throws SOSInvalidDataException {
        Calendar cal = Calendar.getInstance(UTC);
        if (from == null || from.isEmpty()) {
            return (Calendar) fromRef.clone();
        }
        if (!from.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            throw new SOSInvalidDataException("json field 'from' must have the format YYYY-MM-DD.");
        }
        cal.setTime(Date.from(Instant.parse(from + "T12:00:00Z")));
        if (cal.after(fromRef)) {
            return cal;
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
        Calendar cal = Calendar.getInstance(UTC);
        cal.setTime(Date.from(Instant.parse(to + "T12:00:00Z")));
        if (cal.before(toRef)) {
            return cal;
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
                dates.put(DF.format(from.toInstant()), (Calendar) from.clone());
            }
            from.add(Calendar.DATE, 1);
        }
        return dates;
    }

    private Set<String> resolveWeekDaysRestrictions(List<Integer> days, Calendar from, Calendar to, Calendar dateFromOrig)
            throws SOSInvalidDataException {
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
            Calendar curDate = date.getValue();
            if (curDate.after(to)) {
                break;
            }
            if (curDate.before(from)) {
                continue;
            }
            if (curDate.before(dateFromOrig)) {
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
                    dates.put(DF.format(from.toInstant()), (Calendar) from.clone());
                }
            }
            if (weeklyDays != null) {
                weeklyDay.setDay(from.get(Calendar.DAY_OF_WEEK) - 1);
                weeklyDay.setWeekOfMonth(getWeekOfMonthOfWeeklyDay(from));
                if (weeklyDays.contains(weeklyDay)) {
                    dates.put(DF.format(from.toInstant()), (Calendar) from.clone());
                }
            }
            from.add(Calendar.DATE, 1);
        }
        return dates;
    }

    private Set<String> resolveMonthDaysRestrictions(List<Integer> days, List<WeeklyDay> weeklyDays, Calendar from, Calendar to,
            Calendar dateFromOrig) throws SOSInvalidDataException {

        String method = "resolveMonthDaysRestrictions";
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        String debugDays = null;
        String debugWeeklyDays = null;
        if (isDebugEnabled) {
            debugDays = days == null ? "" : String.join(",", days.stream().map(e -> {
                return e.toString();
            }).collect(Collectors.toList()));
            debugWeeklyDays = weeklyDays == null ? "" : String.join(",", weeklyDays.stream().map(e -> {
                return new StringBuilder("[day=").append(e.getDay()).append(",weekOfMonth=").append(e.getWeekOfMonth()).append("]").toString();
            }).collect(Collectors.toList()));
            LOGGER.debug(String.format("[%s][start][this.dateFrom=%s, from=%s, to=%s, dateFromOrig=%s][days=%s][weeklyDays=%s]", method, SOSDate
                    .getDateTimeAsString(this.dateFrom), SOSDate.getDateTimeAsString(from), SOSDate.getDateTimeAsString(to), SOSDate
                            .getDateTimeAsString(dateFromOrig), debugDays, debugWeeklyDays));
        }

        Set<String> dates = new HashSet<String>();
        int dayOfMonth = 0;
        int lastMonth = -1;
        int dateFromMonth = this.dateFrom.get(Calendar.MONTH);
        boolean dateFromOrigAfterDateFrom = dateFromOrig.after(this.dateFrom);
        boolean missingDaysResolved = false;
        boolean missingDaysCurrentDayFound = false;
        for (Entry<String, Calendar> date : this.dates.entrySet()) {
            if (date == null || date.getValue() == null) {
                continue;
            }
            if (date.getValue().after(to)) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][break]is after to=%s", method, SOSDate.getDateTimeAsString(date.getValue()), SOSDate
                            .getDateTimeAsString(to)));
                }
                break;
            }
            Calendar curDate = date.getValue();
            int curMonth = curDate.get(Calendar.MONTH);
            if (curMonth != lastMonth) {
                dayOfMonth = 0;
                lastMonth = curMonth;
            }
            if (curDate.before(from)) {
                dayOfMonth++;
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][skip][dayOfMonth=%s]is before from=%s", method, SOSDate.getDateTimeAsString(curDate),
                            dayOfMonth, SOSDate.getDateTimeAsString(from)));
                }
                continue;
            }
            if (days != null) {
                // this.dateFrom - first day of month
                // from - first day of month or the restriction "from"
                // dateFromOrig - is the original from
                // 2021-02-03 after 2021-02-01
                if (!missingDaysResolved && baseCalendarIncludes != null && ((dateFromOrigAfterDateFrom && dateFromMonth == curMonth)
                        || hasRepetitions(baseCalendarIncludes))) {
                    missingDaysResolved = true;
                    List<String> missingDays = getDatesFromIncludes(this.dateFrom, dateFromOrig, false);
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][fromFirstDayOfMonth][start][from=%s, to=%s]%s", method, SOSDate.getDateTimeAsString(
                                curDate), SOSDate.getDateTimeAsString(this.dateFrom), SOSDate.getDateTimeAsString(dateFromOrig), String.join(",",
                                        missingDays)));
                    }

                    dayOfMonth = 0;
                    String lastYearMonth = null;// TODO check if not more needed because && dateFromMonth == curMonth added
                    String currentYearMonth;
                    for (String missingDay : missingDays) {
                        currentYearMonth = missingDay.substring(0, 7);// 2022-02
                        if (lastYearMonth != null && !lastYearMonth.equals(currentYearMonth)) {
                            dayOfMonth = 0;
                        }
                        dayOfMonth++;
                        lastYearMonth = currentYearMonth;

                        if (missingDay.equals(date.getKey())) {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[%s][%s][fromFirstDayOfMonth][%s][dayOfMonth=%s][break]current day", method, SOSDate
                                        .getDateTimeAsString(curDate), missingDay, dayOfMonth));
                            }
                            missingDaysCurrentDayFound = true;
                            break;
                        }

                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][fromFirstDayOfMonth][%s][dayOfMonth=%s]lastYearMonth=%s", method, SOSDate
                                    .getDateTimeAsString(curDate), missingDay, dayOfMonth, lastYearMonth));
                        }
                    }
                } else {
                    dayOfMonth++;
                }
                if (days.contains(dayOfMonth)) {
                    if (missingDaysResolved && !missingDaysCurrentDayFound) {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][skip][curDate not found in the fromFirstDayOfMonth][dayOfMonth=%s][days=%s]dates=%s",
                                    method, SOSDate.getDateTimeAsString(curDate), dayOfMonth, debugDays, dates));
                        }
                    } else {
                        if (curDate.before(dateFromOrig)) {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[%s][%s][skip][dayOfMonth=%s][curDate before dateFromOrig=%s][days=%s]dates=%s", method,
                                        SOSDate.getDateTimeAsString(curDate), dayOfMonth, SOSDate.getDateTimeAsString(dateFromOrig), debugDays,
                                        dates));
                            }
                        } else {
                            dates.add(date.getKey());
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[%s][%s][added][dayOfMonth=%s][days=%s]dates=%s", method, SOSDate.getDateTimeAsString(
                                        curDate), dayOfMonth, debugDays, dates));
                            }
                        }
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][skip][dayOfMonth=%s][days=%s]dates=%s", method, SOSDate.getDateTimeAsString(curDate),
                                dayOfMonth, debugDays, dates));
                    }
                }
            }
            if (weeklyDays != null) {
                WeeklyDay weeklyDay = new WeeklyDay();
                weeklyDay.setDay(curDate.get(Calendar.DAY_OF_WEEK) - 1);
                weeklyDay.setWeekOfMonth(getWeekOfMonth(curDate, false));

                if (weeklyDays.contains(weeklyDay)) {
                    if (curDate.before(dateFromOrig)) {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format(
                                    "[%s][%s][skip][weeklyDay day=%s, weekOfMonth=%s][curDate before dateFromOrig=%s][weeklyDays=%s]dates=%s", method,
                                    SOSDate.getDateTimeAsString(curDate), weeklyDay.getDay(), weeklyDay.getWeekOfMonth(), SOSDate.getDateTimeAsString(
                                            dateFromOrig), debugWeeklyDays, dates));
                        }
                    } else {
                        dates.add(date.getKey());

                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][added][weeklyDay day=%s, weekOfMonth=%s][weeklyDays=%s]dates=%s", method, SOSDate
                                    .getDateTimeAsString(curDate), weeklyDay.getDay(), weeklyDay.getWeekOfMonth(), debugWeeklyDays, dates));
                        }
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][skip][weeklyDay day=%s, weekOfMonth=%s][weeklyDays=%s]dates=%s", method, SOSDate
                                .getDateTimeAsString(curDate), weeklyDay.getDay(), weeklyDay.getWeekOfMonth(), debugWeeklyDays, dates));
                    }
                }
            }
        }

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][end]dates=%s", method, dates));
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

    private boolean hasRepetitions(Frequencies includes) {
        return includes.getRepetitions() != null && includes.getRepetitions().size() > 0;
    }

    // used by restrictions
    // TODO from, to is not really used
    private List<String> getDatesFromIncludes(Calendar from, Calendar to, boolean isUltimos) throws SOSInvalidDataException {
        Map<String, Calendar> result = new HashMap<>();

        if (baseCalendarIncludes.getDates() != null && baseCalendarIncludes.getDates().size() > 0) {
            result.putAll(resolveDates(baseCalendarIncludes.getDates()));
        }

        if (baseCalendarIncludes.getHolidays() != null && baseCalendarIncludes.getHolidays().size() > 0) {
            for (Holidays holiday : baseCalendarIncludes.getHolidays()) {
                result.putAll(resolveDates(holiday.getDates()));
            }
        }

        if (baseCalendarIncludes.getWeekdays() != null && baseCalendarIncludes.getWeekdays().size() > 0) {
            for (WeekDays weekDay : baseCalendarIncludes.getWeekdays()) {
                result.putAll(resolveWeekDays(weekDay.getDays(), getFrom(weekDay.getFrom(), from), getTo(weekDay.getTo(), to)));
            }
        }

        if (baseCalendarIncludes.getMonthdays() != null && baseCalendarIncludes.getMonthdays().size() > 0) {
            for (MonthDays monthDay : baseCalendarIncludes.getMonthdays()) {
                result.putAll(resolveMonthDays(monthDay.getDays(), monthDay.getWeeklyDays(), getFrom(monthDay.getFrom(), from), getTo(monthDay
                        .getTo(), to)));
            }
        }

        if (baseCalendarIncludes.getUltimos() != null && baseCalendarIncludes.getUltimos().size() > 0) {
            for (MonthDays monthDay : baseCalendarIncludes.getUltimos()) {
                result.putAll(resolveUltimos(monthDay.getDays(), monthDay.getWeeklyDays(), getFrom(monthDay.getFrom(), from), getTo(monthDay.getTo(),
                        to)));
            }
        }

        if (hasRepetitions(baseCalendarIncludes)) {
            for (Repetition repetition : baseCalendarIncludes.getRepetitions()) {
                result.putAll(resolveRepetitions(repetition));
            }
        }

        // excludes/remove
        if (baseCalendarExcludes != null && result.size() > 0) {
            if (baseCalendarExcludes.getDates() != null && baseCalendarExcludes.getDates().size() > 0) {
                result = removeDays(result, resolveDates(baseCalendarExcludes.getDates()));
            }

            if (result.size() > 0 && baseCalendarExcludes.getHolidays() != null && baseCalendarExcludes.getHolidays().size() > 0) {
                for (Holidays holiday : baseCalendarExcludes.getHolidays()) {
                    result = removeDays(result, resolveDates(holiday.getDates()));
                }
            }

            if (result.size() > 0 && baseCalendarExcludes.getWeekdays() != null && baseCalendarExcludes.getWeekdays().size() > 0) {
                for (WeekDays weekDay : baseCalendarExcludes.getWeekdays()) {
                    result = removeDays(result, resolveWeekDays(weekDay.getDays(), getFrom(weekDay.getFrom(), from), getTo(weekDay.getTo(), to)));
                }
            }

            if (result.size() > 0 && baseCalendarExcludes.getMonthdays() != null && baseCalendarExcludes.getMonthdays().size() > 0) {
                for (MonthDays monthDay : baseCalendarExcludes.getMonthdays()) {
                    result = removeDays(result, resolveMonthDays(monthDay.getDays(), monthDay.getWeeklyDays(), getFrom(monthDay.getFrom(), from),
                            getTo(monthDay.getTo(), to)));
                }
            }

            if (result.size() > 0 && baseCalendarExcludes.getUltimos() != null && baseCalendarExcludes.getUltimos().size() > 0) {
                for (MonthDays monthDay : baseCalendarExcludes.getUltimos()) {
                    result = removeDays(result, resolveUltimos(monthDay.getDays(), monthDay.getWeeklyDays(), getFrom(monthDay.getFrom(), from), getTo(
                            monthDay.getTo(), to)));
                }
            }

            if (result.size() > 0 && hasRepetitions(baseCalendarExcludes)) {
                for (Repetition repetition : baseCalendarExcludes.getRepetitions()) {
                    result = removeDays(result, resolveRepetitions(repetition));
                }
            }
        }

        // remove > to
        List<String> toRemove = result.entrySet().stream().filter(e -> {
            return e.getValue().after(to);
        }).map(e -> {
            return e.getKey();
        }).collect(Collectors.toList());
        for (String key : toRemove) {
            result.remove(key);
        }

        if (isUltimos) {// sort descending
            return result.entrySet().stream().sorted((c1, c2) -> c2.getValue().compareTo(c1.getValue())).map(e -> {
                return e.getKey();
            }).collect(Collectors.toList());
        }
        // sort ascending
        return result.entrySet().stream().sorted((c1, c2) -> c1.getValue().compareTo(c2.getValue())).map(e -> {
            return e.getKey();
        }).collect(Collectors.toList());
    }

    private Map<String, Calendar> removeDays(Map<String, Calendar> result, Map<String, Calendar> toRemove) {
        for (String item : toRemove.keySet()) {
            if (result.containsKey(item)) {
                result.remove(item);
            }
        }
        return result;
    }

    private int getWeekOfMonth(Calendar c, boolean isUltimos) {
        int result = 1;
        int month = c.get(Calendar.MONTH);
        int weekStep = isUltimos ? 7 : -7;
        Calendar copy = (Calendar) c.clone();

        boolean run = true;
        while (run) {
            java.util.Calendar prev = (Calendar) copy.clone();
            prev.add(java.util.Calendar.DATE, weekStep);
            copy = prev;
            if (month == prev.get(Calendar.MONTH)) {
                result++;
            } else {
                run = false;
            }
            // max 5 - e.g. 2022-05-30 is a 5th Monday in month
            if (result > 5) {// avoid endless loop
                result = 5;
                run = false;
            }
        }
        return result;
    }

    private Map<String, Calendar> resolveUltimos(List<Integer> days, List<WeeklyDay> weeklyDays, Calendar from, Calendar to)
            throws SOSInvalidDataException {

        Map<String, Calendar> dates = new HashMap<String, Calendar>();
        WeeklyDay weeklyDay = new WeeklyDay();

        while (from.compareTo(to) <= 0) {
            if (days != null) {
                int dayOfUltimo = from.getActualMaximum(Calendar.DAY_OF_MONTH) + 1 - from.get(Calendar.DAY_OF_MONTH);
                if (days.contains(dayOfUltimo)) {
                    dates.put(DF.format(from.toInstant()), (Calendar) from.clone());
                }
            }
            if (weeklyDays != null) {
                weeklyDay.setDay(from.get(Calendar.DAY_OF_WEEK) - 1);
                weeklyDay.setWeekOfMonth(getWeekOfMonthOfUltimoWeeklyDay(from));
                if (weeklyDays.contains(weeklyDay)) {
                    dates.put(DF.format(from.toInstant()), (Calendar) from.clone());
                }
            }
            from.add(Calendar.DATE, 1);
        }
        return dates;
    }

    private Set<String> resolveUltimosRestrictions(List<Integer> days, List<WeeklyDay> weeklyDays, Calendar from, Calendar to, Calendar dateFromOrig)
            throws SOSInvalidDataException {

        String method = "resolveUltimosRestrictions";
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        String debugDays = null;
        String debugWeeklyDays = null;
        if (isDebugEnabled) {
            debugDays = days == null ? "" : String.join(",", days.stream().map(e -> {
                return e.toString();
            }).collect(Collectors.toList()));
            debugWeeklyDays = weeklyDays == null ? "" : String.join(",", weeklyDays.stream().map(e -> {
                return new StringBuilder("[day=").append(e.getDay()).append(",weekOfMonth=").append(e.getWeekOfMonth()).append("]").toString();
            }).collect(Collectors.toList()));
            LOGGER.debug(String.format("[%s][start][from=%s,to=%s,dateFromOrig=%s][days=%s][weeklyDays=%s]", method, SOSDate.getDateTimeAsString(
                    from), SOSDate.getDateTimeAsString(to), SOSDate.getDateTimeAsString(dateFromOrig), debugDays, debugWeeklyDays));
        }

        SortedMap<String, Calendar> reverseDates = new TreeMap<String, Calendar>(new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                return o2.compareTo(o1);
            }
        });
        reverseDates.putAll(this.dates);

        Set<String> dates = new HashSet<String>();
        int dayOfMonth = 0;
        int lastMonth = -1;
        boolean missingDaysResolved = false;
        boolean missingDaysCurrentDayFound = false;
        Calendar lastDayOfMonthCalendar = getLastDayOfMonthCalendar(to);
        int lastDayOfMonthCalendarMonth = lastDayOfMonthCalendar.get(Calendar.MONTH);
        boolean toBeforeLastDayOfMonthCalendar = to.before(lastDayOfMonthCalendar);
        for (Entry<String, Calendar> date : reverseDates.entrySet()) {
            if (date == null || date.getValue() == null) {
                continue;
            }
            if (date.getValue().before(from)) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][break]is before from=%s", method, SOSDate.getDateTimeAsString(date.getValue()), SOSDate
                            .getDateTimeAsString(from)));
                }
                break;
            }
            Calendar curDate = date.getValue();
            int curMonth = curDate.get(Calendar.MONTH);
            if (curMonth != lastMonth) {
                dayOfMonth = 0;
                lastMonth = curMonth;
            }
            if (curDate.after(to)) {
                dayOfMonth++;
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][skip][dayOfMonth=%s]is after to=%s", method, SOSDate.getDateTimeAsString(curDate),
                            dayOfMonth, SOSDate.getDateTimeAsString(to)));
                }
                continue;
            }
            if (days != null) {
                if (!missingDaysResolved && baseCalendarIncludes != null && ((toBeforeLastDayOfMonthCalendar
                        && lastDayOfMonthCalendarMonth == curMonth) || hasRepetitions(baseCalendarIncludes))) {
                    missingDaysResolved = true;
                    List<String> missingDays = getDatesFromIncludes(from, lastDayOfMonthCalendar, true);
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][fromLastDayOfMonth][start][from=%s, to=%s]%s", method, SOSDate.getDateTimeAsString(
                                curDate), SOSDate.getDateTimeAsString(from), SOSDate.getDateTimeAsString(lastDayOfMonthCalendar), String.join(",",
                                        missingDays)));
                    }

                    dayOfMonth = 0;
                    String lastYearMonth = null; // TODO check if not more needed because && lastDayOfMonthCalendarMonth == curMonth
                    String currentYearMonth;
                    for (String missingDay : missingDays) {
                        currentYearMonth = missingDay.substring(0, 7);// 2022-02
                        if (lastYearMonth != null && !lastYearMonth.equals(currentYearMonth)) {
                            dayOfMonth = 0;
                        }
                        dayOfMonth++;
                        lastYearMonth = currentYearMonth;

                        if (missingDay.equals(date.getKey())) {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[%s][%s][fromLastDayOfMonth][%s][dayOfMonth=%s][break]current day", method, SOSDate
                                        .getDateTimeAsString(curDate), missingDay, dayOfMonth));
                            }
                            missingDaysCurrentDayFound = true;
                            break;
                        }

                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][fromLastDayOfMonth][%s][dayOfMonth=%s]lastYearMonth=%s", method, SOSDate
                                    .getDateTimeAsString(curDate), missingDay, dayOfMonth, lastYearMonth));
                        }
                    }
                } else {
                    dayOfMonth++;
                }
                if (days.contains(dayOfMonth)) {
                    if (missingDaysResolved && !missingDaysCurrentDayFound) {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][skip][curDate not found in the fromFirstDayOfMonth][dayOfMonth=%s][days=%s]dates=%s",
                                    method, SOSDate.getDateTimeAsString(curDate), dayOfMonth, debugDays, dates));
                        }
                    } else {
                        if (curDate.before(dateFromOrig)) {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[%s][%s][skip][dayOfMonth=%s][curDate before dateFromOrig=%s][days=%s]dates=%s", method,
                                        SOSDate.getDateTimeAsString(curDate), dayOfMonth, SOSDate.getDateTimeAsString(dateFromOrig), debugDays,
                                        dates));
                            }
                        } else {
                            dates.add(date.getKey());

                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[%s][%s][added][dayOfMonth=%s][days=%s]dates=%s", method, SOSDate.getDateTimeAsString(
                                        curDate), dayOfMonth, debugDays, dates));
                            }
                        }
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][skip][dayOfMonth=%s][days=%s]dates=%s", method, SOSDate.getDateTimeAsString(curDate),
                                dayOfMonth, debugDays, dates));
                    }
                }
            }

            if (weeklyDays != null) {
                WeeklyDay weeklyDay = new WeeklyDay();
                weeklyDay.setDay(curDate.get(Calendar.DAY_OF_WEEK) - 1);
                weeklyDay.setWeekOfMonth(getWeekOfMonth(curDate, true));

                if (weeklyDays.contains(weeklyDay)) {
                    if (curDate.before(dateFromOrig)) {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format(
                                    "[%s][%s][skip][weeklyDay day=%s, weekOfMonth=%s][curDate before dateFromOrig=%s][weeklyDays=%s]dates=%s", method,
                                    SOSDate.getDateTimeAsString(curDate), weeklyDay.getDay(), weeklyDay.getWeekOfMonth(), SOSDate.getDateTimeAsString(
                                            dateFromOrig), debugWeeklyDays, dates));
                        }
                    } else {
                        dates.add(date.getKey());

                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][added][weeklyDay day=%s, weekOfMonth=%s][weeklyDays=%s]dates=%s", method, SOSDate
                                    .getDateTimeAsString(date.getValue()), weeklyDay.getDay(), weeklyDay.getWeekOfMonth(), debugWeeklyDays, dates));
                        }
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][skip][weeklyDay day=%s, weekOfMonth=%s][weeklyDays=%s]dates=%s", method, SOSDate
                                .getDateTimeAsString(date.getValue()), weeklyDay.getDay(), weeklyDay.getWeekOfMonth(), debugWeeklyDays, dates));
                    }
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

    private Map<String, Calendar> resolveRepetitions(Repetition repetition) throws SOSInvalidDataException {
        if (repetition.getRepetition() == null) {
            throw new SOSInvalidDataException("json field 'repetition' in 'repetitions' is undefined.");
        }

        Calendar from = getCalendar(repetition.getFrom());
        Calendar to = getTo(repetition.getTo());
        Integer step = repetition.getStep() == null ? 1 : repetition.getStep();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[resolveRepetitions][repetition=%s step=%s][from=%s][to=%s]", repetition.getRepetition(), step, SOSDate
                    .getDateTimeAsString(from), SOSDate.getDateTimeAsString(to)));
        }

        int fromDayOfMonth = from.get(Calendar.DAY_OF_MONTH);

        Map<String, Calendar> dates = new HashMap<String, Calendar>();
        while (from.compareTo(to) <= 0) {
            dates.put(DF.format(from.toInstant()), (Calendar) from.clone());

            switch (repetition.getRepetition()) {
            case DAILY:
                from.add(Calendar.DATE, step);
                break;
            case WEEKLY:
                from.add(Calendar.DATE, (step * 7));
                break;
            case MONTHLY:
                from.add(Calendar.MONTH, step);
                // e.g.: from: YYYY-03-31 step 1
                // next: YYYY-04-30
                if (fromDayOfMonth > from.get(Calendar.DAY_OF_MONTH) && from.getActualMaximum(Calendar.DAY_OF_MONTH) >= fromDayOfMonth) {
                    from.set(Calendar.DAY_OF_MONTH, fromDayOfMonth);
                }
                break;
            case YEARLY:
                from.add(Calendar.YEAR, step);
                // e.g.: from: YYYY-02-29 step 1
                // next: YYYY-02-28
                if (fromDayOfMonth > from.get(Calendar.DAY_OF_MONTH) && from.getActualMaximum(Calendar.DAY_OF_MONTH) >= fromDayOfMonth) {
                    from.set(Calendar.DAY_OF_MONTH, fromDayOfMonth);
                }
                break;
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[resolveRepetitions][repetition=%s step=%s]dates size=%s", repetition.getRepetition(), step, dates.size()));
        }
        return dates;
    }

    /** this.calendarFrom */
    private Set<String> resolveRepetitionsRestrictions(Repetition repetition, Calendar dateFromOrig) throws SOSInvalidDataException {
        if (repetition.getRepetition() == null) {
            throw new SOSInvalidDataException("json field 'repetition' in 'repetitions' is undefined.");
        }

        String method = "resolveRepetitionsRestrictions";
        boolean isDebugEnabled = LOGGER.isDebugEnabled();

        Calendar from = getCalendar(repetition.getFrom());
        int fromDayOfMonth = from.get(Calendar.DAY_OF_MONTH);
        int fromMonth = from.get(Calendar.MONTH);
        boolean fromDayIsMonthMaximumDay = from.getActualMaximum(Calendar.DAY_OF_MONTH) == fromDayOfMonth;

        Integer step = repetition.getStep() == null ? 1 : repetition.getStep();
        Calendar to = getTo(repetition.getTo());

        if (isDebugEnabled) {
            LOGGER.info(String.format("[%s][from=%s][to=%s][dateFromOrig=%s]fromDayIsMonthMaximumDay=%s", method, SOSDate.getDateTimeAsString(from),
                    SOSDate.getDateTimeAsString(to), SOSDate.getDateTimeAsString(dateFromOrig), fromDayIsMonthMaximumDay));
        }

        Set<String> weekly = new HashSet<>();
        Set<String> monthly = new HashSet<>();
        Set<String> yearly = new HashSet<>();

        Map<String, Integer> monthMaximums = new HashMap<>();
        long daysBetween = 0;
        boolean missingDaysResolved = false;

        Set<String> dates = new HashSet<String>();
        for (Entry<String, Calendar> date : this.dates.entrySet()) {
            if (date == null || date.getValue() == null) {
                continue;
            }
            if (date.getValue().after(to)) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][break]is after to=%s", method, SOSDate.getDateTimeAsString(date.getValue()), SOSDate
                            .getDateTimeAsString(to)));
                }
                break;
            }
            Calendar curDate = date.getValue();
            if (curDate.before(from)) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][skip]is before from=%s", method, SOSDate.getDateTimeAsString(curDate), SOSDate
                            .getDateTimeAsString(from)));
                }
                continue;
            }
            if (curDate.before(dateFromOrig)) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][skip]is before dateFromOrig=%s", method, SOSDate.getDateTimeAsString(curDate), SOSDate
                            .getDateTimeAsString(dateFromOrig)));
                }
                continue;
            }

            long periodBetween = 0;
            Integer curDateMonthMaximum = null;
            int curDateYear = -1;
            int curDateMonth = -1;
            int curDateDayOfMonth = -1;
            int curDateMonthMaximumDay = -1;
            switch (repetition.getRepetition()) {
            case DAILY:
                if (!missingDaysResolved) {
                    missingDaysResolved = true;
                    if (curDate.after(from)) {
                        List<String> missingDays = getDatesFromIncludes(from, curDate, false);
                        int s = missingDays.size();
                        int diff = 0;
                        if (s > 0) {
                            String last = missingDays.get(s - 1);
                            if (last.equals(date.getKey())) {// remove curDate from count
                                diff = 1;
                            }
                        }
                        daysBetween = s - diff;
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][missingDays][from=%s][to curDate=%s][daysBetween=%s]%s", method, SOSDate
                                    .getDateTimeAsString(curDate), SOSDate.getDateTimeAsString(from), SOSDate.getDateTimeAsString(curDate),
                                    daysBetween, stringJoin(missingDays)));
                        }
                    }
                }

                periodBetween = daysBetween;
                if (periodBetween % step == 0) {
                    dates.add(date.getKey());

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][added][DAILY step=%s]daysBetween=%s", method, SOSDate.getDateTimeAsString(curDate), step,
                                periodBetween));
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][skip][DAILY step=%s]daysBetween=%s", method, SOSDate.getDateTimeAsString(curDate), step,
                                periodBetween));
                    }
                }
                daysBetween++;
                break;
            case WEEKLY:
                curDateYear = curDate.get(Calendar.YEAR);
                curDateMonth = curDate.get(Calendar.MONTH);
                curDateDayOfMonth = curDate.get(Calendar.DAY_OF_MONTH);

                /*-----------------*/
                long weeksBetween = 0;
                String weeklyKey = null;

                if (!missingDaysResolved) {
                    missingDaysResolved = true;
                    if (curDate.after(from)) {
                        List<String> missingDays = getDatesFromIncludes(from, curDate, false);
                        for (String md : missingDays) {
                            Calendar mdc = getCalendar(md);
                            if (mdc.equals(curDate)) {
                                break;
                            }

                            // the same handling as below
                            // TODO function
                            periodBetween = daysBetween(from, mdc);
                            weeksBetween = (periodBetween / 7);
                            weeklyKey = String.valueOf(weeksBetween);

                            if (weekly.contains(weeklyKey)) {
                            } else {
                                if (periodBetween % (step * 7) == 0) {// exact the same day
                                    weekly.add(weeklyKey);
                                } else {
                                    if (weeksBetween % step == 0) {
                                        weekly.add(weeklyKey);
                                    }
                                }
                            }
                        }
                    }
                }
                /*-----------------*/

                periodBetween = daysBetween(from, curDate);
                weeksBetween = (periodBetween / 7);
                weeklyKey = String.valueOf(weeksBetween);
                if (weekly.contains(weeklyKey)) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][skip][WEEKLY step=%s][%s][already added]weeksBetween=%s(daysBetween=%s)", method, SOSDate
                                .getDateTimeAsString(curDate), step, weeklyKey, weeksBetween, periodBetween));
                    }
                } else {
                    if (periodBetween % (step * 7) == 0) {// exact the same day
                        dates.add(date.getKey());

                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][added][exactly][WEEKLY step=%s][%s]weeksBetween=%s(daysBetween=%s)", method, SOSDate
                                    .getDateTimeAsString(curDate), step, weeklyKey, weeksBetween, periodBetween));
                        }
                        weekly.add(weeklyKey);
                    } else {
                        if (weeksBetween % step == 0) {
                            dates.add(date.getKey());

                            if (isDebugEnabled) {
                                LOGGER.debug(String.format(
                                        "[%s][%s][added][WEEKLY step=%s][%s]weeksBetween=%s(daysBetween=%s)curDateDayOfMonth=%s,fromDayOfMonth=%s",
                                        method, SOSDate.getDateTimeAsString(curDate), step, weeklyKey, weeksBetween, periodBetween, curDateDayOfMonth,
                                        fromDayOfMonth));
                            }
                            weekly.add(weeklyKey);
                        } else {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[%s][%s][skip][WEEKLY step=%s][%s]weeksBetween=%s(daysBetween=%s)", method, SOSDate
                                        .getDateTimeAsString(curDate), step, weeklyKey, weeksBetween, periodBetween));
                            }
                        }
                    }
                }
                break;
            case MONTHLY:
                /*-----------------*/
                String monthlyKey = null;
                if (!missingDaysResolved) {
                    missingDaysResolved = true;
                    if (curDate.after(from)) {
                        List<String> missingDays = getDatesFromIncludes(from, curDate, false);
                        for (String md : missingDays) {
                            Calendar mdc = getCalendar(md);
                            if (mdc.equals(curDate)) {
                                break;
                            }

                            curDateYear = mdc.get(Calendar.YEAR);
                            curDateMonth = mdc.get(Calendar.MONTH);
                            curDateDayOfMonth = mdc.get(Calendar.DAY_OF_MONTH);

                            // the same handling as below
                            // TODO function
                            periodBetween = periodBetween(Calendar.MONTH, from, mdc);
                            monthlyKey = String.valueOf(periodBetween);

                            if (monthly.contains(monthlyKey)) {
                            } else {
                                if (fromDayIsMonthMaximumDay) {
                                    curDateMonthMaximum = monthMaximums.get(monthlyKey);
                                    if (curDateMonthMaximum == null) {
                                        curDateMonthMaximum = mdc.getActualMaximum(Calendar.DAY_OF_MONTH);
                                        monthMaximums.put(monthlyKey, curDateMonthMaximum);
                                    }
                                    curDateMonthMaximumDay = curDateMonthMaximum;
                                }

                                if (periodBetween % step == 0 && monthDayEquals(method, mdc, fromDayOfMonth, fromDayIsMonthMaximumDay,
                                        curDateDayOfMonth, curDateMonthMaximumDay)) {
                                    monthly.add(monthlyKey);
                                } else {
                                    if (periodBetween % step == 0) {
                                        if (curDateDayOfMonth > fromDayOfMonth) {
                                            monthly.add(monthlyKey);
                                        } else {
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                /*-----------------*/

                curDateYear = curDate.get(Calendar.YEAR);
                curDateMonth = curDate.get(Calendar.MONTH);
                curDateDayOfMonth = curDate.get(Calendar.DAY_OF_MONTH);

                periodBetween = periodBetween(Calendar.MONTH, from, curDate);
                monthlyKey = String.valueOf(periodBetween);
                if (monthly.contains(monthlyKey)) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][skip][MONTHLY step=%s][%s][already added]monthsBetween=%s", method, SOSDate
                                .getDateTimeAsString(curDate), step, monthlyKey, periodBetween));
                    }
                } else {
                    if (fromDayIsMonthMaximumDay) {
                        curDateMonthMaximum = monthMaximums.get(monthlyKey);
                        if (curDateMonthMaximum == null) {
                            curDateMonthMaximum = curDate.getActualMaximum(Calendar.DAY_OF_MONTH);
                            monthMaximums.put(monthlyKey, curDateMonthMaximum);
                        }
                        curDateMonthMaximumDay = curDateMonthMaximum;
                    }

                    if (periodBetween % step == 0 && monthDayEquals(method, curDate, fromDayOfMonth, fromDayIsMonthMaximumDay, curDateDayOfMonth,
                            curDateMonthMaximumDay)) {
                        dates.add(date.getKey());

                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][added][exactly][MONTHLY step=%s][%s]monthsBetween=%s", method, SOSDate
                                    .getDateTimeAsString(curDate), step, monthlyKey, periodBetween));
                        }
                        monthly.add(monthlyKey);
                    } else {
                        if (periodBetween % step == 0) {
                            if (curDateDayOfMonth > fromDayOfMonth) {
                                dates.add(date.getKey());

                                if (isDebugEnabled) {
                                    LOGGER.debug(String.format(
                                            "[%s][%s][added][MONTHLY step=%s][%s][monthsBetween=%s]curDateDayOfMonth=%s > fromDayOfMonth=%s", method,
                                            SOSDate.getDateTimeAsString(curDate), step, monthlyKey, periodBetween, curDateDayOfMonth,
                                            fromDayOfMonth));
                                }
                                monthly.add(monthlyKey);
                            } else {
                                if (isDebugEnabled) {
                                    LOGGER.debug(String.format(
                                            "[%s][%s][skip][MONTHLY step=%s][%s][monthsBetween=%s]curDateDayOfMonth=%s <= fromDayOfMonth=%s", method,
                                            SOSDate.getDateTimeAsString(curDate), step, monthlyKey, periodBetween, curDateDayOfMonth,
                                            fromDayOfMonth));
                                }
                            }
                        } else {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[%s][%s][skip][MONTHLY step=%s][%s]monthsBetween=%s", method, SOSDate.getDateTimeAsString(
                                        curDate), step, monthlyKey, periodBetween));
                            }
                        }
                    }
                }
                break;
            case YEARLY:
                /*-----------------*/
                String yearlyKey = null;
                if (!missingDaysResolved) {
                    missingDaysResolved = true;
                    if (curDate.after(from)) {
                        List<String> missingDays = getDatesFromIncludes(from, curDate, false);
                        for (String md : missingDays) {
                            Calendar mdc = getCalendar(md);
                            if (mdc.equals(curDate)) {
                                break;
                            }

                            curDateYear = mdc.get(Calendar.YEAR);
                            curDateMonth = mdc.get(Calendar.MONTH);
                            curDateDayOfMonth = mdc.get(Calendar.DAY_OF_MONTH);

                            // the same handling as below
                            // TODO function
                            periodBetween = periodBetween(Calendar.YEAR, from, mdc);
                            yearlyKey = curDateYear + "";

                            if (yearly.contains(yearlyKey)) {
                            } else {
                                if (fromDayIsMonthMaximumDay) {
                                    String monthKey = mdc.get(Calendar.YEAR) + "_" + curDateMonth;
                                    curDateMonthMaximum = monthMaximums.get(monthKey);
                                    if (curDateMonthMaximum == null) {
                                        curDateMonthMaximum = mdc.getActualMaximum(Calendar.DAY_OF_MONTH);
                                        monthMaximums.put(monthKey, curDateMonthMaximum);
                                    }
                                    curDateMonthMaximumDay = curDateMonthMaximum;
                                }

                                if (periodBetween % step == 0 && fromMonth == curDateMonth && monthDayEquals(method, mdc, fromDayOfMonth,
                                        fromDayIsMonthMaximumDay, curDateDayOfMonth, curDateMonthMaximumDay)) {
                                    yearly.add(yearlyKey);
                                } else {
                                    if (periodBetween % step == 0 && fromMonth == curDateMonth) {
                                        if (curDateDayOfMonth > fromDayOfMonth) {
                                            yearly.add(yearlyKey);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                curDateYear = curDate.get(Calendar.YEAR);
                curDateMonth = curDate.get(Calendar.MONTH);
                curDateDayOfMonth = curDate.get(Calendar.DAY_OF_MONTH);

                periodBetween = periodBetween(Calendar.YEAR, from, curDate);
                yearlyKey = curDateYear + "";
                if (yearly.contains(yearlyKey)) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][skip][YEARLY step=%s][%s][already added]yearsBetween=%s", method, SOSDate
                                .getDateTimeAsString(curDate), step, yearlyKey, periodBetween));
                    }
                } else {
                    if (fromDayIsMonthMaximumDay) {
                        String monthKey = curDate.get(Calendar.YEAR) + "_" + curDateMonth;
                        curDateMonthMaximum = monthMaximums.get(monthKey);
                        if (curDateMonthMaximum == null) {
                            curDateMonthMaximum = curDate.getActualMaximum(Calendar.DAY_OF_MONTH);
                            monthMaximums.put(monthKey, curDateMonthMaximum);
                        }
                        curDateMonthMaximumDay = curDateMonthMaximum;
                    }

                    if (periodBetween % step == 0 && fromMonth == curDateMonth && monthDayEquals(method, curDate, fromDayOfMonth,
                            fromDayIsMonthMaximumDay, curDateDayOfMonth, curDateMonthMaximumDay)) {
                        dates.add(date.getKey());

                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][added][exactly][YEARLY step=%s]yearsBetween=%s", method, SOSDate.getDateTimeAsString(
                                    curDate), step, periodBetween));
                        }
                        yearly.add(yearlyKey);
                    } else {
                        if (periodBetween % step == 0 && fromMonth == curDateMonth) {
                            if (curDateDayOfMonth > fromDayOfMonth) {
                                dates.add(date.getKey());

                                if (isDebugEnabled) {
                                    LOGGER.debug(String.format(
                                            "[%s][%s][added][YEARLY step=%s][%s][yearsBetween=%s][curDateMonth=fromMonth=%s]curDateDayOfMonth=%s > fromDayOfMonth=%s",
                                            method, SOSDate.getDateTimeAsString(curDate), step, yearlyKey, periodBetween, fromMonth,
                                            curDateDayOfMonth, fromDayOfMonth));
                                }
                                yearly.add(yearlyKey);
                            } else {
                                if (isDebugEnabled) {
                                    LOGGER.debug(String.format(
                                            "[%s][%s][skip][YEARLY step=%s][%s][yearsBetween=%s][curDateMonth=fromMonth=%s]curDateDayOfMonth=%s <= fromDayOfMonth=%s",
                                            method, SOSDate.getDateTimeAsString(curDate), step, yearlyKey, periodBetween, fromMonth,
                                            curDateDayOfMonth, fromDayOfMonth));
                                }
                            }
                        } else {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[%s][%s][skip][YEARLY step=%s][%s][yearsBetween=%s]fromMonth=%s,curDateMonth=%s", method,
                                        SOSDate.getDateTimeAsString(curDate), step, yearlyKey, periodBetween, fromMonth, curDateMonth));
                            }
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

    private long daysBetween(Calendar from, Calendar currentDay) {
        if (from == null || currentDay == null) {
            return -1;
        }
        try {
            return ChronoUnit.DAYS.between(toLocalDate(from), toLocalDate(currentDay));
        } catch (Throwable e) {
            LOGGER.error(String.format("[daysBetween][from=%s][currentDay=%s]%s", from, currentDay, e.toString()), e);
            return -1;
        }
    }

    private long periodBetween(int unit, Calendar from, Calendar currentDay) {
        if (from == null || currentDay == null) {
            return -1;
        }

        long diff = 0;
        Calendar f = (Calendar) from.clone();
        while (f.before(currentDay)) {
            int next = f.get(unit) + 1;
            f.set(unit, next);
            if (!f.after(currentDay)) {
                diff++;
            }
        }
        return diff;
    }

    private boolean monthDayEquals(String caller, Calendar curDate, int fromDayOfMonth, boolean fromDayIsMonthMaximumDay, int curDateDayOfMonth,
            int curDateMonthMaximumDay) {
        if (curDate == null) {
            return false;
        }

        String method = "monthDaysEquals";
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        if (fromDayIsMonthMaximumDay) {
            try {
                LOGGER.debug(String.format("[%s][%s][%s][curDateMonthMaximumDay=%s][curDateDayOfMonth=%s]", caller, method, SOSDate.getDateAsString(
                        curDate), curDateMonthMaximumDay, curDateDayOfMonth));
            } catch (SOSInvalidDataException e) {

            }
            if (curDateMonthMaximumDay == curDateDayOfMonth) {
                return true;
            }
        }

        if (isDebugEnabled) {
            try {
                LOGGER.debug(String.format("[%s][%s][%s][fromDayOfMonth=%s][curDateDayOfMonth=%s]", caller, method, SOSDate.getDateAsString(curDate),
                        fromDayOfMonth, curDateDayOfMonth));
            } catch (SOSInvalidDataException e) {

            }
        }

        if (fromDayOfMonth == curDateDayOfMonth) {
            return true;
        }
        return false;
    }

    private LocalDate toLocalDate(Calendar cal) {
        if (cal == null) {
            return null;
        }
        return LocalDate.of(cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal.get(Calendar.DAY_OF_MONTH));
    }

    private Calendar getTodayCalendar() {
        // use today at 12:00:00.000 as default
        Calendar cal = Calendar.getInstance(UTC);
        cal.setTime(Date.from(Instant.now()));
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    private Calendar getFirstDayOfMonthCalendar(Calendar curCalendar) {
        curCalendar.set(Calendar.DATE, 1);
        return (Calendar) curCalendar.clone();
    }

    private Calendar getLastDayOfMonthCalendar(Calendar curCalendar) {
        Calendar copy = (Calendar) curCalendar.clone();
        copy.set(Calendar.DATE, copy.getActualMaximum(Calendar.DATE));
        return copy;
    }

    private void addDatesRestrictions(Calendar dateFromOrig) throws SOSInvalidDataException {
        if (includes != null && includes.getDates() != null && !includes.getDates().isEmpty()) {
            for (Entry<String, Calendar> date : this.dates.entrySet()) {
                if (date == null || date.getValue() == null) {
                    continue;
                }
                Calendar curDate = date.getValue();
                if (curDate.after(dateTo)) {
                    break;
                }
                if (curDate.before(dateFrom)) {
                    continue;
                }
                if (curDate.before(dateFromOrig)) {
                    continue;
                }
                if (includes.getDates().contains(date.getKey())) {
                    restrictions.add(date.getKey());
                }
            }
            datesWithoutRestrictions.clear();
        }
    }

    private void addWeekDaysRestrictions(Calendar dateFromOrig) throws SOSInvalidDataException {
        if (includes != null) {
            addWeekDaysRestrictions(includes.getWeekdays(), dateFrom, dateTo, dateFromOrig);
        }
    }

    private void addWeekDaysRestrictions(List<WeekDays> weekDays, Calendar from, Calendar to, Calendar dateFromOrig) throws SOSInvalidDataException {
        if (weekDays != null) {
            for (WeekDays weekDay : weekDays) {
                addRestrictions(resolveWeekDaysRestrictions(weekDay.getDays(), getFrom(weekDay.getFrom(), from), getTo(weekDay.getTo(), to),
                        dateFromOrig));
            }
        }
    }

    private void addMonthDaysRestrictions(Calendar dateFromOrig) throws SOSInvalidDataException {
        if (includes != null) {
            addMonthDaysRestrictions(includes.getMonthdays(), dateFrom, dateTo, dateFromOrig);
        }
    }

    private void addMonthDaysRestrictions(List<MonthDays> monthDays, Calendar from, Calendar to, Calendar dateFromOrig)
            throws SOSInvalidDataException {
        if (monthDays != null) {
            for (MonthDays monthDay : monthDays) {
                addRestrictions(resolveMonthDaysRestrictions(monthDay.getDays(), monthDay.getWeeklyDays(), getFrom(monthDay.getFrom(), from), getTo(
                        monthDay.getTo(), to), dateFromOrig));
            }
        }
    }

    private void addUltimosRestrictions(Calendar dateFromOrig) throws SOSInvalidDataException {
        if (includes != null && includes.getUltimos() != null) {
            addUltimosRestrictions(includes.getUltimos(), dateFrom, dateTo, dateFromOrig);
        }
    }

    private void addUltimosRestrictions(List<MonthDays> ultimos, Calendar from, Calendar to, Calendar dateFromOrig) throws SOSInvalidDataException {
        if (ultimos != null) {
            for (MonthDays ultimo : ultimos) {
                addRestrictions(resolveUltimosRestrictions(ultimo.getDays(), ultimo.getWeeklyDays(), getFrom(ultimo.getFrom(), from), getTo(ultimo
                        .getTo(), to), dateFromOrig));
            }
        }
    }

    private void addRepetitionsRestrictions(Calendar dateFromOrig) throws SOSInvalidDataException {
        if (includes != null && includes.getRepetitions() != null) {
            for (Repetition repetition : includes.getRepetitions()) {
                addRestrictions(resolveRepetitionsRestrictions(repetition, dateFromOrig));
            }
        }
    }

    public SortedMap<String, Calendar> getDates() {
        return dates;
    }

    public SortedSet<String> getWithExcludes() {
        return withExcludes;
    }

}
