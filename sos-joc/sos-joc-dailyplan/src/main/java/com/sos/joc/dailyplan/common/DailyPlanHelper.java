package com.sos.joc.dailyplan.common;

import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.schedule.OrderParameterisation;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;

public class DailyPlanHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanHelper.class);
    private static final String UTC = "UTC";

    public static Date getDailyPlanDateAsDate(Long startTime) {
        java.util.Calendar calendar = java.util.Calendar.getInstance(TimeZone.getTimeZone(UTC));
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

    public static java.util.Calendar getCalendar(String time, String timeZoneName) {

        if (time == null) {
            time = "00:00";
        }

        // SOSClassUtil.printStackTrace(true, LOGGER);
        // LOGGER.debug("Timezone is " + timeZoneName);
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

    public static String getStartTimeAsString(String timeZoneName, String dailyPlanStartTime, String periodBegin) {
        java.util.Calendar startCalendar;
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneName);
        java.util.Calendar now = java.util.Calendar.getInstance(timeZone);

        if (!"".equals(dailyPlanStartTime)) {
            startCalendar = DailyPlanHelper.getCalendar(dailyPlanStartTime, timeZoneName);
        } else {
            startCalendar = DailyPlanHelper.getCalendar(periodBegin, timeZoneName);
            startCalendar.add(java.util.Calendar.DATE, 1);
            startCalendar.add(java.util.Calendar.MINUTE, -30);
        }

        if (startCalendar.before(now)) {
            startCalendar.add(java.util.Calendar.DATE, 1);
        }

        SimpleDateFormat format = new SimpleDateFormat(SOSDate.DATETIME_FORMAT);
        format.setTimeZone(timeZone);
        return format.format(startCalendar.getTime());
    }

    public static String buildOrderId(String dailyPlanDate, Schedule schedule, OrderParameterisation orderParameterisation, Long startTime, Integer startMode)
            throws SOSInvalidDataException {
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
        java.util.Calendar calendar = java.util.Calendar.getInstance(TimeZone.getTimeZone(settings.getTimeZone()));
        calendar.setTime(date);
        calendar.add(java.util.Calendar.DATE, 1);
        return calendar.getTime();
    }
}
