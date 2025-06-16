package com.sos.joc.dailyplan.common;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
    private Set<String> frequencyResolverDates;

    public PeriodResolver(DailyPlanSettings settings) {
        super();
        this.settings = settings;
        this.periods = new LinkedHashMap<String, Period>();
        this.frequencyResolverDates = new HashSet<>();
    }

    public void addStartTimes(Period period, String dailyPlanDate, String timeZone) throws ParseException, SOSInvalidDataException {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        if (isDebugEnabled) {
            LOGGER.debug(String.format("  [addStartTimes][dailyPlanDate=%s][timeZone=%s]%s", dailyPlanDate, timeZone, SOSString.toString(period)));
        }

        period = normalizePeriod(period);
        if (isDebugEnabled) {
            LOGGER.debug(String.format("  [addStartTimes][dailyPlanDate=%s][timeZone=%s][normalized]%s", dailyPlanDate, timeZone, SOSString.toString(
                    period)));
        }

        if (period.getSingleStart() != null && !period.getSingleStart().isEmpty()) {
            add(period.getSingleStart(), period);
        }
        // TODO why not else ???
        addRepeat(period, dailyPlanDate, timeZone);
    }

    public Map<Long, Period> getStartTimes(String frequencyResolverDate, String dailyPlanDate, String timeZone) throws ParseException {
        return getStartTimes(frequencyResolverDate, dailyPlanDate, timeZone, false);
    }

    
    public Map<Long, Period> getStartTimes(String frequencyResolverDate, String dailyPlanDate, String timeZone, boolean includeLate) throws ParseException {
        Map<Long, Period> startTimes = new LinkedHashMap<>();

        if (frequencyResolverDates.contains(frequencyResolverDate)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format(
                        "  [getStartTimes][dailyPlanDate=%s][frequencyResolverDate=%s][skip]dailyPlanDate or the current frequencyResolverDate have already been processed",
                        dailyPlanDate, frequencyResolverDate));
            }
            return startTimes;
        }

        for (Entry<String, Period> periodEntry : periods.entrySet()) {
            DailyPlanPeriodResult result = isInDailyPlanPeriod(frequencyResolverDate + " " + periodEntry.getKey(), dailyPlanDate,
                    frequencyResolverDate, timeZone);
            if (result.isInDailyPlanPeriod) {
                // Optional<Instant> scheduledFor = JobSchedulerDate.getScheduledForInUTC(frequencyResolverDate + " " + periodEntry.getKey(), timeZone);
                // startTimes.put(scheduledFor.get().getEpochSecond() * 1000, periodEntry.getValue());
                // if (LOGGER.isDebugEnabled()) {
                // LOGGER.debug(String.format(" [getStartTimes][dailyPlanDate=%s][frequencyResolverDate=%s]scheduledFor=%s", dailyPlanDate,
                // frequencyResolverDate, SOSDate.tryGetDateTimeAsString(scheduledFor.get())));
                // }
                startTimes.put(result.startUTC.getTime(), periodEntry.getValue());
            } else if(!result.isInDailyPlanPeriod && includeLate) {
                // else for recreation of late orders
                startTimes.put(result.startUTC.getTime(), periodEntry.getValue());
            }
        }
        return startTimes;
    }

    private DailyPlanPeriodResult isInDailyPlanPeriod(String startDateTime, String dailyPlanDate, String frequencyResolverDate, String timeZone)
            throws ParseException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("  [isInDailyPlanPeriod][dailyPlanDate=%s][frequencyResolverDate=%s]startDateTime=%s(%s)", dailyPlanDate,
                    frequencyResolverDate, startDateTime, timeZone));
        }

        // DailyPlan DAY Period: START/END
        String periodDateTime = dailyPlanDate + " " + settings.getPeriodBegin();
        Date periodStartUTC = Date.from(JobSchedulerDate.getScheduledForInUTC(periodDateTime, timeZone).get());
        Date periodEndUTC = SOSDate.add(periodStartUTC, 1, ChronoUnit.DAYS);
        // Start DateTime
        Date startUTC = SOSDate.tryGetDateTime(startDateTime, TimeZone.getTimeZone(timeZone));
        // NOW
        Date nowUTC = JobSchedulerDate.nowInUtc();

        // Check
        boolean isInDailyPlanPeriod = startUTC.after(nowUTC) && startUTC.getTime() >= periodStartUTC.getTime() && startUTC.before(periodEndUTC);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("    [isInDailyPlanPeriod=%s][UTC][start=%s][DailyPlanPeriod start=%s, end=%s][now=%s]", isInDailyPlanPeriod,
                    SOSDate.tryGetDateTimeAsString(startUTC), SOSDate.tryGetDateTimeAsString(periodStartUTC), SOSDate.tryGetDateTimeAsString(
                            periodEndUTC), SOSDate.tryGetDateTimeAsString(nowUTC)));
        }

        frequencyResolverDates.add(frequencyResolverDate);
        if (isInDailyPlanPeriod) {
            frequencyResolverDates.add(getNextDateAsString(frequencyResolverDate));
        } else {
            // only for period_begin <> 00:00:00 add 1 day to the start
            if (!settings.isPeriodBeginMidnight() && dailyPlanDate.equals(frequencyResolverDate)) {
                frequencyResolverDates.add(getNextDateAsString(frequencyResolverDate));

                startUTC = SOSDate.add(startUTC, 1, ChronoUnit.DAYS);
                // Check
                isInDailyPlanPeriod = startUTC.after(nowUTC) && startUTC.getTime() >= periodStartUTC.getTime() && startUTC.before(periodEndUTC);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format(
                            "    [isInDailyPlanPeriod=%s][recheck due to period_begin=%s][UTC][start=%s][DailyPlanPeriod start=%s, end=%s][now=%s]",
                            isInDailyPlanPeriod, settings.getPeriodBegin(), SOSDate.tryGetDateTimeAsString(startUTC), SOSDate.tryGetDateTimeAsString(
                                    periodStartUTC), SOSDate.tryGetDateTimeAsString(periodEndUTC), SOSDate.tryGetDateTimeAsString(nowUTC)));
                }
            }
        }
        return new DailyPlanPeriodResult(startUTC, isInDailyPlanPeriod);
    }

    private String getNextDateAsString(String date) {
        try {
            return SOSDate.getDateAsString(SOSDate.add(SOSDate.getDate(date), 1, ChronoUnit.DAYS));
        } catch (SOSInvalidDataException e) {
            LOGGER.error(String.format("[getNextDateAsString][%s]%s", date, e.toString()), e);
            return "";
        }
    }

    private Date getDate(String day, String time, String format) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        String dateInString = String.format("%s %s", day, time);
        return dateFormat.parse(dateInString);
    }

    private void add(String start, Period period) {
        LOGGER.trace("  Adding " + start);

        Period p = periods.get(start);
        if (p == null) {
            periods.put(start, period);
        } else {
            LOGGER.info(String.format("[add][overlapping period for start][start=%s][added=%s]current=%s", start, SOSString.toString(p), SOSString
                    .toString(period)));
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

    private class DailyPlanPeriodResult {

        private final Date startUTC;
        private final boolean isInDailyPlanPeriod;

        private DailyPlanPeriodResult(Date startUTC, boolean isInDailyPlanPeriod) {
            this.startUTC = startUTC;
            this.isInDailyPlanPeriod = isInDailyPlanPeriod;
        }
    }
}
