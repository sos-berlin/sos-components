package com.sos.js7.order.initiator.classes;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.inventory.model.Schedule;
import com.sos.joc.db.orders.DBItemDailyPlanOrders;

public class DailyPlanHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanHelper.class);
    private static final String UTC = "UTC";

    public static Date stringAsDate(String date) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.parse(date);
    }

    public static String dateAsString(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateS = formatter.format(date);
        return dateS;
    }

    public static String getDailyPlanDateAsString(Long startTime) {
        java.util.Calendar calendar = java.util.Calendar.getInstance(TimeZone.getTimeZone(UTC));
        calendar.setTime(new Date(startTime));
        return DailyPlanHelper.dateAsString(calendar.getTime());
    }

    public static Date getDailyPlanDateAsDate(Long startTime) {
        java.util.Calendar calendar = java.util.Calendar.getInstance(TimeZone.getTimeZone(UTC));
        calendar.setTime(new Date(startTime));
        return calendar.getTime();
    }

    public static java.util.Calendar getDailyplanCalendar(String time) {
   
        String timeZoneName = OrderInitiatorGlobals.orderInitiatorSettings.getTimeZone();
        if (time == null) {
            time = "00:00";
        }

        LOGGER.debug("Timezone is " + timeZoneName);
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneName);
        java.util.Calendar calendar = java.util.Calendar.getInstance(timeZone);

        String[] timeArray = time.split(":");
        int hours = 0;
        int minutes = 0;
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
        if (timeArray.length == 2) {
            try {
                hours = Integer.parseInt(timeArray[0]);
                minutes = Integer.parseInt(timeArray[1]);
            } catch (NumberFormatException e) {
                LOGGER.warn("Wrong time format for: " + time);
                hours = 0;
                minutes = 0;
                time = "00:00";
            }
        }

        calendar.set(java.util.Calendar.HOUR_OF_DAY, hours);
        calendar.set(java.util.Calendar.MINUTE, minutes);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        calendar.getTimeInMillis();

        return calendar;
    }
    
    public static String getStartTimeAsString(){
        java.util.Calendar startCalendar;
        if (!"".equals(OrderInitiatorGlobals.orderInitiatorSettings.getDailyPlanStartTime())) {
            startCalendar = DailyPlanHelper.getDailyplanCalendar(OrderInitiatorGlobals.orderInitiatorSettings.getDailyPlanStartTime());
        } else {
            startCalendar = DailyPlanHelper.getDailyplanCalendar(OrderInitiatorGlobals.orderInitiatorSettings.getPeriodBegin());
            startCalendar.add(java.util.Calendar.DATE, 1);
            startCalendar.add(java.util.Calendar.MINUTE, -30);
        }
        
        String timeZoneName = OrderInitiatorGlobals.orderInitiatorSettings.getTimeZone();
      
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneName);

        SimpleDateFormat startTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        startTimeFormatter.setTimeZone(timeZone);
        String startTime = startTimeFormatter.format(startCalendar.getTime());
        return startTime;

    }


    public static String getDayOfYear(java.util.Calendar calendar) {

        int month = calendar.get(java.util.Calendar.MONTH) + 1;
        int dayOfMonth = calendar.get(java.util.Calendar.DAY_OF_MONTH);
        int year = calendar.get(java.util.Calendar.YEAR);

        String result = String.valueOf(year) + String.valueOf(month) + String.valueOf(dayOfMonth);
        LOGGER.debug("period day of year is: " + result);

        return result;
    }

    private static String buildOrderId(Path path, Long startTime, Integer startMode) {
        String shortScheduleName = path.getFileName().toString();
        if (shortScheduleName.length() > 30) {
            shortScheduleName = shortScheduleName.substring(0, 30);
        }

        String orderId = "";
        String dailyPlanDate = getDailyPlanDateAsString(startTime);
        if (startMode == 0) {
            orderId = "#" + dailyPlanDate + "#P" + "<id" + startTime + ">-" + shortScheduleName;
        } else {
            orderId = "#" + dailyPlanDate + "#C" + "<id" + startTime + ">-<nr00000>-<size>-" + shortScheduleName;
        }
        return orderId;
    }

    public static String buildOrderId(Schedule o, Long startTime, Integer startMode) {
        return buildOrderId(Paths.get(o.getPath()), startTime, startMode);
    }

    public static String buildOrderId(DBItemDailyPlanOrders dbItemDailyPlanOrders) {
        return buildOrderId(Paths.get(dbItemDailyPlanOrders.getSchedulePath()), dbItemDailyPlanOrders.getPlannedStart().getTime(),
                dbItemDailyPlanOrders.getStartMode());
    }

    public static Date getNextDay(Date dateForPlan) throws ParseException {
        TimeZone timeZone = TimeZone.getTimeZone(OrderInitiatorGlobals.orderInitiatorSettings.getTimeZone());

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
                newOrderId = newOrderId +  add + "-" + stringSplit[stringSplit.length-1];
            }else {
                newOrderId = newOrderId +  stringSplit[3] + "-" + stringSplit[4] + "-" + add + "-" + stringSplit[stringSplit.length-1];
            }
        } else {
            newOrderId = oldOrderId;
        }
        return newOrderId;
    }
}
