package com.sos.commons.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSInvalidDataException;

public class SOSDate {

    private static Logger LOGGER = LoggerFactory.getLogger(SOSDate.class);

    public static final String DATE_FORMAT = new String("yyyy-MM-dd");
    public static final String DATETIME_FORMAT = new String("yyyy-MM-dd HH:mm:ss");

    private static final boolean LENIENT = false;

    // returns Date from String
    public static Date getDate(String date) throws SOSInvalidDataException {
        return parse(date, DATE_FORMAT);
    }

    public static Date getDateTime(String date) throws SOSInvalidDataException {
        return parse(date, DATETIME_FORMAT);
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
        return format(new Date(), DATE_FORMAT);
    }

    public static String getCurrentDateTimeAsString() throws SOSInvalidDataException {
        return format(new Date(), DATETIME_FORMAT);
    }

    // returns String from Date
    public static String getDateAsString(Date date) throws SOSInvalidDataException {
        return format(date, DATE_FORMAT);
    }

    public static String getDateTimeAsString(Date date) throws SOSInvalidDataException {
        return format(date, SOSDate.DATETIME_FORMAT, null);
    }

    public static String getDateWithTimeZoneAsString(Date date, String timeZone) throws SOSInvalidDataException {
        return format(date, DATE_FORMAT, SOSString.isEmpty(timeZone) ? null : TimeZone.getTimeZone(timeZone));
    }

    public static String getDateTimeWithTimeZoneAsString(Date date, String timeZone) throws SOSInvalidDataException {
        return format(date, DATETIME_FORMAT, SOSString.isEmpty(timeZone) ? null : TimeZone.getTimeZone(timeZone));
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

    public static String getDateTimeAsString(long datetime) throws SOSInvalidDataException {
        return format(new Date(datetime), DATETIME_FORMAT);
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
        return d.getTime() / 1000 / 60;
    }

    public static Long getSeconds(Date d) {
        return d.getTime() / 1000;
    }

    /** @TODO */
    public static int getWeek(Date d) {
        return d.toInstant().atZone(ZoneId.systemDefault()).get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    }

    /** @TODO */
    public static int getMonth(Date d) {
        return d.toInstant().atZone(ZoneId.systemDefault()).getMonthValue();
    }

    /** @TODO */
    public static int getQuarter(Date d) {
        return d.toInstant().atZone(ZoneId.systemDefault()).get(IsoFields.QUARTER_OF_YEAR);
    }

    /** @TODO */
    public static int getYear(Date d) {
        return d.toInstant().atZone(ZoneId.systemDefault()).get(IsoFields.WEEK_BASED_YEAR);
    }

    public static String getTime(Instant it) {
        try {
            return it.toString().split("T")[1].replace("Z", "");
        } catch (Throwable t) {
            return it.toString();
        }
    }

    public static String getDuration(Instant start, Instant end) {
        return getDuration(Duration.between(start, end));
    }

    public static String getDuration(Duration duration) {
        return duration == null ? null : duration.toString().replace("PT", "").toLowerCase();
    }

    public static String getDuration(long seconds) {
        return getDuration(Duration.ofSeconds(seconds));
    }

    public static boolean equals(Date val1, Date val2) {
        if (val1 == null && val2 == null) {
            return true;
        } else if (val1 == null || val2 == null) {
            return false;
        }
        return val1.getTime() == val2.getTime();
    }

    // TODO Weeks, Years not supported...
    public static Date add(Date input, long amountToAdd, TemporalUnit unit) {
        return add(input.toInstant(), amountToAdd, unit);
    }

    public static Date add(Instant input, long amountToAdd, TemporalUnit unit) {
        if (input == null) {
            return null;
        }
        if (amountToAdd > 0) {
            return Date.from(input.plus(amountToAdd, unit));
        } else {
            return Date.from(input.minus(amountToAdd, unit));
        }
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

    public static void main(String[] args) {
        try {
            Date d = new Date();
            System.out.println(SOSDate.getWeek(d));
            System.out.println(SOSDate.getMonth(d));
            System.out.println(SOSDate.getYear(d));
            System.out.println(SOSDate.format(d, "yyyy-MM-dd HH:mm:ss.SSSZZZZ", TimeZone.getTimeZone("PST")));
            System.out.println(SOSDate.format(d, "yyyy-MM-dd HH:mm:ss.SSSZZZZ", TimeZone.getTimeZone("Europe/Berlin")));
            System.out.println(SOSDate.getDuration(0));
            System.out.println(SOSDate.getDuration(60));
            System.out.println(SOSDate.getDuration(100_000));
        } catch (Exception e) {
            System.err.println("..error: " + e.toString());
        }
    }

}