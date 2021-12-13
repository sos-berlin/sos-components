package com.sos.joc.dailyplan.common;

import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.schedule.VariableSet;
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

    private static String getDailyPlanDateAsString(Long startTime, String timeZone) throws SOSInvalidDataException {
        java.util.Calendar calendar = java.util.Calendar.getInstance(TimeZone.getTimeZone(UTC));
        calendar.setTime(new Date(startTime));
        return SOSDate.getDateWithTimeZoneAsString(calendar.getTime(), timeZone);
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
        DateFormat format = new SimpleDateFormat("hh:mm:ss");

        Map<CycleOrderKey, List<DBItemDailyPlanOrder>> map = new TreeMap<CycleOrderKey, List<DBItemDailyPlanOrder>>();
        for (DBItemDailyPlanOrder item : plannedOrders) {
            if ((item.getStartMode() == 1)) {
                CycleOrderKey key = new CycleOrderKey();
                key.setPeriodBegin(format.format(item.getPeriodBegin()));
                key.setPeriodEnd(format.format(item.getPeriodEnd()));
                key.setRepeat(String.valueOf(item.getRepeatInterval()));
                key.setOrderName(item.getOrderName());
                key.setWorkflowPath(item.getWorkflowPath());
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

        Map<CycleOrderKey, List<PlannedOrder>> map = new TreeMap<CycleOrderKey, List<PlannedOrder>>();
        for (PlannedOrder plannedOrder : plannedOrders.values()) {
            if ((plannedOrder.getPeriod().getSingleStart() == null)) {
                CycleOrderKey key = new CycleOrderKey();
                key.setPeriodBegin(plannedOrder.getPeriod().getBegin());
                key.setPeriodEnd(plannedOrder.getPeriod().getEnd());
                key.setRepeat(String.valueOf(plannedOrder.getPeriod().getRepeat()));
                key.setOrderName(plannedOrder.getOrderName());
                key.setWorkflowPath(plannedOrder.getSchedule().getWorkflowPath());
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

    private static String buildOrderId(String orderName, Long startTime, Integer startMode, String timeZone) throws SOSInvalidDataException {
        String orderId = "";
        String dailyPlanDate = getDailyPlanDateAsString(startTime, timeZone);
        if (startMode == 0) {
            orderId = "#" + dailyPlanDate + "#P" + "<id" + startTime + ">-" + orderName;
        } else {
            orderId = "#" + dailyPlanDate + "#C" + "<id" + startTime + ">-<nr00000>-<size>-" + orderName;
        }
        return orderId;
    }

    public static String buildOrderId(Schedule schedule, VariableSet variableSet, Long startTime, Integer startMode, String timeZone)
            throws SOSInvalidDataException {
        String orderName = "";
        if ((variableSet.getOrderName() == null) || (variableSet.getOrderName().isEmpty())) {
            orderName = Paths.get(schedule.getPath()).getFileName().toString();
        } else {
            orderName = variableSet.getOrderName();
        }
        if (orderName.length() > 30) {
            orderName = orderName.substring(0, 30);
        }

        return buildOrderId(orderName, startTime, startMode, timeZone);
    }

    public static Date getNextDay(Date date, DailyPlanSettings settings) throws ParseException {
        java.util.Calendar calendar = java.util.Calendar.getInstance(TimeZone.getTimeZone(settings.getTimeZone()));
        calendar.setTime(date);
        calendar.add(java.util.Calendar.DATE, 1);
        return calendar.getTime();
    }

    public static String modifiedOrderId(String oldOrderId, Long add) {
        String[] stringSplit = oldOrderId.split("-");
        String newOrderId = "";
        if (stringSplit.length >= 4) {
            for (int i = 0; i < 3; i++) {
                newOrderId = newOrderId + stringSplit[i] + "-";
            }

            if (stringSplit.length <= 5) {
                newOrderId = newOrderId + add + "-" + stringSplit[stringSplit.length - 1];
            } else {
                newOrderId = newOrderId + stringSplit[3] + "-" + stringSplit[4] + "-" + add + "-" + stringSplit[stringSplit.length - 1];
            }
        } else {
            newOrderId = oldOrderId;
        }
        return newOrderId;
    }
}
