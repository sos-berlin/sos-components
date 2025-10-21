package com.sos.js7.converter.commons.wokflow;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.sos.commons.util.SOSDate;

public class JS7WorkflowTimesCalculator {

    private final static int SECONDS_PER_DAY = 86_400;

    /** Calculates the number of seconds since the beginning of the month for the n-th occurrence of a weekday. e.g. for MonthlyWeekdayPeriod p = new
     * MonthlyWeekdayPeriod();<br/>
     * p.setSecondOfWeeks
     * 
     * @param weekOfMonth occurrence: 1 = First, 2 = Second, ...
     * @param weekday 1 = Monday, 2 = Tuesday, ..., 7 = Sunday
     * @param timeOfDaySeconds seconds of start time, e.g. of of 12:00
     * @param minute minute of the hour
     * @return seconds since the start of the month */
    public static long getSecondOfWeeks(int weekOfMonth, int weekday, long timeOfDaySeconds) {
        return timeOfDaySeconds + (weekday - 1) * SECONDS_PER_DAY + (weekOfMonth - 1) * 7L * SECONDS_PER_DAY;
    }

    public static long getSecondOfWeeks(int weekOfMonth, int weekday, String startTime) {
        return getSecondOfWeeks(weekOfMonth, weekday, SOSDate.getTimeAsSeconds(startTime));
    }

    /** e.g. for: MonthlyDatePeriod p = new MonthlyDatePeriod();<br/>
     * p.setSecondOfMonth
     * 
     * @param monthday
     * @param timeOfDaySeconds
     * @return */
    public static long getSecondOfMonth(int monthday, long timeOfDaySeconds) {
        return timeOfDaySeconds + (monthday - 1) * SECONDS_PER_DAY;
    }

    /** e.g. for(ultimos): MonthlyLastDatePeriod p = new MonthlyLastDatePeriod();<br/>
     * p.setLastSecondOfMonth
     * 
     * @param monthday
     * @param timeOfDaySeconds
     * @return */
    public static long getLastSecondOfMonth(int monthday, long timeOfDaySeconds) {
        return -1 * ((monthday * SECONDS_PER_DAY) + (SECONDS_PER_DAY - timeOfDaySeconds));
    }

    /** e.g. for: WeekdayPeriod p=new WeekdayPeriod();<br/>
     * p.setSecondOfWeek(null);
     * 
     * @param weekday
     * @param timeOfDaySeconds
     * @return */
    public static long getSecondOfWeek(int weekday, long timeOfDaySeconds) {
        return timeOfDaySeconds + (weekday - 1) * SECONDS_PER_DAY;
    }

    /** e.g. for: SpecificDatePeriod p = new SpecificDatePeriod();<br/>
     * p.setSecondOfMonth
     * 
     * @param isoDatetime e.g. 2025-01-29T18:00:00
     * @return */
    public static long getSecondsSinceLocalEpoch(String isoDatetime) {
        // ZoneOffset.UTC
        return LocalDateTime.parse(isoDatetime).toEpochSecond(ZoneOffset.UTC);
    }

    public static long getSecondsSinceLocalEpoch(String isoDate, String time) {
        return getSecondsSinceLocalEpoch(isoDate + "T" + time);
    }
}
