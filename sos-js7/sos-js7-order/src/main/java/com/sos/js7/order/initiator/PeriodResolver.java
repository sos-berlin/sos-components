package com.sos.js7.order.initiator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.inventory.model.calendar.Period;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.js7.order.initiator.classes.OrderInitiatorGlobals;

public class PeriodResolver {

    private static final String DATE_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String DATE_FORMAT_TIME ="HH:mm:ss";
    private static final String DATE_FORMAT_SIMPLE = "yyyy-M-dd HH:mm:ss";
    private static final Logger LOGGER = LoggerFactory.getLogger(PeriodResolver.class);
    private Map<Long, Period> listOfStartTimes;
    private Map<String, Period> listOfPeriods;

    public PeriodResolver() {
        super();
        listOfStartTimes = new HashMap<Long, Period>();
        listOfPeriods = new HashMap<String, Period>();
    }

    private Date getDate(String day, String time, String format) throws ParseException {

        SimpleDateFormat dateFormat = new SimpleDateFormat(format);

        String dateInString = String.format("%s %s", day, time);
        return dateFormat.parse(dateInString);
    }

    private void logPeriod(Period p) {
        LOGGER.info(p.getBegin() + " - " + p.getEnd());
        LOGGER.info("Single Start: " + p.getSingleStart());
        LOGGER.info("Repeat: " + p.getRepeat());
    }

    private void add(String start, Period period) {
        LOGGER.debug("Adding " + start);
        Period p = listOfPeriods.get(start);
        if (p == null) {
            listOfPeriods.put(start, period);
        } else {
            LOGGER.info("Overlapping period for start time: " + start);
            logPeriod(p);
            logPeriod(period);
        }
    }

    private void addRepeat(Period period, String dailyPlanDate, String timeZone) throws ParseException {
        if (!period.getRepeat().isEmpty() && !"00:00:00".equals(period.getRepeat())) {

            ZonedDateTime startUtc = JobSchedulerDate.convertDateTimeZoneToTimeZone(DATE_FORMAT_SIMPLE, timeZone, "UTC", dailyPlanDate + " " + period.getBegin());
            period.setBegin(JobSchedulerDate.asTimeString(startUtc));

            ZonedDateTime endUtc = JobSchedulerDate.convertDateTimeZoneToTimeZone(DATE_FORMAT_SIMPLE, timeZone, "UTC", dailyPlanDate + " " + period.getEnd());
            period.setEnd(JobSchedulerDate.asTimeString(endUtc));

            Date repeat = getDate(dailyPlanDate, period.getRepeat(), DATE_FORMAT_SIMPLE);
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(repeat);
            long offset = (calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 + calendar.get(Calendar.MINUTE) * 60 + calendar.get(Calendar.SECOND));
            while (offset > 0 && startUtc.isBefore(endUtc)) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                String s = startUtc.format(formatter);
                add(s, period);
                startUtc = startUtc.plusSeconds(offset);
            }
        }
    }

    private void check(String s, int max) throws SOSInvalidDataException {
        int check = Integer.parseInt(s);
        if (check > max && max > 0) {
            throw new SOSInvalidDataException(s + " increases maximum value: " + max);
        }
    }

    private String normalizeTimeValue(String s) throws SOSInvalidDataException {
        String res = s;
        boolean left = false;
        String format;

        if (res != null && res.startsWith("+")) {
            left = true;
            res = res.substring(1);
        }

        if (res != null && !res.isEmpty()) {

            String[] time = res.split(":");

            if (time.length == 1) {
                check(time[0], 59);
                if (left) {
                    format = "00:00:%s";
                } else {
                    format = "%s:00:00";
                }
                res = String.format(format, res);
            }
            if (time.length == 2) {

                if (left) {
                    format = "00:%s";
                } else {
                    format = "%s:00";
                }
                check(time[0], 59);
                check(time[1], 59);
                res = String.format(format, res);
            }
            if (time.length == 3) {
                check(time[0], 24);
                check(time[1], 59);
                check(time[2], 59);
            }
        }
        return res;
    }

    private Period normalizePeriod(Period p) throws SOSInvalidDataException {

        if (p.getBegin() == null || p.getBegin().isEmpty()) {
            p.setBegin("00:00:00");
        }
        if (p.getEnd() == null || p.getEnd().isEmpty()) {
            p.setEnd("24:00:00");
        }

        p.setBegin(normalizeTimeValue(p.getBegin()));
        p.setEnd(normalizeTimeValue(p.getEnd()));
        if (p.getRepeat() == null || p.getRepeat().isEmpty()) {
            p.setRepeat("00:00:00");
        } else {
            p.setRepeat(normalizeTimeValue("+" + p.getRepeat()));
        }
        if (p.getSingleStart() != null && !p.getSingleStart().isEmpty()) {
            p.setSingleStart(normalizeTimeValue(p.getSingleStart()));
        }

        return p;
    }

    private String getTimeFromIso(Instant instant) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_FORMAT_ISO);
        LocalDateTime dateTime = LocalDateTime.parse(instant.toString(), dtf);
        dtf = DateTimeFormatter.ofPattern(DATE_FORMAT_TIME);
        return dtf.format(dateTime);
    }

    public void addStartTimes(Period period, String dailyPlanDate, String timeZone) throws ParseException, SOSInvalidDataException {
        period = normalizePeriod(period);
        if (period.getSingleStart() != null && !period.getSingleStart().isEmpty()) {
            Optional<Instant> scheduledFor = JobSchedulerDate.getScheduledForInUTC(dailyPlanDate + " " + period.getSingleStart(), timeZone);
            period.setSingleStart(getTimeFromIso(scheduledFor.get()));
            add(period.getSingleStart(), period);
        }
        addRepeat(period, dailyPlanDate, timeZone);
    }

    public Map<Long, Period> getStartTimes() {
        return listOfStartTimes;
    }

    private boolean dayIsInPlan(Date start, String dailyPlanDate) throws ParseException {
        String timeZone = OrderInitiatorGlobals.orderInitiatorSettings.getTimeZone();
        String periodBegin = OrderInitiatorGlobals.orderInitiatorSettings.getPeriodBegin();
        String dateInString = String.format("%s %s", dailyPlanDate, periodBegin);

        Instant instant = JobSchedulerDate.getScheduledForInUTC(dateInString, timeZone).get();
        Date dailyPlanStartPeriod = Date.from(instant);

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(dailyPlanStartPeriod);
        calendar.add(java.util.Calendar.HOUR, 24);
        Date dailyPlanEndPeriod = calendar.getTime();

        return (start.after(JobSchedulerDate.nowInUtc()) && start.after(dailyPlanStartPeriod) || start.equals(dailyPlanStartPeriod)) && (start.before(
                dailyPlanEndPeriod));

    }

    public Map<Long, Period> getStartTimes(String d, String dailyPlanDate) throws ParseException {
        listOfStartTimes = new HashMap<Long, Period>();
        for (Entry<String, Period> periodEntry : listOfPeriods.entrySet()) {
            Date start = getDate(d, periodEntry.getKey(), DATE_FORMAT_SIMPLE);
            if (dayIsInPlan(start, dailyPlanDate)) {
                listOfStartTimes.put(start.getTime(), periodEntry.getValue());
            }
        }
        return listOfStartTimes;
    }
}
