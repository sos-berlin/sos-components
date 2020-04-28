package com.sos.commons.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.IsoFields;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class SOSDate {

    private static String outputDateTimeFormat = new String("MM/dd/yy HH:mm:ss");
    private static boolean lenient = false;
    public static String dateFormat = new String("yyyy-MM-dd");
    public static String dateTimeFormat = new String("yyyy-MM-dd HH:mm:ss");
    public static final int SHORT = DateFormat.SHORT;
    public static final int MEDIUM = DateFormat.MEDIUM;
    public static final int LONG = DateFormat.LONG;
    public static final int FULL = DateFormat.FULL;
    public static int dateStyle = DateFormat.SHORT;
    public static int timeStyle = DateFormat.SHORT;
    public static Locale locale = Locale.UK;

    public void setDateFormat(String dateFormat) {
        SOSDate.dateFormat = dateFormat;
    }

    public static String getDateFormat() {
        return SOSDate.dateFormat;
    }

    public static void setDateTimeFormat(String dateTimeFormat) {
        SOSDate.dateTimeFormat = dateTimeFormat;
    }

    public static String getDateTimeFormat() {
        return SOSDate.dateTimeFormat;
    }

    public static Date getCurrentDate() throws Exception {
        return SOSDate.getDate();
    }

    public static String getCurrentDateAsString() throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat(SOSDate.dateFormat);
        formatter.setLenient(lenient);
        Calendar now = Calendar.getInstance();
        return formatter.format(now.getTime());
    }

    public static String getCurrentDateAsString(String dateFormat) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        formatter.setLenient(lenient);
        Calendar now = Calendar.getInstance();
        return formatter.format(now.getTime());
    }

    public static Date getCurrentTime() throws Exception {
        return SOSDate.getTime();
    }

    public static String getCurrentTimeAsString() throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat(SOSDate.dateTimeFormat);
        formatter.setLenient(lenient);
        Calendar now = Calendar.getInstance();
        return formatter.format(now.getTime());
    }

    public static String getCurrentTimeAsString(String dateTimeFormat) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat(dateTimeFormat);
        formatter.setLenient(lenient);
        Calendar now = Calendar.getInstance();
        return formatter.format(now.getTime());
    }

    public static Date getDate() throws Exception {
        SimpleDateFormat formatter;
        try {
            formatter = new SimpleDateFormat(SOSDate.dateFormat);
            formatter.setLenient(lenient);
        } catch (Exception e) {
            throw new Exception("invalid date format string: " + e.toString());
        }
        try {
            Calendar now = Calendar.getInstance();
            return now.getTime();
        } catch (Exception e) {
            throw new Exception("illegal date value: " + e.toString());
        }
    }

    public static String getDateAsString() throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat(SOSDate.dateFormat);
        formatter.setLenient(lenient);
        return formatter.format(SOSDate.getDate());
    }

    public static Date getDate(String dateStr) throws Exception {
        SimpleDateFormat formatter;
        try {
            formatter = new SimpleDateFormat(SOSDate.dateFormat);
            formatter.setLenient(lenient);
        } catch (Exception e) {
            throw new Exception("invalid date format string: " + e.toString());
        }
        try {
            return formatter.parse(dateStr);
        } catch (Exception e) {
            throw new Exception("illegal date value: " + e.toString());
        }
    }

    public static String getDateAsString(Date date) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat(SOSDate.dateFormat);
        formatter.setLenient(lenient);
        return formatter.format(date);
    }

    public static Date getDate(String dateStr, String dateFormat) throws Exception {
        SimpleDateFormat formatter;
        try {
            formatter = new SimpleDateFormat(dateFormat);
            formatter.setLenient(lenient);
        } catch (Exception e) {
            throw new Exception("invalid date format string: " + e.toString());
        }
        try {
            return formatter.parse(dateStr);
        } catch (Exception e) {
            throw new Exception("illegal date string: " + e.toString());
        }
    }

    public static String getDateAsString(Date date, String dateFormat) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        formatter.setLenient(lenient);
        return formatter.format(date);
    }

    public static String getDateAsString(Date date, String dateFormat, TimeZone zone) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        formatter.setTimeZone(zone);
        formatter.setLenient(lenient);
        return formatter.format(date);
    }

    public static Date getTime() throws Exception {
        try {
            Calendar now = Calendar.getInstance();
            return now.getTime();
        } catch (Exception e) {
            throw new Exception("illegal date value: " + e.toString());
        }
    }

    public static String getTimeAsString() throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat(SOSDate.dateTimeFormat);
        formatter.setLenient(lenient);
        return formatter.format(SOSDate.getTime());
    }

    public static Date getTime(String dateTimeStr) throws Exception {
        SimpleDateFormat formatter;
        try {
            formatter = new SimpleDateFormat(SOSDate.dateTimeFormat);
            formatter.setLenient(lenient);
        } catch (Exception e) {
            throw new Exception("invalid date format string: " + e.toString());
        }
        try {
            return formatter.parse(dateTimeStr);
        } catch (Exception e) {
            throw new Exception("illegal date value: " + e.toString());
        }
    }

    public static String getTimeAsString(Date date) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat(SOSDate.dateTimeFormat);
        formatter.setLenient(lenient);
        return formatter.format(date);
    }

    public static Date getTime(String dateTimeStr, String dateTimeFormat) throws Exception {
        SimpleDateFormat formatter;
        try {
            formatter = new SimpleDateFormat(dateTimeFormat);
            formatter.setLenient(lenient);
        } catch (Exception e) {
            throw new Exception("invalid date format string: " + e.toString());
        }
        try {
            return formatter.parse(dateTimeStr);
        } catch (Exception e) {
            throw new Exception("illegal date value: " + e.toString());
        }
    }

    public static String getTimeAsString(Date date, String dateTimeFormat) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat(dateTimeFormat);
        formatter.setLenient(lenient);
        return formatter.format(date);
    }

    public static boolean isValidDate(String text, int dateStyle, Locale locale) {
        try {
            getDate(text, dateStyle, locale);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidTime(String text, int timeStyle, Locale locale) {
        try {
            getTime(text, timeStyle, locale);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidDateTime(String text, int dateStyle, int timeStyle, Locale locale) {
        try {
            getDateTime(text, dateStyle, timeStyle, locale);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getDateAsString(Date date, int dateStyle, Locale locale) {
        DateFormat formatter = DateFormat.getDateInstance(dateStyle, locale);
        return formatter.format(date);
    }

    public static String getTimeAsString(Date date, int timeStyle, Locale locale) {
        DateFormat formatter = DateFormat.getTimeInstance(timeStyle, locale);
        return formatter.format(date);
    }

    public static String getDateTimeAsString(Date date, int dateStyle, int timeStyle, Locale locale) {
        DateFormat formatter = DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
        return formatter.format(date);
    }

    public static String getDateTimeAsString(String datestr) throws Exception {
        return getDateTimeAsString(datestr, null);
    }

    public static String getDateTimeAsString(String datestr, String outputDateTimeFormat) throws Exception {
        Date date = null;
        if ("%now".equals(datestr)) {
            date = new Date();
        } else {
            date = SOSDate.getTime(datestr);
        }
        if (outputDateTimeFormat == null || outputDateTimeFormat.isEmpty()) {
            outputDateTimeFormat = SOSDate.getOutputDateTimeFormat();
        }
        DateFormat formatter = new SimpleDateFormat(outputDateTimeFormat);
        formatter.setLenient(lenient);
        return formatter.format(date);
    }

    public static String getDateTimeAsString(Date date, String outputDateTimeFormat) throws Exception {
        if (outputDateTimeFormat == null || outputDateTimeFormat.isEmpty()) {
            outputDateTimeFormat = SOSDate.getOutputDateTimeFormat();
        }
        DateFormat formatter = new SimpleDateFormat(outputDateTimeFormat);
        formatter.setLenient(lenient);
        return formatter.format(date);
    }

    public static Date getDate(String text, int dateStyle, Locale locale) throws ParseException {
        DateFormat formatter = DateFormat.getDateInstance(dateStyle, locale);
        return formatter.parse(text);
    }

    public static Date getTime(String text, int timeStyle, Locale locale) throws ParseException {
        DateFormat formatter = DateFormat.getTimeInstance(timeStyle, locale);
        return formatter.parse(text);
    }

    public static Date getDateTime(String text, int dateStyle, int timeStyle, Locale locale) throws ParseException {
        DateFormat formatter = DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
        return formatter.parse(text);
    }

    public static String getDatePattern(int dateStyle, Locale locale) {
        SimpleDateFormat formatter = (SimpleDateFormat) DateFormat.getDateInstance(dateStyle, locale);
        formatter.setLenient(lenient);
        return formatter.toLocalizedPattern();
    }

    public static String getTimePattern(int timeStyle, Locale locale) {
        SimpleDateFormat formatter = (SimpleDateFormat) DateFormat.getTimeInstance(timeStyle, locale);
        formatter.setLenient(lenient);
        return formatter.toLocalizedPattern();
    }

    public static String getDateTimePattern(int dateStyle, int timeStyle, Locale locale) {
        SimpleDateFormat formatter = (SimpleDateFormat) DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
        formatter.setLenient(lenient);
        return formatter.toLocalizedPattern();
    }

    public static String getOutputDateTimeFormat() {
        return outputDateTimeFormat;
    }

    public static void setOutputDateTimeFormat(String outputDateTimeFormat) {
        SOSDate.outputDateTimeFormat = outputDateTimeFormat;
    }

    public static String getLocaleDateAsString(String datestr) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(SOSDate.dateFormat);
        sdf.setLenient(lenient);
        Date date = sdf.parse(datestr);
        DateFormat formatter = DateFormat.getDateInstance(SOSDate.dateStyle, SOSDate.locale);
        return formatter.format(date);
    }

    public static String getLocaleDateTimeAsString(String datestr) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(SOSDate.dateTimeFormat);
        sdf.setLenient(lenient);
        Date date = sdf.parse(datestr);
        DateFormat formatter = DateFormat.getDateTimeInstance(SOSDate.dateStyle, SOSDate.timeStyle, SOSDate.locale);
        return formatter.format(date);
    }

    public static String getLocaleDateAsString(Date date) throws Exception {
        DateFormat formatter = DateFormat.getDateInstance(SOSDate.dateStyle, SOSDate.locale);
        return formatter.format(date);
    }

    public static String getLocaleDateTimeAsString(Date date) throws Exception {
        DateFormat formatter = DateFormat.getDateTimeInstance(SOSDate.dateStyle, SOSDate.timeStyle, SOSDate.locale);
        return formatter.format(date);
    }

    public static String getISODateTimeAsString(GregorianCalendar date) throws Exception {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        isoFormat.setLenient(lenient);
        return isoFormat.format(date.getTime());
    }

    public static String getISODateAsString(GregorianCalendar date) throws Exception {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");
        isoFormat.setLenient(lenient);
        return isoFormat.format(date.getTime());
    }

    public static boolean isLenient() {
        return lenient;
    }

    public static void setLenient(boolean lenient) {
        SOSDate.lenient = lenient;
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

    public static void main(String[] args) {
        try {
            Date d = SOSDate.getCurrentDate();
            System.out.println(SOSDate.getWeek(d));
            System.out.println(SOSDate.getMonth(d));
            System.out.println(SOSDate.getYear(d));
            System.out.println(SOSDate.getDateAsString(new Date(), "yyyy-MM-dd HH:mm:ss.SSSZZZZ", TimeZone.getTimeZone("PST")));
            System.out.println(SOSDate.getDateAsString(new Date(), "yyyy-MM-dd HH:mm:ss.SSSZZZZ", TimeZone.getTimeZone("Europe/Berlin")));
        } catch (Exception e) {
            System.err.println("..error: " + e.toString());
        }
    }

}