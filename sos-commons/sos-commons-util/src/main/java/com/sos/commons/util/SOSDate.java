package com.sos.commons.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSInvalidDataException;

public class SOSDate {

    private static Logger LOGGER = LoggerFactory.getLogger(SOSDate.class);

    public static final String DATE_FORMAT = new String("yyyy-MM-dd");
    public static final String TIME_FORMAT = new String("HH:mm:ss");
    public static final String DATETIME_FORMAT = new String(DATE_FORMAT + " " + TIME_FORMAT);
    public static final String DATETIME_FORMAT_WITH_ZONE_OFFSET = new String(DATE_FORMAT + "'T'" + TIME_FORMAT + "Z");// 2022-04-07T16:37:35+0200

    public static final String TIMEZONE_UTC = "Etc/UTC";
    private static final boolean LENIENT = false;

    // returns Date from String
    public static Date getDate(String date) throws SOSInvalidDataException {
        return getDate(date, null);
    }

    public static Date getDate(String date, TimeZone timeZone) throws SOSInvalidDataException {
        return parse(date, DATE_FORMAT, timeZone);
    }

    public static Date getDateTime(String date) throws SOSInvalidDataException {
        return getDateTime(date, null);
    }

    public static Date getDateTime(String date, TimeZone timeZone) throws SOSInvalidDataException {
        return parse(date, DATETIME_FORMAT, timeZone);
    }

    public static Date tryGetDateTime(String date, TimeZone timeZone) {
        try {
            return parse(date, DATETIME_FORMAT, timeZone);
        } catch (SOSInvalidDataException e) {
            return null;
        }
    }

    public static Date parse(String date, String format) throws SOSInvalidDataException {
        return parse(date, format, null);
    }

    public static Date parse(String date, String format, TimeZone timeZone) throws SOSInvalidDataException {
        if (date == null) {
            return null;
        }
        DateFormat df;
        try {
            df = new SimpleDateFormat(format == null ? DATETIME_FORMAT : format);
            df.setLenient(LENIENT);
            if (timeZone != null) {
                df.setTimeZone(timeZone);
            }
            return df.parse(date);
        } catch (Throwable e) {
            String add = timeZone == null ? "" : "[" + timeZone + "]";
            throw new SOSInvalidDataException(String.format("[%s][%s]%s%s", date, format, add, e.toString()), e);
        }
    }

    // returns String from current Date
    public static String getCurrentDateAsString() throws SOSInvalidDataException {
        return getCurrentDateAsString(null);
    }

    public static String getCurrentDateAsString(TimeZone timeZone) throws SOSInvalidDataException {
        return format(new Date(), DATE_FORMAT, timeZone);
    }

    public static String getCurrentDateTimeAsString() throws SOSInvalidDataException {
        return getCurrentDateTimeAsString(null);
    }

    public static String tryGetCurrentDateTimeAsString() {
        try {
            return getCurrentDateTimeAsString(null);
        } catch (SOSInvalidDataException e) {
            return e.toString();
        }
    }

    public static String getCurrentDateTimeAsString(TimeZone timeZone) throws SOSInvalidDataException {
        return format(new Date(), DATETIME_FORMAT, timeZone);
    }

    // returns String from Date
    public static String getDateAsString(Date date) throws SOSInvalidDataException {
        return getDateAsString(date, null);
    }

    public static String getDateAsString(Date date, TimeZone timeZone) throws SOSInvalidDataException {
        return format(date, DATE_FORMAT, timeZone);
    }

    public static String getDateTimeAsString(Date date) throws SOSInvalidDataException {
        return getDateTimeAsString(date, null);
    }

    public static String tryGetDateTimeAsString(Date date) {
        try {
            return getDateTimeAsString(date, null);
        } catch (SOSInvalidDataException e) {
            return e.toString();
        }
    }

    public static String tryGetDateTimeAsString(Instant date) {
        try {
            return getDateTimeAsString(Date.from(date), null);
        } catch (SOSInvalidDataException e) {
            return e.toString();
        }
    }

    public static String tryGetDateTimeAsString(long datetime) {
        try {
            return getDateTimeAsString(datetime);
        } catch (SOSInvalidDataException e) {
            return e.toString();
        }
    }

    public static String getDateTimeAsString(long datetime) throws SOSInvalidDataException {
        return format(new Date(datetime), DATETIME_FORMAT);
    }

    public static String getDateTimeAsString(Date date, TimeZone timeZone) throws SOSInvalidDataException {
        return format(date, SOSDate.DATETIME_FORMAT, timeZone);
    }

    public static String getDateTimeWithZoneOffsetAsString(Date date, TimeZone timeZone) throws SOSInvalidDataException {
        return format(date, SOSDate.DATETIME_FORMAT_WITH_ZONE_OFFSET, timeZone);
    }

    public static String getTimeAsString(Date date) throws SOSInvalidDataException {
        return getTimeAsString(date, null);
    }

    public static String getTimeAsString(Long seconds) throws SOSInvalidDataException {
        long HH = seconds / 3600;
        long MM = (seconds % 3600) / 60;
        long SS = seconds % 60;
        return String.format("%02d:%02d:%02d", HH, MM, SS);
    }

    public static String getTimeAsString(Date date, TimeZone timeZone) throws SOSInvalidDataException {
        return format(date, SOSDate.TIME_FORMAT, timeZone);
    }

    public static String getTimeAsString(Instant it) {
        try {
            return it.toString().split("T")[1].replace("Z", "");
        } catch (Throwable t) {
            return it.toString();
        }
    }

    public static String getDateWithTimeZoneAsString(Date date, String timeZone) throws SOSInvalidDataException {
        return format(date, DATE_FORMAT, SOSString.isEmpty(timeZone) ? null : TimeZone.getTimeZone(timeZone));
    }

    public static String tryGetDateWithTimeZoneAsString(Date date, String timeZone) {
        try {
            return getDateWithTimeZoneAsString(date, timeZone);
        } catch (SOSInvalidDataException e) {
            return e.toString();
        }
    }

    public static String getDateTimeWithTimeZoneAsString(Date date, String timeZone) throws SOSInvalidDataException {
        return format(date, DATETIME_FORMAT, SOSString.isEmpty(timeZone) ? null : TimeZone.getTimeZone(timeZone));
    }

    public static String tryGetDateTimeWithTimeZoneAsString(Date date, String timeZone) {
        try {
            return getDateTimeWithTimeZoneAsString(date, timeZone);
        } catch (SOSInvalidDataException e) {
            return e.toString();
        }
    }

    // returns String from Calendar
    public static String getDateAsString(Calendar calendar) throws SOSInvalidDataException {
        if (calendar == null) {
            return null;
        }
        return format(calendar.getTime(), DATE_FORMAT);
    }

    public static String getDateTimeAsString(Calendar calendar) throws SOSInvalidDataException {
        if (calendar == null) {
            return null;
        }
        return format(calendar.getTime(), DATETIME_FORMAT);
    }

    // returns String from long
    public static String getDateAsString(long datetime) throws SOSInvalidDataException {
        return format(new Date(datetime), DATE_FORMAT);
    }

    // returns String from String
    public static String format(String date, String format) throws SOSInvalidDataException {
        if (date == null) {
            return null;
        }
        Date d = null;
        if ("%now".equals(date)) {
            d = new Date();
        } else {
            d = getDateTime(date);
        }
        return format(d, format, null);
    }

    // returns String from Date
    public static String format(Date date, String format) throws SOSInvalidDataException {
        return format(date, format, null);
    }

    public static String format(Date date, String format, TimeZone timeZone) throws SOSInvalidDataException {
        if (date == null) {
            return null;
        }
        DateFormat df;
        try {
            df = new SimpleDateFormat(format == null ? DATETIME_FORMAT : format);
            df.setLenient(LENIENT);
            if (timeZone != null) {
                df.setTimeZone(timeZone);
            }
            return df.format(date);
        } catch (Throwable e) {
            String add = timeZone == null ? "" : "[" + timeZone + "]";
            throw new SOSInvalidDataException(String.format("[%s][%s]%s%s", date, format, add, e.toString()), e);
        }
    }

    public static Long getMinutes(Date d) {
        if (d == null) {
            return 0L;
        }
        return d.getTime() / 1000 / 60;
    }

    public static Long getSeconds(Date d) {
        if (d == null) {
            return 0L;
        }
        return d.getTime() / 1000;
    }

    /** @TODO */
    public static int getWeek(Date d) {
        if (d == null) {
            return 0;
        }
        return d.toInstant().atZone(ZoneId.systemDefault()).get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    }

    /** @TODO */
    public static int getMonth(Date d) {
        if (d == null) {
            return 0;
        }
        return d.toInstant().atZone(ZoneId.systemDefault()).getMonthValue();
    }

    /** @TODO */
    public static int getQuarter(Date d) {
        if (d == null) {
            return 0;
        }
        return d.toInstant().atZone(ZoneId.systemDefault()).get(IsoFields.QUARTER_OF_YEAR);
    }

    /** @TODO */
    public static int getYear(Date d) {
        if (d == null) {
            return 0;
        }
        return d.toInstant().atZone(ZoneId.systemDefault()).get(IsoFields.WEEK_BASED_YEAR);
    }

    public static String getDuration(Instant start, Instant end) {
        return getDuration(Duration.between(start, end));
    }

    public static String getDuration(Duration duration) {
        return duration == null ? null : duration.toString().replace("PT", "").toLowerCase();
    }

    public static String getDurationOfSeconds(long val) {
        return getDuration(Duration.ofSeconds(val));
    }

    public static String getDurationOfMillis(long val) {
        return getDuration(Duration.ofMillis(val));
    }

    public static boolean equals(Date val1, Date val2) {
        if (val1 == null && val2 == null) {
            return true;
        } else if (val1 == null || val2 == null) {
            return false;
        }
        return val1.getTime() == val2.getTime();
    }

    /** TODO Weeks, Years not supported...<br/>
     * Examples: SOSDate.add(new Date(),1, ChronoUnit.DAYS);<br/>
     * SOSDate.add(new Date(),-1, ChronoUnit.DAYS);
     * 
     * @param input
     * @param amountToAdd
     * @param unit
     * @return */
    public static Date add(Date input, long amountToAdd, TemporalUnit unit) {
        return add(input.toInstant(), amountToAdd, unit);
    }

    public static Date add(Instant input, long amountToAdd, TemporalUnit unit) {
        if (input == null) {
            return null;
        }
        return Date.from(input.plus(amountToAdd, unit));
    }

    /** @param range e.g.: m - minutes,s -seconds, ms - milliseconds
     * @param age , e.g.: 1w 2h 45s
     * @return age in minutes, seconds or milliseconds
     * @throws SOSInvalidDataException */
    public static Long resolveAge(String range, String age) throws SOSInvalidDataException {
        if (SOSString.isEmpty(age)) {
            throw new SOSInvalidDataException("age is empty");
        }

        int multiplicatorSeconds = -1;
        int multiplicatorMilliseconds = -1;

        switch (range) {
        case "ms": // milliseconds
            multiplicatorSeconds = 60;
            multiplicatorMilliseconds = 1_000;
            break;
        case "s": // seconds
            multiplicatorSeconds = 60;
            multiplicatorMilliseconds = 1;
            break;
        default: // minutes
            range = "m";
            multiplicatorSeconds = 1;
            multiplicatorMilliseconds = 1;
            break;
        }

        Long result = Long.valueOf(0);
        String[] parts = age.trim().toLowerCase().split(" ");
        for (String part : parts) {
            if (!SOSString.isEmpty(part)) {
                String numericalPart = part;
                try {
                    int len = part.length() - 1;
                    String lastCharacter = part.substring(len);
                    numericalPart = part.substring(0, len);
                    switch (lastCharacter) {
                    case "w":
                        result += multiplicatorMilliseconds * multiplicatorSeconds * 60 * 24 * 7 * Long.parseLong(numericalPart);
                        break;
                    case "d":
                        result += multiplicatorMilliseconds * multiplicatorSeconds * 60 * 24 * Long.parseLong(numericalPart);
                        break;
                    case "h":
                        result += multiplicatorMilliseconds * multiplicatorSeconds * 60 * Long.parseLong(numericalPart);
                        break;
                    case "m":
                        result += multiplicatorMilliseconds * multiplicatorSeconds * Long.parseLong(numericalPart);
                        break;
                    case "s":
                        if (range.equals("m")) {
                            LOGGER.warn("[ignored][" + part + "]");
                            continue;
                        }
                        result += multiplicatorMilliseconds * Long.parseLong(numericalPart);
                        break;
                    default:
                        result += Long.parseLong(part);
                        break;
                    }
                } catch (Exception ex) {
                    throw new SOSInvalidDataException(String.format("[invalid numeric value][%s][%s][%s]%s", age, part, numericalPart, ex.toString()),
                            ex);
                }
            }
        }
        return result;
    }

    /** @param val, format: s, hh:mm:ss, hh:mm<br/>
     *            e.g. 1, 00:00:05, 01:00 */
    public static long getTimeAsSeconds(String val) {
        if (SOSString.isEmpty(val)) {
            return 0L;
        }

        int[] num = { 1, 60, 3600, 3600 * 24 };
        int j = 0;
        long seconds = 0L;
        String[] arr = val.split(":");
        if (arr.length == 2) {
            j++;
        }
        for (int i = arr.length - 1; i >= 0; i--) {
            try {
                seconds += Integer.valueOf(arr[i].trim()) * num[j++];
            } catch (Throwable e) {
            }
        }
        return seconds;
    }

    public static long getTimeAsMillis(String val) {
        return getTimeAsSeconds(val) * 1_000;
    }

    /** @param startDateISO text string such as 2024-12-11
     * @param endDateISO text string such as 2024-12-01
     * @return */
    public static List<String> getDatesInRange(String startDateISO, String endDateISO) {
        LocalDate start = LocalDate.parse(startDateISO);
        LocalDate end = LocalDate.parse(endDateISO);

        List<String> dates = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        while (!start.isAfter(end)) {
            dates.add(start.format(formatter));
            start = start.plusDays(1);
        }
        return dates;
    }

    /** @param dateISO text string such as 2025-01-01
     * @return text string such as 2025-01-02 */
    public static String getNextDate(String dateISO) {
        return LocalDate.parse(dateISO).plusDays(1).format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    /** @param dateISO text string such as 2025-01-01
     * @return text string such as 2024-12-31 */
    public static String getPreviousDate(String dateISO) {
        return LocalDate.parse(dateISO).minusDays(1).format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    /** Examples(fromTime,toTime): <br/>
     * - 04:00 to 23:00, times=00:00,01:00,02:00,03:00,04:00,05:00,07:00,23:01<br/>
     * -- return 04:00,05:00,07:00<br/>
     * Supports intervals that cross midnight, e.g.<br/>
     * - 04:00 to 02:00, times=00:00,01:00,02:00,03:00,04:00,05:00,07:00,23:01<br/>
     * -- return 00:00,01:00,02:00,04:00,05:00,07:00,23:01<br/>
     * 
     * @param fromTime format: s, hh:mm:ss, hh:mm
     * @param toTime format: s, hh:mm:ss, hh:mm
     * @param times format: s, hh:mm:ss, hh:mm<br/>
     * 
     * @return times in the fromTime-toTime range */
    public static List<String> getFilteredTimesInRange(String fromTime, String toTime, List<String> times) {
        return getFilteredTimesInRange(getTimeAsSeconds(fromTime), getTimeAsSeconds(toTime), times);
    }

    /** @param fromSeconds - day seconds based from format s, hh:mm:ss, hh:mm
     * @param toSeconds - day seconds based from format s, hh:mm:ss, hh:mm
     * @param times
     * @return */
    public static List<String> getFilteredTimesInRange(long fromSeconds, long toSeconds, List<String> times) {
        // from=00:00 to=00:00
        if (fromSeconds == 0 && toSeconds == 0) {
            return times;
        }

        boolean isOverMidnight = fromSeconds > toSeconds;// from=04:00 to=01:00
        final long endOfDay = 86_400;// 24h in seconds

        return times.stream().filter(time -> {
            long timeSeconds = getTimeAsSeconds(time);
            if (isOverMidnight) {
                return (timeSeconds >= fromSeconds && timeSeconds < endOfDay) // before midnight
                        || (timeSeconds >= 0 && timeSeconds <= toSeconds);  // after midnight
            } else {
                return timeSeconds >= fromSeconds && timeSeconds <= toSeconds;
            }
        }).collect(Collectors.toList());
    }

    /** Returns the time span in seconds between two time points within a day.<br/>
     * Supports intervals that cross midnight, e.g., from 23:00:00 to 01:00:00.
     *
     * @param fromTime The start time (e.g., 23:00:00).
     * @param toTime The end time (e.g., 01:00:00).
     * @return The time span in seconds. */
    public static long getDaySpanInSeconds(String fromTime, String toTime) {
        return getDaySpanInSeconds(getTimeAsSeconds(fromTime), getTimeAsSeconds(toTime));
    }

    public static long getDaySpanInSeconds(long fromSeconds, long toSeconds) {
        final long endOfDay = 86_400;
        if (fromSeconds == toSeconds) {
            return endOfDay;
        } else if (fromSeconds < toSeconds) {
            return toSeconds - fromSeconds;
        } else {
            return endOfDay - fromSeconds + toSeconds;
        }
    }

    public static void main(String[] args) {
        try {
            Date d = new Date();
            System.out.println(SOSDate.getWeek(d));
            System.out.println(SOSDate.getMonth(d));
            System.out.println(SOSDate.getYear(d));
            System.out.println(SOSDate.format(d, "yyyy-MM-dd HH:mm:ss.SSSZZZZ", TimeZone.getTimeZone("PST")));
            System.out.println(SOSDate.format(d, "yyyy-MM-dd HH:mm:ss.SSSZZZZ", TimeZone.getTimeZone("Europe/Berlin")));
            System.out.println(SOSDate.getDurationOfSeconds(0));
            System.out.println(SOSDate.getDurationOfSeconds(60));
            System.out.println(SOSDate.getDurationOfSeconds(100_000));
            System.out.println(SOSDate.add(new Date(), 3, ChronoUnit.DAYS));
            System.out.println(SOSDate.add(new Date(), -3, ChronoUnit.DAYS));

            List<String> times = Arrays.asList("00:02", "04:01", "04:30", "05:00", "05:02", "23:00", "23:01");
            System.out.println(SOSDate.getFilteredTimesInRange("05:00", "23:00", times));
            System.out.println(SOSDate.getFilteredTimesInRange("05:00", "04:01", times));
        } catch (Exception e) {
            System.err.println("..error: " + e.toString());
        }
    }

}