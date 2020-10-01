package com.sos.joc.classes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.exceptions.JobSchedulerBadRequestException;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;

public class JobSchedulerDate {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerDate.class);

    
    public static Date nowInUtc() {
        String now = getNowInISO();
        Instant instant = getScheduledForInUTC(now, TimeZone.getDefault().getID()).get();
        return Date.from(instant);
    }

    public static String getNowInISO() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
        df.setTimeZone(tz);
        return df.format(new Date());
    }

    public static Date getDateFromISO8601String(String dateString) {
        Instant fromString = getInstantFromISO8601String(dateString);
        if (fromString != null) {
            try {
                return Date.from(fromString);
            } catch (Exception e) {
                LOGGER.warn(dateString, e);
            }
        }
        return null;
    }

    public static Instant getInstantFromISO8601String(String dateString) {
        Instant fromString = null;
        if (dateString != null && !dateString.isEmpty()) {
            try {
                if (dateString.trim().matches("^\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{1,3})?Z?$")) {
                    // only time will be extended with today to complete timestamp
                    dateString = Instant.now().toString().replaceFirst("^(\\d{4}-\\d{2}-\\d{2}T).*", "$1" + dateString.trim() + "Z").replaceFirst(
                            "ZZ+$", "Z");
                }
                dateString = dateString.trim().replaceFirst("^(\\d{4}-\\d{2}-\\d{2}) ", "$1T");
                // dateString must have 'Z' as offset
                fromString = Instant.parse(dateString);
                // better: dateString can have arbitrary offsets
                // fromString = Instant.ofEpochMilli(DatatypeConverter.parseDateTime(dateString).getTimeInMillis());
                // JobScheduler responses max or min time but means 'never'
                // if (fromString == null || fromString.getEpochSecond() <= 0 || fromString.getEpochSecond() >= Instant.MAX.getEpochSecond()) {
                // highest date in C++ (JobScheduler1) -> 2038-01-19 <=> (2^31)-1
                if (fromString == null || fromString.getEpochSecond() <= 0 || fromString.getEpochSecond() >= 2147483647L) {
                    fromString = null;
                }
            } catch (DateTimeParseException e) {
                LOGGER.warn(dateString, e);
                fromString = null;
            }
        }
        return fromString;
    }

    public static Date getDateFromEventId(Long eventId) {
        if (eventId == null) {
            return null;
        }
        Instant fromEpochMilli = Instant.ofEpochMilli(eventId / 1000);
        return Date.from(fromEpochMilli);
    }

    public static Date getDateTo(String date, String timeZone) throws JobSchedulerInvalidResponseDataException {
        return getDate(date, true, timeZone);
    }

    public static Date getDateFrom(String date, String timeZone) throws JobSchedulerInvalidResponseDataException {
        return getDate(date, false, timeZone);
    }

    public static Date getDate(String date, boolean dateTo, String timeZone) throws JobSchedulerInvalidResponseDataException {
        if (date == null || date.isEmpty()) {
            return null;
        }
        try {
            return Date.from(getInstantFromDateStr(date, dateTo, timeZone));
        } catch (JobSchedulerInvalidResponseDataException e) {
            throw e;
        } catch (Exception e) {
            throw new JobSchedulerInvalidResponseDataException(e);
        }
    }

    public static Optional<Instant> getScheduledForInUTC(String scheduledFor, String userTimezone) throws JobSchedulerBadRequestException {
        if (scheduledFor == null || scheduledFor.isEmpty() || "now".equals(scheduledFor.trim().toLowerCase())) {
            return Optional.empty();
        }
        scheduledFor = scheduledFor.trim();
        if (userTimezone == null) {
            userTimezone = "UTC";
        }
        if (scheduledFor.toLowerCase().contains("now")) {
            return getScheduledForWithNowInUTC(scheduledFor.toLowerCase(), userTimezone);
        }
        return getScheduledForWithoutNowInUTC(scheduledFor, userTimezone);
    }

    private static Optional<Instant> getScheduledForWithNowInUTC(String scheduledFor, String userTimezone) throws JobSchedulerBadRequestException {
        if (!scheduledFor.matches("now|now\\s*\\+\\s*(\\d+|\\d{2}:\\d{2}(:\\d{2})?)")) {
            throw new JobSchedulerBadRequestException(String.format(
                    "formats 'now', 'now + HH:mm:[ss]', 'now + SECONDS' or 'YYYY-MM-DD[T]HH:mm:[ss]' expected for \"scheduledFor\": %1$s",
                    scheduledFor));
        }
        String[] nowPlusParts = scheduledFor.replaceFirst("^now(\\s*\\+\\s*)?", "").split(":");
        LocalDateTime now = LocalDateTime.now();
        now = now.withNano(0);
        if (nowPlusParts.length == 1) { // + SECONDS
            if (!nowPlusParts[0].isEmpty()) {
                Long plusSeconds = Long.valueOf(nowPlusParts[0]);
                if (plusSeconds == 0L) {
                    return Optional.empty();
                }
                now = now.plusSeconds(plusSeconds);
            }
        } else { // + HH:mm:[ss]
            now = now.plusHours(Long.valueOf(nowPlusParts[0]));
            now = now.plusMinutes(Long.valueOf(nowPlusParts[1]));
            if (nowPlusParts.length == 3 && !nowPlusParts[2].isEmpty()) {
                now = now.plusSeconds(Long.valueOf(nowPlusParts[2]));
            }
        }
        return Optional.of(ZonedDateTime.of(now, ZoneId.of(userTimezone)).withZoneSameInstant(ZoneId.of("UTC")).toInstant());
    }

    private static Optional<Instant> getScheduledForWithoutNowInUTC(String scheduledFor, String userTimezone) throws JobSchedulerBadRequestException {
        if (scheduledFor.matches("\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}")) {
            scheduledFor += ":00";
        }
        if (!scheduledFor.matches("\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2}")) {
            throw new JobSchedulerBadRequestException(String.format(
                    "formats 'now', 'now + HH:mm:[ss]', 'now + SECONDS' or 'YYYY-MM-DD[T]HH:mm:[ss]' expected for \"scheduledFor\": %1$s",
                    scheduledFor));
        }
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return Optional.of(ZonedDateTime.of(LocalDateTime.parse(scheduledFor.replace(' ', 'T'), formatter), ZoneId.of(userTimezone))
                .withZoneSameInstant(ZoneId.of("UTC")).toInstant());
    }

    private static Instant getInstantFromDateStr(String dateStr, boolean dateTo, String timeZone) throws JobSchedulerInvalidResponseDataException {
        Pattern offsetPattern = Pattern.compile(
                "(\\d{2,4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}(?:\\.\\d+)?|(?:\\s*[+-]?\\d+\\s*[smhdwMy])+)([+-][0-9:]+|Z)?$");
        Pattern dateTimePattern = Pattern.compile("(?:([+-]?\\d+)\\s*([smhdwMy])\\s*)");
        Matcher m = offsetPattern.matcher(dateStr);
        TimeZone timeZ = null;
        boolean dateTimeIsRelative = false;
        Map<String, Integer> relativeDateTimes = new HashMap<String, Integer>();
        relativeDateTimes.put("y", null);
        relativeDateTimes.put("M", null);
        relativeDateTimes.put("w", null);
        relativeDateTimes.put("d", null);
        relativeDateTimes.put("h", null);
        relativeDateTimes.put("m", null);
        relativeDateTimes.put("s", null);

        try {
            if (timeZone != null && !timeZone.isEmpty()) {
                timeZ = TimeZone.getTimeZone(timeZone);
            }
            if (m.find()) {
                if (timeZ == null) {
                    timeZ = (m.group(2) != null) ? TimeZone.getTimeZone("GMT" + m.group(2)) : TimeZone.getTimeZone(ZoneOffset.UTC);
                }
                dateStr = m.group(1);
            } else if (timeZ == null) {
                timeZ = TimeZone.getTimeZone(ZoneOffset.UTC);
            }
            m = dateTimePattern.matcher(dateStr.replaceAll("([smhdwMy])", "$1 "));
            while (m.find()) {
                dateTimeIsRelative = true;
                Integer number = Integer.valueOf(m.group(1));
                relativeDateTimes.put(m.group(2), number);
            }
            if (!dateTimeIsRelative) {
                Instant instant = Instant.parse(dateStr + "Z");
                int offset = timeZ.getOffset(instant.toEpochMilli());
                return instant.plusMillis(-1 * offset);

            } else {
                Calendar calendar = Calendar.getInstance(timeZ);
                calendar.setTime(Date.from(Instant.now()));
                calendar.setTimeZone(timeZ);
                if (Pattern.compile("[dwMy]").matcher(dateStr).find()) {
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                }
                if (relativeDateTimes.get("y") != null) {
                    if (dateTo) {
                        calendar.add(Calendar.YEAR, 1 + relativeDateTimes.get("y"));
                        calendar.add(Calendar.DAY_OF_YEAR, 1 - calendar.get(Calendar.DAY_OF_YEAR) - 1);
                    } else {
                        calendar.add(Calendar.YEAR, relativeDateTimes.get("y"));
                        calendar.add(Calendar.DAY_OF_YEAR, 1 - calendar.get(Calendar.DAY_OF_YEAR));
                    }
                }
                if (relativeDateTimes.get("M") != null) {
                    if (dateTo) {
                        calendar.add(Calendar.MONTH, 1 + relativeDateTimes.get("M"));
                        calendar.add(Calendar.DATE, 1 - calendar.get(Calendar.DATE) - 1);
                    } else {
                        calendar.add(Calendar.MONTH, relativeDateTimes.get("M"));
                        calendar.add(Calendar.DATE, 1 - calendar.get(Calendar.DATE));
                    }
                }
                if (relativeDateTimes.get("w") != null) {
                    if (dateTo) {
                        calendar.add(Calendar.WEEK_OF_YEAR, 1 + relativeDateTimes.get("w"));
                        calendar.add(Calendar.DATE, Calendar.MONDAY - calendar.get(Calendar.DAY_OF_WEEK) - 1);
                    } else {
                        calendar.add(Calendar.WEEK_OF_YEAR, relativeDateTimes.get("w"));
                        calendar.add(Calendar.DATE, Calendar.MONDAY - calendar.get(Calendar.DAY_OF_WEEK));
                    }
                }
                if (relativeDateTimes.get("d") != null) {
                    if (dateTo) {
                        calendar.add(Calendar.DATE, 1 + relativeDateTimes.get("d") - 1);
                    } else {
                        calendar.add(Calendar.DATE, relativeDateTimes.get("d"));
                    }
                }
                if (dateTo) {
                    calendar.add(Calendar.DATE, 1);
                }
                if (relativeDateTimes.get("h") != null) {
                    calendar.add(Calendar.HOUR_OF_DAY, relativeDateTimes.get("h"));
                }
                if (relativeDateTimes.get("m") != null) {
                    calendar.add(Calendar.MINUTE, relativeDateTimes.get("m"));
                }
                if (relativeDateTimes.get("s") != null) {
                    calendar.add(Calendar.SECOND, relativeDateTimes.get("s"));
                }
                return calendar.toInstant();
            }
        } catch (Exception e) {
            throw new JobSchedulerInvalidResponseDataException(e);
        }
    }

    public static ZonedDateTime convertDateTimeZoneToTimeZone(String dateFormat, String fromTimeZone, String toTimeZone, String fromDateTime) {

        java.time.format.DateTimeFormatter dateTimeFormatter = java.time.format.DateTimeFormatter.ofPattern(dateFormat);

        LocalDateTime dateTime = LocalDateTime.parse(fromDateTime, dateTimeFormatter);
        ZonedDateTime toDateTime = ZonedDateTime.now(ZoneId.of(fromTimeZone)).with(dateTime).withZoneSameInstant(ZoneId.of(toTimeZone));
        return toDateTime;

    }

    public static ZonedDateTime convertTimeTimeZoneToTimeZone(String timeFormat, String fromTimeZone, String toTimeZone, String fromDateTime) {

        java.time.format.DateTimeFormatter dateTimeFormatter = java.time.format.DateTimeFormatter.ofPattern(timeFormat);

        LocalTime dateTime = LocalTime.parse(fromDateTime, dateTimeFormatter);
        ZonedDateTime toDateTime = ZonedDateTime.now(ZoneId.of(fromTimeZone)).with(dateTime).withZoneSameInstant(ZoneId.of(toTimeZone));
        return toDateTime;

    }

    public static String asTimeString(ZonedDateTime time) {
        java.time.format.DateTimeFormatter dateTimeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");
        return time.format(dateTimeFormatter);
    }
}
