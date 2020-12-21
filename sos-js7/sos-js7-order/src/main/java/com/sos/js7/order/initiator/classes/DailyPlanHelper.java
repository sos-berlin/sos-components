package com.sos.js7.order.initiator.classes;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.webservices.order.initiator.model.Schedule;

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
    
    public static String getDailyPlanDate(Long startTime) {
        java.util.Calendar calendar = java.util.Calendar.getInstance(TimeZone.getTimeZone(UTC));
        calendar.setTime(new Date(startTime));
        return DailyPlanHelper.dateAsString(calendar.getTime());
    }


    public static java.util.Calendar getDailyplanCalendar() {
        String periodBegin = OrderInitiatorGlobals.orderInitiatorSettings.getPeriodBegin();
        String timeZoneName = OrderInitiatorGlobals.orderInitiatorSettings.getTimeZone();
        if (periodBegin == null) {
            periodBegin = "00:00";
        }
        
 

        LOGGER.debug("Timezone for period is " + timeZoneName);
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneName);
        java.util.Calendar calendar = java.util.Calendar.getInstance(timeZone);

        String[] period = periodBegin.split(":");
        int hours = 0;
        int minutes = 0;
        if (period.length == 1) {
            try {
                hours = Integer.parseInt(period[0]);
            } catch (NumberFormatException e) {
                LOGGER.warn("Wrong time format for sos.jobstream_period_begin: " + periodBegin);
                hours = 0;
                minutes = 0;
                periodBegin = "00:00";
            }
        }
        if (period.length == 2) {
            try {
                hours = Integer.parseInt(period[0]);
                minutes = Integer.parseInt(period[1]);
            } catch (NumberFormatException e) {
                LOGGER.warn("Wrong time format for period_begin: " + periodBegin);
                hours = 0;
                minutes = 0;
                periodBegin = "00:00";
            }
        }

        calendar.set(java.util.Calendar.HOUR_OF_DAY, hours);
        calendar.set(java.util.Calendar.MINUTE, minutes);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.add(java.util.Calendar.MINUTE, 30 * -1);


        return calendar;
    }

    public static String getDayOfYear(java.util.Calendar calendar) {

        int month = calendar.get(java.util.Calendar.MONTH) + 1;
        int dayOfMonth = calendar.get(java.util.Calendar.DAY_OF_MONTH);
        int year = calendar.get(java.util.Calendar.YEAR);

        String result = String.valueOf(year) + String.valueOf(month) + String.valueOf(dayOfMonth);
        LOGGER.debug("period day of year is: " + result);

        return result;
    }
    
    public static String buildOrderKey(Schedule o, Long startTime) {
        Path path = Paths.get(o.getPath());
        String shortScheduleName = path.getFileName().toString();
        if (shortScheduleName.length() > 30) {
            shortScheduleName = shortScheduleName.substring(0, 30);
        }
        return "#" + getDailyPlanDate(startTime) + "#P" + "<id" + startTime + ">-" + shortScheduleName;
    }
    
    public static Date getNextDay(Date dateForPlan) throws ParseException {
        TimeZone timeZone = TimeZone.getTimeZone(OrderInitiatorGlobals.orderInitiatorSettings.getTimeZone());

        java.util.Calendar calendar = java.util.Calendar.getInstance(timeZone);

        calendar.setTime(dateForPlan);
        calendar.add(java.util.Calendar.DATE, 1);
        return calendar.getTime();
    }

}
