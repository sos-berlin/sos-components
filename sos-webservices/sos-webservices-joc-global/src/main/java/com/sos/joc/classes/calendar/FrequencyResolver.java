package com.sos.joc.classes.calendar;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.SOSDate;
import com.sos.inventory.model.calendar.Frequencies;
import com.sos.inventory.model.calendar.Holidays;
import com.sos.inventory.model.calendar.MonthDays;
import com.sos.inventory.model.calendar.Months;
import com.sos.inventory.model.calendar.Repetition;
import com.sos.inventory.model.calendar.RepetitionText;
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
            addDates();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][after][addDates]dates=%s", method, dates.keySet()));
            }

            addHolidays();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][after][addHolidays]dates=%s", method, dates.keySet()));
            }

            addWeekDays();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][after][addWeekDays]dates=%s", method, dates.keySet()));
            }

            addMonthDays();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][after][addMonthDays]dates=%s", method, dates.keySet()));
            }

            addUltimos();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][after][addUltimos]dates=%s", method, dates.keySet()));
            }

            addMonths();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][after][addMonths]dates=%s", method, dates.keySet()));
            }

            addRepetitions();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][after][addRepetitions]dates=%s", method, dates.keySet()));
            }

            removeDates();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][after][removeDates]dates=%s", method, dates.keySet()));
            }

            removeWeekDays();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][after][removeWeekDays]dates=%s", method, dates.keySet()));
            }

            removeMonthDays();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][after][removeMonthDays]dates=%s", method, dates.keySet()));
            }

            removeUltimos();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][after][removeUltimos]dates=%s", method, dates.keySet()));
            }

            removeMonths();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][after][removeMonths]dates=%s", method, dates.keySet()));
            }

            removeHolidays();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][after][removeHolidays]dates=%s", method, dates.keySet()));
            }

            removeRepetitions();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][after][removeRepetitions]dates=%s", method, dates.keySet()));
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
                    addDatesRestrictions();
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][restrictions][after][addDatesRestrictions]datesWithoutRestrictions=%s", method,
                                datesWithoutRestrictions.keySet()));
                    }

                    addWeekDaysRestrictions();
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][restrictions][after][addWeekDaysRestrictions]datesWithoutRestrictions=%s", method,
                                datesWithoutRestrictions.keySet()));
                    }

                    addMonthDaysRestrictions(dateFromOrig);
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][restrictions][after][addMonthDaysRestrictions]datesWithoutRestrictions=%s", method,
                                datesWithoutRestrictions.keySet()));
                    }

                    addUltimosRestrictions();
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][restrictions][after][addUltimosRestrictions]datesWithoutRestrictions=%s", method,
                                datesWithoutRestrictions.keySet()));
                    }

                    addMonthsRestrictions(dateFromOrig);
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][restrictions][after][addMonthsRestrictions]datesWithoutRestrictions=%s", method,
                                datesWithoutRestrictions.keySet()));
                    }

                    addRepetitionsRestrictions();
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

    private void addDates() throws SOSInvalidDataException {
        if (includes != null) {
            addDates(includes.getDates());
        }
    }

    private void removeDates() throws SOSInvalidDataException {
        if (excludes != null && dates.size() > 0) {
            removeDates(excludes.getDates());
        }
    }

    private void addHolidays() throws SOSInvalidDataException {
        if (includes != null) {
            addHolidays(includes.getHolidays());
        }
    }

    private void removeHolidays() throws SOSInvalidDataException {
        if (excludes != null && dates.size() > 0) {
            removeHolidays(excludes.getHolidays());
        }
    }

    private void addWeekDays() throws SOSInvalidDataException {
        if (includes != null) {
            addWeekDays(includes.getWeekdays());
        }
    }

    private void removeWeekDays() throws SOSInvalidDataException {
        if (excludes != null && dates.size() > 0) {
            removeWeekDays(excludes.getWeekdays());
        }
    }

    private void addMonthDays() throws SOSInvalidDataException {
        if (includes != null) {
            addMonthDays(includes.getMonthdays());
        }
    }

    private void removeMonthDays() throws SOSInvalidDataException {
        if (excludes != null && dates.size() > 0) {
            removeMonthDays(excludes.getMonthdays());
        }
    }

    private void addUltimos() throws SOSInvalidDataException {
        if (includes != null) {
            addUltimos(includes.getUltimos());
        }
    }

    private void removeUltimos() throws SOSInvalidDataException {
        if (excludes != null && dates.size() > 0) {
            removeUltimos(excludes.getUltimos());
        }
    }

    private void addRepetitions() throws SOSInvalidDataException {
        if (includes != null) {
            addRepetitions(includes.getRepetitions());
        }
    }

    private void removeRepetitions() throws SOSInvalidDataException {
        if (excludes != null && dates.size() > 0) {
            removeRepetitions(excludes.getRepetitions());
        }
    }

    private void addMonths() throws SOSInvalidDataException {
        if (includes != null) {
            addMonths(includes.getMonths());
        }
    }

    private void removeMonths() throws SOSInvalidDataException {
        if (excludes != null && dates.size() > 0) {
            removeMonths(excludes.getMonths());
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
            Calendar monthStart = Calendar.getInstance(UTC);
            Calendar monthEnd = Calendar.getInstance(UTC);
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
            Calendar monthStart = Calendar.getInstance(UTC);
            Calendar monthEnd = Calendar.getInstance(UTC);
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
        Calendar cal = Calendar.getInstance(UTC);
        cal.setTime(Date.from(Instant.parse(to + "T12:00:00Z")));
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
                dates.put(DF.format(from.toInstant()), (Calendar) from.clone());
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

    private Set<String> resolveBasedOnMonthDays(List<Integer> days, List<WeeklyDay> weeklyDays, Calendar from, Calendar to, Calendar dateFromOrig)
            throws SOSInvalidDataException {

        String method = "resolveBasedOnMonthDays";
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        String debugDays = null;
        String debugWeeklyDays = null;
        if (isDebugEnabled) {
            debugDays = days == null ? "" : String.join(",", days.stream().map(e -> {
                return e.toString();
            }).collect(Collectors.toList()));
            debugWeeklyDays = weeklyDays == null ? "" : String.join(",", weeklyDays.stream().map(e -> {
                return e.toString();
            }).collect(Collectors.toList()));
            LOGGER.debug(String.format("[%s][start][from=%s, to=%s, dateFromOrig=%s][days=%s][weeklyDays=%s]", method, SOSDate.getDateTimeAsString(
                    from), SOSDate.getDateTimeAsString(to), SOSDate.getDateTimeAsString(dateFromOrig), debugDays, debugWeeklyDays));
        }

        Set<String> dates = new HashSet<String>();
        int dayOfMonth = 0;
        int lastMonth = -1;
        boolean missingDaysResolved = false;
        for (Entry<String, Calendar> date : this.dates.entrySet()) {
            if (date == null || date.getValue() == null) {
                continue;
            }
            if (date.getValue().after(to)) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][break][%s]is after to=%s", method, SOSDate.getDateTimeAsString(date.getValue()), SOSDate
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
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][skip][%s]curDate=%s is before from=%s", method, SOSDate.getDateTimeAsString(date.getValue()),
                            SOSDate.getDateTimeAsString(curDate), SOSDate.getDateTimeAsString(from)));
                }
                continue;
            }
            if (days != null) {
                // from was set to a first day of month
                // dateFromOrig - is the original from
                if (!missingDaysResolved && dateFromOrig.after(from)) {// 2021-02-03 after 2021-02-01
                    missingDaysResolved = true;
                    if (baseCalendarIncludes != null) {
                        List<String> missingDays = getDatesFromIncludes(from, dateFromOrig, false);
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][MonthDays][calculated from the first day of month]%s", method, SOSDate
                                    .getDateTimeAsString(date.getValue()), String.join(",", missingDays)));
                        }

                        dayOfMonth = 0;
                        String lastYearMonth = null;
                        String currentYearMonth;
                        for (String missingDay : missingDays) {
                            currentYearMonth = missingDay.substring(0, 7);// 2022-02
                            if (lastYearMonth != null && !lastYearMonth.equals(currentYearMonth)) {
                                dayOfMonth = 0;
                            }
                            dayOfMonth++;
                            lastYearMonth = currentYearMonth;

                            if (isDebugEnabled) {
                                LOGGER.debug(String.format(
                                        "[%s][%s][MonthDays][calculated from the first day of month]day=%s, dayOfMonth=%s, lastYearMonth=%s", method,
                                        SOSDate.getDateTimeAsString(date.getValue()), missingDay, dayOfMonth, lastYearMonth));
                            }

                            if (missingDay.equals(date.getKey())) {
                                break;
                            }
                        }
                    } else {
                        dayOfMonth++;// TODO
                    }
                } else {
                    dayOfMonth++;
                }
                if (days.contains(dayOfMonth)) {
                    dates.add(date.getKey());

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][MonthDays][added][dayOfMonth=%s][days=%s]dates=%s", method, SOSDate.getDateTimeAsString(
                                date.getValue()), dayOfMonth, debugDays, dates));
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][MonthDays][skip][dayOfMonth=%s][days=%s]dates=%s", method, SOSDate.getDateTimeAsString(
                                date.getValue()), dayOfMonth, debugDays, dates));
                    }
                }
            }
            if (weeklyDays != null) {
                WeeklyDay weeklyDay = new WeeklyDay();
                weeklyDay.setDay(curDate.get(Calendar.DAY_OF_WEEK) - 1);
                weeklyDay.setWeekOfMonth(getWeekOfMonth(curDate, false));

                if (weeklyDays.contains(weeklyDay)) {
                    dates.add(date.getKey());

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][WeeklyDays][added][weeklyDay day=%s, weekOfMonth=%s][weeklyDays=%s]dates=%s", method,
                                SOSDate.getDateTimeAsString(date.getValue()), weeklyDay.getDay(), weeklyDay.getWeekOfMonth(), debugWeeklyDays,
                                dates));
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][WeeklyDays][skip][weeklyDay day=%s, weekOfMonth=%s][weeklyDays=%s]dates=%s", method,
                                SOSDate.getDateTimeAsString(date.getValue()), weeklyDay.getDay(), weeklyDay.getWeekOfMonth(), debugWeeklyDays,
                                dates));
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

    private List<String> getDatesFromIncludes(Calendar from, Calendar to, boolean isUltimos) throws SOSInvalidDataException {
        Map<String, Calendar> result = new HashMap<>();
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

        if (baseCalendarIncludes.getRepetitions() != null && baseCalendarIncludes.getRepetitions().size() > 0) {
            for (Repetition repetition : baseCalendarIncludes.getRepetitions()) {
                result.putAll(resolveRepetitions(repetition.getRepetition(), repetition.getStep(), getFrom(repetition.getFrom(), calendarFrom),
                        getFrom(repetition.getFrom(), from), getTo(repetition.getTo(), to)));
            }
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

    private Set<String> resolveBasedOnUltimos(List<Integer> days, List<WeeklyDay> weeklyDays, Calendar from, Calendar to)
            throws SOSInvalidDataException {

        String method = "resolveBasedOnUltimos";
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        String debugDays = null;
        String debugWeeklyDays = null;
        if (isDebugEnabled) {
            debugDays = days == null ? "" : String.join(",", days.stream().map(e -> {
                return e.toString();
            }).collect(Collectors.toList()));
            debugWeeklyDays = weeklyDays == null ? "" : String.join(",", weeklyDays.stream().map(e -> {
                return e.toString();
            }).collect(Collectors.toList()));
            LOGGER.debug(String.format("[%s][start][from=%s, to=%s][days=%s][weeklyDays=%s]", method, SOSDate.getDateTimeAsString(from), SOSDate
                    .getDateTimeAsString(to), debugDays, debugWeeklyDays));
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
        Calendar lastDayOfMonthCalendar = getLastDayOfMonthCalendar(to);
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
                lastMonth = curMonth;
            }
            if (curDate.after(to)) {
                continue;
            }
            if (days != null) {
                if (!missingDaysResolved && to.before(lastDayOfMonthCalendar)) {
                    missingDaysResolved = true;
                    if (baseCalendarIncludes != null) {
                        List<String> missingDays = getDatesFromIncludes(from, lastDayOfMonthCalendar, true);
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][MonthDays][calculated from the last day of month]%s", method, SOSDate
                                    .getDateTimeAsString(date.getValue()), String.join(",", missingDays)));
                        }

                        dayOfMonth = 0;
                        String lastYearMonth = null;
                        String currentYearMonth;
                        for (String missingDay : missingDays) {
                            currentYearMonth = missingDay.substring(0, 7);// 2022-02
                            if (lastYearMonth != null && !lastYearMonth.equals(currentYearMonth)) {
                                dayOfMonth = 0;
                            }
                            dayOfMonth++;
                            lastYearMonth = currentYearMonth;

                            if (isDebugEnabled) {
                                LOGGER.debug(String.format(
                                        "[%s][%s][MonthDays][calculated from the last day of month]day=%s, dayOfMonth=%s, lastYearMonth=%s", method,
                                        SOSDate.getDateTimeAsString(date.getValue()), missingDay, dayOfMonth, lastYearMonth));
                            }

                            if (missingDay.equals(date.getKey())) {
                                break;
                            }
                        }
                    } else {
                        dayOfMonth++;// TODO
                    }
                } else {
                    dayOfMonth++;
                }
                if (days.contains(dayOfMonth)) {
                    dates.add(date.getKey());

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][MonthDays][added][dayOfMonth=%s][days=%s]dates=%s", method, SOSDate.getDateTimeAsString(
                                date.getValue()), dayOfMonth, debugDays, dates));
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][MonthDays][skip][dayOfMonth=%s][days=%s]dates=%s", method, SOSDate.getDateTimeAsString(
                                date.getValue()), dayOfMonth, debugDays, dates));
                    }
                }
            }

            if (weeklyDays != null) {
                WeeklyDay weeklyDay = new WeeklyDay();
                weeklyDay.setDay(curDate.get(Calendar.DAY_OF_WEEK) - 1);
                weeklyDay.setWeekOfMonth(getWeekOfMonth(curDate, true));

                if (weeklyDays.contains(weeklyDay)) {
                    dates.add(date.getKey());

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][WeeklyDays][added][weeklyDay day=%s, weekOfMonth=%s][weeklyDays=%s]dates=%s", method,
                                SOSDate.getDateTimeAsString(date.getValue()), weeklyDay.getDay(), weeklyDay.getWeekOfMonth(), debugWeeklyDays,
                                dates));
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][WeeklyDays][skip][weeklyDay day=%s, weekOfMonth=%s][weeklyDays=%s]dates=%s", method,
                                SOSDate.getDateTimeAsString(date.getValue()), weeklyDay.getDay(), weeklyDay.getWeekOfMonth(), debugWeeklyDays,
                                dates));
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
                dates.put(DF.format(calFrom.toInstant()), (Calendar) calFrom.clone());
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

    private void addMonthDaysRestrictions(Calendar dateFromOrig) throws SOSInvalidDataException {
        if (includes != null) {
            addMonthDaysRestrictions(includes.getMonthdays(), dateFrom, dateTo, dateFromOrig);
        }
    }

    private void addMonthDaysRestrictions(List<MonthDays> monthDays, Calendar from, Calendar to, Calendar dateFromOrig)
            throws SOSInvalidDataException {
        if (monthDays != null) {
            for (MonthDays monthDay : monthDays) {
                addRestrictions(resolveBasedOnMonthDays(monthDay.getDays(), monthDay.getWeeklyDays(), getFrom(monthDay.getFrom(), from), getTo(
                        monthDay.getTo(), to), dateFromOrig));
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

    private void addMonthsRestrictions(Calendar dateFromOrig) throws SOSInvalidDataException {
        if (includes != null && includes.getMonths() != null) {
            Calendar monthStart = Calendar.getInstance(UTC);
            Calendar monthEnd = Calendar.getInstance(UTC);
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
                            addMonthDaysRestrictions(month.getMonthdays(), monthFrom, monthTo, dateFromOrig);
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
                addRestrictions(resolveBasedOnRepetitions(repetition.getRepetition(), repetition.getStep(), getFrom(repetition.getFrom(),
                        calendarFrom), getFrom(repetition.getFrom()), getTo(repetition.getTo())));
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
