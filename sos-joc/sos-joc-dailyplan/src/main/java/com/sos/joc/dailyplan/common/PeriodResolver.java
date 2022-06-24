package com.sos.joc.dailyplan.common;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.calendar.Period;
import com.sos.joc.classes.JobSchedulerDate;

public class PeriodResolver {

    private static final String DATE_FORMAT_SIMPLE = "yyyy-M-dd HH:mm:ss";
    private static final Logger LOGGER = LoggerFactory.getLogger(PeriodResolver.class);

    private DailyPlanSettings settings;
    private Map<String, Period> periods;

    public PeriodResolver(DailyPlanSettings settings) {
        super();
        this.settings = settings;
        this.periods = new HashMap<String, Period>();
    }

    public void addStartTimes(Period period, String dailyPlanDate, String timeZone) throws ParseException, SOSInvalidDataException {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        if (isDebugEnabled) {
            LOGGER.debug(String.format("  [addStartTimes][start][dailyPlanDate=%s][timeZone=%s]%s", dailyPlanDate, timeZone, SOSString.toString(
                    period)));
        }

        period = normalizePeriod(period);
        if (isDebugEnabled) {
            LOGGER.debug(String.format("  [addStartTimes][normalized][dailyPlanDate=%s][timeZone=%s]%s", dailyPlanDate, timeZone, SOSString.toString(
                    period)));
        }

        if (period.getSingleStart() != null && !period.getSingleStart().isEmpty()) {
            // Optional<Instant> scheduledFor = JobSchedulerDate.getScheduledForInUTC(dailyPlanDate + " " + period.getSingleStart(), timeZone);

            // period.setSingleStart(getTimeFromIso(scheduledFor.get()));
            // add(scheduledFor.get().getEpochSecond(), period);
            add(period.getSingleStart(), period);
        }
        addRepeat(period, dailyPlanDate, timeZone);
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
        LOGGER.debug("  Adding " + start);

        Period p = periods.get(start);
        if (p == null) {
            periods.put(start, period);
        } else {
            LOGGER.info("Overlapping period for start time: " + start);
            logPeriod(p);
            logPeriod(period);
        }
    }

    private void addRepeat(Period period, String dailyPlanDate, String timeZone) throws ParseException {
        if (!period.getRepeat().isEmpty() && !"00:00:00".equals(period.getRepeat())) {

            ZonedDateTime startUtc = JobSchedulerDate.convertDateTimeZoneToTimeZone(DATE_FORMAT_SIMPLE, timeZone, "UTC", dailyPlanDate + " " + period
                    .getBegin());
            period.setBegin(JobSchedulerDate.asTimeString(startUtc));

            ZonedDateTime endUtc = JobSchedulerDate.convertDateTimeZoneToTimeZone(DATE_FORMAT_SIMPLE, timeZone, "UTC", dailyPlanDate + " " + period
                    .getEnd());
            period.setEnd(JobSchedulerDate.asTimeString(endUtc));

            Date repeat = getDate(dailyPlanDate, period.getRepeat(), DATE_FORMAT_SIMPLE);
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(repeat);
            long offset = (calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 + calendar.get(Calendar.MINUTE) * 60 + calendar.get(Calendar.SECOND));
            while (offset > 0 && startUtc.isBefore(endUtc)) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(startUtc.toEpochSecond() * 1000);
                TimeZone timeZoneFromCalendar = TimeZone.getTimeZone(timeZone);

                DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                dateFormat.setTimeZone(timeZoneFromCalendar);
                String s = dateFormat.format(cal.getTime());

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

    public Map<Long, Period> getStartTimes(String frequencyResolverDate, String dailyPlanDate, String timeZone) throws ParseException {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        String method = "getStartTimes";
        Map<Long, Period> startTimes = new HashMap<>();
        for (Entry<String, Period> periodEntry : periods.entrySet()) {
            Date start = getDate(frequencyResolverDate, periodEntry.getKey(), DATE_FORMAT_SIMPLE);
            DayIsInPlanResult result = dayIsInPlan(start, dailyPlanDate, frequencyResolverDate, timeZone);
            if (result.isInPlan) {
                // if (dayIsInPlan(start, dailyPlanDate, frequencyResolverDate, timeZone)) {
                // Optional<Instant> scheduledFor = JobSchedulerDate.getScheduledForInUTC(dailyPlanDate + " " + periodEntry.getKey(), timeZone);
                Optional<Instant> scheduledFor = JobSchedulerDate.getScheduledForInUTC(frequencyResolverDate + " " + periodEntry.getKey(), timeZone);
                startTimes.put(scheduledFor.get().getEpochSecond() * 1000, periodEntry.getValue());

                if (isDebugEnabled) {
                    try {
                        LOGGER.debug(String.format(
                                "      [%s][added][day is in plan][UTC start=%s][dailyPlanDate=%s][FrequencyResolver date=%s][period_begin=%s][timeZone=%s][period=%s]start=%s",
                                method, SOSDate.getDateTimeAsString(result.startUTC), dailyPlanDate, frequencyResolverDate, settings.getPeriodBegin(),
                                timeZone, periodEntry.getKey(), SOSDate.getDateTimeAsString(start)));
                    } catch (SOSInvalidDataException e) {

                    }
                }
            } else {
                if (isDebugEnabled) {
                    try {
                        LOGGER.debug(String.format(
                                "      [%s][skip][day is not in plan][UTC start=%s][dailyPlanDate=%s][FrequencyResolver date=%s][period_begin=%s][timeZone=%s][period=%s]start=%s",
                                method, SOSDate.getDateTimeAsString(result.startUTC), dailyPlanDate, frequencyResolverDate, settings.getPeriodBegin(),
                                timeZone, periodEntry.getKey(), SOSDate.getDateTimeAsString(start)));
                    } catch (SOSInvalidDataException e) {

                    }
                }
            }
        }
        return startTimes;
    }

    private DayIsInPlanResult dayIsInPlan(Date start, String dailyPlanDate, String frequencyResolverDate, String timeZone) throws ParseException {
        String timeZoneDailyplan = settings.getTimeZone();
        String periodBegin = settings.getPeriodBegin();
        String dateInString = String.format("%s %s", dailyPlanDate, periodBegin);

        Instant instant = JobSchedulerDate.getScheduledForInUTC(dateInString, timeZoneDailyplan).get();
        Date dailyPlanStartPeriod = Date.from(instant);

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(dailyPlanStartPeriod);
        calendar.add(java.util.Calendar.HOUR, 24);
        Date dailyPlanEndPeriod = calendar.getTime();

        SimpleDateFormat sdfUtc = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdfUtc.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
        start = sdf.parse(sdfUtc.format(start));

        Date now = JobSchedulerDate.nowInUtc();
        if (LOGGER.isDebugEnabled()) {
            try {
                LOGGER.debug(String.format(
                        "  [dayIsInPlan][check][UTC start=%s][dailyPlanDate=%s][FrequencyResolver date=%s][period_begin=%s][UTC now=%s, dailyPlanStartPeriod=%s, dailyPlanEndPeriod=%s]",
                        SOSDate.getDateTimeAsString(start), dailyPlanDate, frequencyResolverDate, periodBegin, SOSDate.getDateTimeAsString(now),
                        SOSDate.getDateTimeAsString(dailyPlanStartPeriod), SOSDate.getDateTimeAsString(dailyPlanEndPeriod)));
            } catch (SOSInvalidDataException e) {

            }
        }

        // return (start.after(now) && start.after(dailyPlanStartPeriod) || start.equals(dailyPlanStartPeriod)) && (start.before(dailyPlanEndPeriod));
        // return start.after(now) && start.before(dailyPlanEndPeriod) && start.getTime() >= dailyPlanStartPeriod.getTime();
        boolean isInPlan = start.after(now) && start.before(dailyPlanEndPeriod) && start.getTime() >= dailyPlanStartPeriod.getTime();
        return new DayIsInPlanResult(start, isInPlan);
    }

    private class DayIsInPlanResult {

        private final Date startUTC;
        private final boolean isInPlan;

        private DayIsInPlanResult(Date startUTC, boolean isInPlan) {
            this.startUTC = startUTC;
            this.isInPlan = isInPlan;
        }
    }
}
