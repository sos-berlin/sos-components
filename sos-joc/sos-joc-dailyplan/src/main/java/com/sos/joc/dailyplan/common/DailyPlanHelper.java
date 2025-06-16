package com.sos.joc.dailyplan.common;

import java.nio.file.Paths;
import java.text.ParseException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.calendar.AssignedNonWorkingDayCalendars;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.schedule.OrderParameterisation;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.joc.Globals;
import com.sos.joc.classes.calendar.FrequencyResolver;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.dailyplan.common.DailyPlanDate;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class DailyPlanHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanHelper.class);

    public static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC);

    public static Date getDailyPlanDateAsDate(Long startTime) {
        java.util.Calendar calendar = java.util.Calendar.getInstance(TimeZone.getTimeZone(SOSDate.TIMEZONE_UTC));
        calendar.setTime(new Date(startTime));
        return calendar.getTime();
    }

    // TODO move to SOSPath (see duplicate in HistoryUtil)
    public static String getFolderFromPath(String path) {
        if (path == null || !path.startsWith("/")) {
            return "/";
        }
        int li = path.lastIndexOf("/");
        if (li == 0) {
            return path.substring(0, 1);
        }
        return li > -1 ? path.substring(0, li) : path;
    }

    // TODO move to SOSPath (see duplicate in HistoryUtil)
    public static String getBasenameFromPath(String path) {
        if (SOSString.isEmpty(path)) {
            return path;
        }
        int li = path.lastIndexOf("/");
        return li > -1 ? path.substring(li + 1) : path;
    }

    public static java.util.Calendar getUTCCalendarNow() {
        return getCalendar(null, SOSDate.TIMEZONE_UTC);
    }

    public static java.util.Calendar getNextDateUTCCalendar(Date date) {
        if (date == null) {
            return null;
        }
        java.util.Calendar cal = java.util.Calendar.getInstance(TimeZone.getTimeZone(SOSDate.TIMEZONE_UTC));
        cal.setTime(date);
        cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
        return cal;
    }

    public static String getDate(java.util.Calendar cal) {
        if (cal == null) {
            return null;
        }
        return DATE_FORMATTER.format(cal.toInstant());
    }

    public static String getFirstDateOfMonth(java.util.Calendar cal) {
        java.util.Calendar clone = clone(cal);
        clone.set(java.util.Calendar.DAY_OF_MONTH, 1);
        return DATE_FORMATTER.format(clone.toInstant());
    }

    public static String getLastDateOfMonth(java.util.Calendar cal) {
        java.util.Calendar clone = clone(cal);
        clone.set(java.util.Calendar.DATE, clone.getActualMaximum(java.util.Calendar.DATE));
        // copy.set(java.util.Calendar.YEAR, year);
        return DATE_FORMATTER.format(clone.toInstant());
    }

    public static java.util.Calendar clone(java.util.Calendar cal) {
        if (cal == null) {
            return null;
        }
        return (java.util.Calendar) cal.clone();
    }

    public static int getYear(java.util.Calendar cal) {
        if (cal == null) {
            return 0;
        }
        return cal.get(java.util.Calendar.YEAR);
    }

    public static java.util.Calendar getFirstDayOfYearCalendar(java.util.Calendar cal, int year) {
        if (cal == null) {
            return null;
        }
        cal.set(java.util.Calendar.YEAR, year);
        cal.set(java.util.Calendar.MONTH, 0);
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        return cal;
    }

    public static java.util.Calendar add2Clone(java.util.Calendar cal, int field, int amount) {
        java.util.Calendar clone = clone(cal);
        if (clone == null) {
            return null;
        }
        clone.add(field, amount);
        return clone;
    }

    public static java.util.Calendar getCalendar(String time, String timeZoneName) {
        if (time == null) {
            time = "00:00";
        }
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneName);
        java.util.Calendar calendar = java.util.Calendar.getInstance(timeZone);

        String[] timeArray = time.split(":");
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        if (timeArray.length == 1) {
            try {
                hours = Integer.parseInt(timeArray[0]);
                minutes = 0;
            } catch (NumberFormatException e) {
                LOGGER.warn("Wrong time format for: " + time);
                hours = 0;
                minutes = 0;
                time = "00:00";
            }
        }
        if (timeArray.length > 1) {
            try {
                hours = Integer.parseInt(timeArray[0]);
                minutes = Integer.parseInt(timeArray[1]);
                if (timeArray.length > 2) {
                    seconds = Integer.parseInt(timeArray[2]);
                }
            } catch (NumberFormatException e) {
                LOGGER.warn("Wrong time format for: " + time);
                hours = 0;
                minutes = 0;
                time = "00:00";
            }
        }

        calendar.set(java.util.Calendar.HOUR_OF_DAY, hours);
        calendar.set(java.util.Calendar.MINUTE, minutes);
        calendar.set(java.util.Calendar.SECOND, seconds);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        calendar.getTimeInMillis();
        return calendar;
    }

    // service
    public static java.util.Calendar getNextDayCalendar() {
        java.util.Calendar calendar = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.add(java.util.Calendar.DATE, 1);
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        return calendar;
    }

    public static OrderCounter getOrderCount(List<DBItemDailyPlanOrder> plannedOrders) {
        OrderCounter c = new OrderCounter();

        Map<MainCyclicOrderKey, List<DBItemDailyPlanOrder>> map = new HashMap<MainCyclicOrderKey, List<DBItemDailyPlanOrder>>();
        for (DBItemDailyPlanOrder item : plannedOrders) {
            if ((item.getStartMode() == 1)) {
                MainCyclicOrderKey key = new MainCyclicOrderKey(item);
                if (map.get(key) == null) {
                    map.put(key, new ArrayList<DBItemDailyPlanOrder>());
                    c.addCyclic();
                }
                map.get(key).add(item);
                c.addCyclicTotal();
            } else {
                c.addSingle();
            }
        }
        return c;
    }

    public static OrderCounter getOrderCount(Map<PlannedOrderKey, PlannedOrder> plannedOrders) {
        OrderCounter c = new OrderCounter();

        Map<MainCyclicOrderKey, List<PlannedOrder>> map = new HashMap<MainCyclicOrderKey, List<PlannedOrder>>();
        for (PlannedOrder plannedOrder : plannedOrders.values()) {
            if ((plannedOrder.getPeriod().getSingleStart() == null)) {
                MainCyclicOrderKey key = new MainCyclicOrderKey(plannedOrder);
                if (map.get(key) == null) {
                    map.put(key, new ArrayList<PlannedOrder>());
                    c.addCyclic();
                }
                map.get(key).add(plannedOrder);
                c.addCyclicTotal();
            } else {
                c.addSingle();
            }
        }
        return c;
    }

    public static java.util.Calendar getStartTimeCalendar(DailyPlanSettings settings) {
        return getStartTimeCalendar(settings.getDailyPlanStartTime(), settings.getPeriodBegin(), settings.getTimeZone());
    }

    private static java.util.Calendar getStartTimeCalendar(String startTime, String periodBegin, String timezone) {
        java.util.Calendar cal = null;
        if (!SOSString.isEmpty(startTime)) {
            cal = DailyPlanHelper.getCalendar(startTime, timezone);
        } else {
            cal = DailyPlanHelper.getCalendar(periodBegin, timezone);
            cal.add(java.util.Calendar.DATE, 1);
            cal.add(java.util.Calendar.MINUTE, -1 * DailyPlanSettings.DEFAULT_START_TIME_MINUTES_BEFORE_PRERIOD_BEGIN);
        }
        java.util.Calendar now = java.util.Calendar.getInstance(TimeZone.getTimeZone(timezone));
        if (cal.before(now)) {
            cal.add(java.util.Calendar.DATE, 1);
        }
        return cal;
    }

    public static String getNextStartMsg(DailyPlanSettings settings, java.util.Calendar startCalendar) {
        return getNextStartMsg(settings, startCalendar, null);
    }

    public static String getNextStartMsg(DailyPlanSettings settings, java.util.Calendar startCalendar, String identifier) {
        String identifierMsg = SOSString.isEmpty(identifier) ? "" : "[" + identifier + "]";
        String msg = String.format("%s[%s][planned][%s %s]", identifierMsg, settings.getStartMode(), DailyPlanHelper.getStartTimeWithTimeZone(
                settings, startCalendar), settings.getTimeZone());
        if (settings.getDaysAheadPlan() > 0) {
            return (String.format("%s[creating daily plan for %s days ahead, submitting for %s days ahead]%s", msg, settings.getDaysAheadPlan(),
                    settings.getDaysAheadSubmit(), settings.toString()));
        } else {
            return (String.format("%s[skip][because creating daily plan for %s days ahead]%s", msg, settings.getDaysAheadPlan(), settings
                    .toString()));
        }
    }

    public static String getCallerForLog(DailyPlanSettings settings) {
        if (SOSCollection.isEmpty(settings.getCaller())) {
            return "";
        }
        return settings.getCaller().stream().map(item -> "[" + item + "]").collect(Collectors.joining());
    }

    public static String getStartTimeWithTimeZone(DailyPlanSettings settings, java.util.Calendar startCalendar) {
        return SOSDate.tryGetDateTimeWithTimeZoneAsString(startCalendar.getTime(), settings.getTimeZone());
    }

    public static String buildOrderId(String dailyPlanDate, Schedule schedule, OrderParameterisation orderParameterisation, Long startTime,
            Integer startMode) throws SOSInvalidDataException {
        return buildOrderId(dailyPlanDate, getOrderName(schedule, orderParameterisation), startTime, startMode);
    }

    public static String getOrderName(Schedule schedule, OrderParameterisation orderParameterisation) {
        String orderName = "";
        if (SOSString.isEmpty(orderParameterisation.getOrderName())) {
            orderName = Paths.get(schedule.getPath()).getFileName().toString();
        } else {
            orderName = orderParameterisation.getOrderName();
        }
        if (orderName.length() > 30) {
            orderName = orderName.substring(0, 30);
        }
        return orderName;
    }

    private static String buildOrderId(String dailyPlanDate, String orderName, Long startTime, Integer startMode) throws SOSInvalidDataException {
        String orderId = "";
        if (startMode == 0) {
            orderId = "#" + dailyPlanDate + "#P" + "<id" + startTime + ">-" + orderName;
        } else {
            orderId = "#" + dailyPlanDate + "#C" + "<id" + startTime + ">-<nr00000>-<size>-" + orderName;
        }
        return orderId;
    }

    public static Date getNextDay(Date date, DailyPlanSettings settings) throws ParseException {
        return getDay(date, settings, 1);
    }

    public static Date getPreviousDay(Date date, DailyPlanSettings settings) throws ParseException {
        return getDay(date, settings, -1);
    }

    public static Date getDay(Date date, DailyPlanSettings settings, int amount) throws ParseException {
        // TODO why settings. Maybe ....
        // return Date.from(date.toInstant().plusSeconds(amount * TimeUnit.DAYS.toSeconds(1)));
        java.util.Calendar calendar = java.util.Calendar.getInstance(TimeZone.getTimeZone(settings.getTimeZone()));
        calendar.setTime(date);
        calendar.add(java.util.Calendar.DATE, amount);
        return calendar.getTime();
    }

    @SuppressWarnings("unused")
    private static String getDayAfterNonWorkingDaysTmp(final Set<String> nonWorkingDays, final String date) throws SOSInvalidDataException {
        java.util.Calendar cal = FrequencyResolver.getCalendarFromString(date);
        cal.add(java.util.Calendar.DATE, 1);
        String result = DATE_FORMATTER.format(cal.toInstant());
        while (nonWorkingDays.contains(result)) {
            cal.add(java.util.Calendar.DATE, 1);
            result = DATE_FORMATTER.format(cal.toInstant());
        }
        return result;
    }

    public static String getDayAfterNonWorkingDays(final TreeSet<String> nonWorkingDays) throws SOSInvalidDataException {
        if (nonWorkingDays.size() == 0) {
            return null;
        }
        java.util.Calendar cal = FrequencyResolver.getCalendarFromString(nonWorkingDays.descendingIterator().next());
        cal.add(java.util.Calendar.DATE, 1);
        return DATE_FORMATTER.format(cal.toInstant());
    }

    @SuppressWarnings("unused")
    private static String getDayBeforeNonWorkingDaysTmp(final Set<String> nonWorkingDays, final String date) throws SOSInvalidDataException {
        java.util.Calendar cal = FrequencyResolver.getCalendarFromString(date);
        cal.add(java.util.Calendar.DATE, -1);
        String result = DATE_FORMATTER.format(cal.toInstant());
        while (nonWorkingDays.contains(result)) {
            cal.add(java.util.Calendar.DATE, -1);
            result = DATE_FORMATTER.format(cal.toInstant());
        }
        return result;
    }

    public static String getDayBeforeNonWorkingDays(final Set<String> nonWorkingDays) throws SOSInvalidDataException {
        if (nonWorkingDays.size() == 0) {
            return null;
        }
        java.util.Calendar cal = FrequencyResolver.getCalendarFromString(nonWorkingDays.iterator().next());
        cal.add(java.util.Calendar.DATE, -1);
        return DATE_FORMATTER.format(cal.toInstant());
    }

    // nonWorkingDays=2023-01-10,2023-01-11,2023-01-15,2023-01-20,2023-01-21
    // returns 2023-01-20
    // TODO optimize
    public static String getFirstNonWorkingDayFromLastBlock(final TreeSet<String> nonWorkingDays) throws SOSInvalidDataException {
        java.util.Calendar cal = null;
        String result = null;
        int i = 0;

        Iterator<String> it = nonWorkingDays.descendingIterator();
        while (it.hasNext()) {
            String d = it.next();
            if (i == 0) {
                result = d;
                cal = FrequencyResolver.getCalendarFromString(result);
                i++;
            } else {
                if (i > 0) {
                    cal.add(java.util.Calendar.DATE, -i);
                    String nd = DATE_FORMATTER.format(cal.toInstant());
                    if (nd.equals(d)) {
                        result = d;
                    } else {
                        return result;
                    }
                }
            }
        }
        return result;
    }

    // nonWorkingDays=2023-01-10,2023-01-11,2023-01-15,2023-01-20
    // returns 2023-01-11
    // TODO optimize
    public static String getLastNonWorkingDayFromFirstBlock(final Set<String> nonWorkingDays) throws SOSInvalidDataException {
        java.util.Calendar cal = null;
        String result = null;
        int i = 0;

        Iterator<String> it = nonWorkingDays.iterator();
        while (it.hasNext()) {
            String d = it.next();
            if (i == 0) {
                result = d;
                cal = FrequencyResolver.getCalendarFromString(result);
                i++;
            } else {
                if (i > 0) {
                    cal.add(java.util.Calendar.DATE, i);
                    String nd = DATE_FORMATTER.format(cal.toInstant());
                    if (nd.equals(d)) {
                        result = d;
                    } else {
                        return result;
                    }
                }
            }
        }
        return result;
    }

    public static Set<String> getNonWorkingDays(String caller, InventoryDBLayer dbLayer, List<AssignedNonWorkingDayCalendars> current,
            String dateFrom, String dateTo) throws SOSHibernateException, JsonMappingException, JsonProcessingException, SOSMissingDataException,
            SOSInvalidDataException {
        return getNonWorkingDays(caller, dbLayer, current, dateFrom, dateTo, new HashMap<>());
    }

    // all - not null
    public static Set<String> getNonWorkingDays(String caller, InventoryDBLayer dbLayer, List<AssignedNonWorkingDayCalendars> current,
            String dateFrom, String dateTo, Map<String, Calendar> all) throws SOSHibernateException, JsonMappingException, JsonProcessingException,
            SOSMissingDataException, SOSInvalidDataException {
        Set<String> result = new TreeSet<>();
        if (current != null && !current.isEmpty()) {
            List<String> notInAllNames = current.stream().map(AssignedNonWorkingDayCalendars::getCalendarName).distinct().filter(e -> !all
                    .containsKey(e)).collect(Collectors.toList());

            if (notInAllNames != null && notInAllNames.size() > 0) {
                List<DBItemInventoryReleasedConfiguration> dbItems = null;
                boolean newSession = false;
                try {
                    newSession = dbLayer.getSession() == null;
                    if (newSession) {
                        dbLayer.setSession(Globals.createSosHibernateStatelessConnection(caller + "-getNonWorkingDays"));
                    }
                    dbItems = dbLayer.getReleasedConfigurations(notInAllNames, ConfigurationType.NONWORKINGDAYSCALENDAR);
                } finally {
                    if (newSession) {
                        dbLayer.close();
                    }
                }
                if (dbItems != null && !dbItems.isEmpty()) {
                    for (DBItemInventoryReleasedConfiguration dbItem : dbItems) {
                        Calendar c = Globals.objectMapper.readValue(dbItem.getContent(), Calendar.class);
                        c.setId(dbItem.getId());
                        c.setName(dbItem.getName());
                        c.setPath(dbItem.getPath());

                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug(String.format("[NonWorkingDaysCalendar=%s]%s", c.getName(), SOSString.toString(c, true)));
                        }

                        all.put(c.getName(), c);
                    }
                }
            }

            FrequencyResolver fr = new FrequencyResolver();
            for (AssignedNonWorkingDayCalendars ac : current) {
                Calendar c = all.get(ac.getCalendarName());
                if (c == null) {
                    continue;
                }
                result.addAll(fr.resolveCalendar(c, dateFrom, dateTo).getDates());
            }
        }
        return result;
    }

    public static String toZonedUTCDateTime(Long dateTime) {
        return toZonedUTCDateTime(SOSDate.tryGetDateTimeAsString(dateTime));
    }

    public static String toZonedUTCDateTime(String dateTime) {
        return dateTime.replace(' ', 'T') + "Z";
    }

    public static String toZonedUTCDateTimeCyclicPeriod(String date, Date datePart) throws SOSInvalidDataException {
        String d = SOSDate.getDateTimeAsString(datePart);
        return toZonedUTCDateTime(d.replace(DailyPlanDate.PERIOD_DEFAULT_DATE, date));
    }

    public static String toString(Period p) {
        if (p == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("period ");
        if (p.getSingleStart() == null) {
            sb.append("begin=" + p.getBegin());
            sb.append(",").append("end=" + p.getEnd());
            sb.append(",").append("repeat=" + p.getRepeat());
        } else {
            sb.append("singleStart=" + p.getSingleStart());
        }
        return sb.toString();
    }

}
