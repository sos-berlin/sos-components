package com.sos.js7.order.initiator.classes;

import java.nio.file.Path;
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

import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.schedule.VariableSet;
import com.sos.joc.db.orders.DBItemDailyPlanOrders;
import com.sos.js7.order.initiator.OrderInitiatorSettings;

public class DailyPlanHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanHelper.class);
    private static final String UTC = "UTC";

    public static Date stringAsDate(String date) throws ParseException {
        TimeZone savT = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat sdfUtc = new SimpleDateFormat("yyyy-MM-dd");
        Date d = sdfUtc.parse(date);
        TimeZone.setDefault(savT);
        return d;
    }

    public static String dateAsString(Date date, String timeZone) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setTimeZone(TimeZone.getTimeZone(timeZone));
        String dateS = formatter.format(date);
        return dateS;
    }

    public static String getDailyPlanDateAsString(Long startTime, String timeZone, String periodBegin) {
        java.util.Calendar calendar = java.util.Calendar.getInstance(TimeZone.getTimeZone(UTC));
        calendar.setTime(new Date(startTime));
        return DailyPlanHelper.dateAsString(calendar.getTime(), timeZone);
    }

    public static Date getDailyPlanDateAsDate(Long startTime) {
        java.util.Calendar calendar = java.util.Calendar.getInstance(TimeZone.getTimeZone(UTC));
        calendar.setTime(new Date(startTime));
        return calendar.getTime();
    }

    public static java.util.Calendar getDailyplanCalendar(String time, String timeZoneName) {

        if (time == null) {
            time = "00:00";
        }

        LOGGER.debug("Timezone is " + timeZoneName);
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

    public static OrderCounter getOrderCount(List<DBItemDailyPlanOrders> listOfPlannedOrders) {
        OrderCounter o = new OrderCounter();
        o.countSingle = 0L;
        o.countCycled = 0L;
        o.countCycledAll = 0L;

        DateFormat periodFormat = new SimpleDateFormat("hh:mm:ss");
        Map<CycleOrderKey, List<DBItemDailyPlanOrders>> mapOfCycledOrders = new TreeMap<CycleOrderKey, List<DBItemDailyPlanOrders>>();

        for (DBItemDailyPlanOrders dbItemDailyPlanOrders : listOfPlannedOrders) {

            if ((dbItemDailyPlanOrders.getStartMode() == 1)) {
                CycleOrderKey cycleOrderKey = new CycleOrderKey();
                cycleOrderKey.setPeriodBegin(periodFormat.format(dbItemDailyPlanOrders.getPeriodBegin()));
                cycleOrderKey.setPeriodEnd(periodFormat.format(dbItemDailyPlanOrders.getPeriodEnd()));
                cycleOrderKey.setRepeat(String.valueOf(dbItemDailyPlanOrders.getRepeatInterval()));
                cycleOrderKey.setOrderName(dbItemDailyPlanOrders.getOrderName());
                cycleOrderKey.setWorkflowPath(dbItemDailyPlanOrders.getWorkflowPath());
                if (mapOfCycledOrders.get(cycleOrderKey) == null) {
                    mapOfCycledOrders.put(cycleOrderKey, new ArrayList<DBItemDailyPlanOrders>());
                    o.countCycled = o.countCycled + 1;
                }

                mapOfCycledOrders.get(cycleOrderKey).add(dbItemDailyPlanOrders);
                o.countCycledAll = o.countCycledAll + 1;
            } else {
                o.countSingle = o.countSingle + 1;
            }
        }

        return o;
    }

    public static OrderCounter getOrderCount(Map<PlannedOrderKey, PlannedOrder> listOfPlannedOrders) {
        OrderCounter o = new OrderCounter();
        o.countSingle = 0L;
        o.countCycled = 0L;
        o.countCycledAll = 0L;

        Map<CycleOrderKey, List<PlannedOrder>> mapOfCycledOrders = new TreeMap<CycleOrderKey, List<PlannedOrder>>();
        for (PlannedOrder plannedOrder : listOfPlannedOrders.values()) {

            if ((plannedOrder.getPeriod().getSingleStart() == null)) {
                CycleOrderKey cycleOrderKey = new CycleOrderKey();
                cycleOrderKey.setPeriodBegin(plannedOrder.getPeriod().getBegin());
                cycleOrderKey.setPeriodEnd(plannedOrder.getPeriod().getEnd());
                cycleOrderKey.setRepeat(String.valueOf(plannedOrder.getPeriod().getRepeat()));
                cycleOrderKey.setOrderName(plannedOrder.getOrderName());
                cycleOrderKey.setWorkflowPath(plannedOrder.getSchedule().getWorkflowPath());
                if (mapOfCycledOrders.get(cycleOrderKey) == null) {
                    mapOfCycledOrders.put(cycleOrderKey, new ArrayList<PlannedOrder>());
                    o.countCycled = o.countCycled + 1;
                }

                mapOfCycledOrders.get(cycleOrderKey).add(plannedOrder);
                o.countCycledAll = o.countCycledAll + 1;
            } else {
                o.countSingle = o.countSingle + 1;
            }
        }

        return o;

    }

    public static String getStartTimeAsString(String timeZoneName, String dailyPlanStartTime, String periodBegin) {
        java.util.Calendar startCalendar;
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneName);
        java.util.Calendar now = java.util.Calendar.getInstance(timeZone);

        if (!"".equals(dailyPlanStartTime)) {
            startCalendar = DailyPlanHelper.getDailyplanCalendar(dailyPlanStartTime, timeZoneName);
        } else {
            startCalendar = DailyPlanHelper.getDailyplanCalendar(periodBegin, timeZoneName);
            startCalendar.add(java.util.Calendar.DATE, 1);
            startCalendar.add(java.util.Calendar.MINUTE, -30);
        }

        if (startCalendar.before(now)) {
            startCalendar.add(java.util.Calendar.DATE, 1);
        }

        SimpleDateFormat startTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        startTimeFormatter.setTimeZone(timeZone);
        String startTime = startTimeFormatter.format(startCalendar.getTime());
        return startTime;

    }

    public static String getDayOfYear(java.util.Calendar calendar) {
        SimpleDateFormat startTimeFormatter = new SimpleDateFormat("yyyy-MM-dd");
        String date = startTimeFormatter.format(calendar.getTime());
        return date;
    }

    private static String buildOrderId(String orderName, Long startTime, Integer startMode, String timeZone, String periodBegin) {
        
        String orderId = "";
        String dailyPlanDate = getDailyPlanDateAsString(startTime, timeZone, periodBegin);
        if (startMode == 0) {
            orderId = "#" + dailyPlanDate + "#P" + "<id" + startTime + ">-" + orderName;
        } else {
            orderId = "#" + dailyPlanDate + "#C" + "<id" + startTime + ">-<nr00000>-<size>-" + orderName;
        }
        return orderId;
    }

    public static String buildOrderId(Schedule schedule, VariableSet variableSet, Long startTime, Integer startMode, String timeZone,
            String periodBegin) {
        String orderName = "";
        if ((variableSet.getOrderName() == null) || (variableSet.getOrderName().isEmpty())) {
            orderName = schedule.getPath();
        }else {
            orderName = variableSet.getOrderName();
        }
        if (orderName.length() > 30) {
            orderName = orderName.substring(0, 30);
        }

        return buildOrderId(orderName, startTime, startMode, timeZone, periodBegin);
    }

    public static Date getNextDay(Date dateForPlan, OrderInitiatorSettings orderInitiatorSettings) throws ParseException {
        TimeZone timeZone = TimeZone.getTimeZone(orderInitiatorSettings.getTimeZone());

        java.util.Calendar calendar = java.util.Calendar.getInstance(timeZone);

        calendar.setTime(dateForPlan);
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
