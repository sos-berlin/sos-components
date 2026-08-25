package com.sos.joc.dailyplan.common;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.calendar.Period;

public class PeriodResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(PeriodResolver.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter START_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private DailyPlanSettings settings;
    private Map<String, Period> periods;
    private Set<String> frequencyResolverDates;

    public PeriodResolver(DailyPlanSettings settings) {
        super();
        this.settings = settings;
        this.periods = new LinkedHashMap<String, Period>();
        this.frequencyResolverDates = new HashSet<>();
    }

    public void addStartTimes(Period period, String dailyPlanDate, String scheduleTimeZone) throws ParseException, SOSInvalidDataException {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[addStartTimes][dailyPlanDate=%s][scheduleTimeZone=%s]%s", dailyPlanDate, scheduleTimeZone, SOSString
                    .toString(period, true)));
        }

        period = normalizePeriod(period);
        if (isDebugEnabled) {
            LOGGER.debug(String.format("  [addStartTimes][dailyPlanDate=%s][scheduleTimeZone=%s][normalized]%s", dailyPlanDate, scheduleTimeZone,
                    SOSString.toString(period, true)));
        }

        if (period.getSingleStart() != null && !period.getSingleStart().isEmpty()) {
            add(period.getSingleStart(), period);
        }
        // TODO why not else ???
        addRepeat(period, dailyPlanDate, scheduleTimeZone);
    }

    public Map<Long, Period> getStartTimes(String frequencyResolverDate, String dailyPlanDate, String scheduleTimeZone, boolean includeLate)
            throws ParseException {
        Map<Long, Period> startTimes = new TreeMap<>();

        if (frequencyResolverDates.contains(frequencyResolverDate)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format(
                        "[getStartTimes][dailyPlanDate=%s][frequencyResolverDate=%s][skip]dailyPlanDate or the current frequencyResolverDate have already been processed",
                        dailyPlanDate, frequencyResolverDate));
            }
            return startTimes;
        }

        ZoneId periodZone = ZoneId.of(settings.getTimeZone());
        ZonedDateTime periodStartZoned = ZonedDateTime.of(LocalDate.parse(dailyPlanDate), LocalTime.parse(settings.getPeriodBegin()), periodZone);
        ZonedDateTime periodEndZoned = periodStartZoned.plusDays(1);
        Instant periodStartUTC = periodStartZoned.toInstant();
        Instant periodEndUTC = periodEndZoned.toInstant();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format(
                    "[getStartTimes][dailyPlanDate=%s][frequencyResolverDate=%s][scheduleTimeZone=%s][includeLate=%s]period_begin=%s(%s) DailyPlanPeriod start=%s, end=%s",
                    dailyPlanDate, frequencyResolverDate, scheduleTimeZone, includeLate, settings.getPeriodBegin(), settings.getTimeZone(), format(
                            periodStartZoned, periodStartUTC), format(periodEndZoned, periodEndUTC)));
        }

        ZoneId scheduleZone = ZoneId.of(scheduleTimeZone);

        for (Entry<String, Period> periodEntry : periods.entrySet()) {
            ZonedDateTime startZoned = LocalDateTime.parse(frequencyResolverDate + " " + periodEntry.getKey(), DATE_TIME_FORMATTER).atZone(
                    scheduleZone);

            DailyPlanPeriodResult result = isInDailyPlanPeriod(startZoned, dailyPlanDate, frequencyResolverDate, periodStartUTC, periodEndUTC);
            if (result.isInDailyPlanPeriod) {
                startTimes.put(result.startEpochMilli, periodEntry.getValue());
            } else if (!result.isInDailyPlanPeriod && includeLate) {
                // else for recreation of late orders
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("    [getStartTimes][isInDailyPlanPeriod=false][includeLate=true][%s]start=%s", result.canBeUsedAsLate
                            ? "add" : "skip][not late due to start +1 day", format(result.startEpochMilli)));
                }
                if (result.canBeUsedAsLate) {
                    startTimes.put(result.startEpochMilli, periodEntry.getValue());
                }
            }
        }

        // see PeriodResolverRepeatBeginEndHandler comments

        if (LOGGER.isDebugEnabled()) {
            startTimes.entrySet().stream().forEach(e -> {
                Instant i = Instant.ofEpochMilli(e.getKey());
                LocalDateTime lu = LocalDateTime.ofInstant(i, ZoneOffset.UTC);
                LocalDateTime lt = LocalDateTime.ofInstant(i, ZoneId.of(scheduleTimeZone));
                LOGGER.debug(String.format("[getStartTimes][dailyPlanDate=%s][result]%s=%s %s", dailyPlanDate, format(lt, scheduleTimeZone), format(
                        lu, "UTC"), SOSString.toString(e.getValue(), true)));
            });
        }
        return startTimes;
    }

    private DailyPlanPeriodResult isInDailyPlanPeriod(ZonedDateTime startZoned, String dailyPlanDate, String frequencyResolverDate,
            Instant periodStartUTC, Instant periodEndUTC) throws ParseException {

        Instant startUTC = startZoned.toInstant();
        // Using System.currentTimeMillis() for test compatibility (Instant.now() is not mockable in our test setup).
        Instant nowUTC = Instant.ofEpochMilli(System.currentTimeMillis());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format(
                    "  [isInDailyPlanPeriod][dailyPlanDate=%s][start=%s][frequencyResolverDate=%s][period_begin=%s(%s) DailyPlanPeriod start=%s, end=%s]now=%s",
                    dailyPlanDate, format(startZoned, startUTC), frequencyResolverDate, settings.getPeriodBegin(), settings.getTimeZone(), format(
                            periodStartUTC), format(periodEndUTC), format(nowUTC)));
        }

        // Check
        boolean isInDailyPlanPeriod = startUTC.isAfter(nowUTC) && !startUTC.isBefore(periodStartUTC) && startUTC.isBefore(periodEndUTC);
        boolean canBeUsedAsLate = true;
        if (LOGGER.isDebugEnabled()) {
            String msg = "";
            if (!isInDailyPlanPeriod) {
                if (!startUTC.isAfter(nowUTC)) {
                    msg = " <= now(" + format(nowUTC) + ")";
                } else if (!(!startUTC.isBefore(periodStartUTC))) {
                    msg = " < DailyPlanPeriod start(" + format(periodStartUTC) + ")";
                } else if (!(startUTC.isBefore(periodEndUTC))) {
                    msg = " > DailyPlanPeriod end(" + format(periodEndUTC) + ")";
                }
            }
            LOGGER.debug(String.format("      [isInDailyPlanPeriod=%s][%s]start=%s%s", isInDailyPlanPeriod, isInDailyPlanPeriod ? "add" : "skip",
                    format(startZoned, startUTC), msg));
        }

        frequencyResolverDates.add(frequencyResolverDate);
        if (isInDailyPlanPeriod) {
            frequencyResolverDates.add(getNextDateAsString(frequencyResolverDate));
        } else {
            // only for period_begin <> 00:00:00 add 1 day to the start
            if (!settings.isPeriodBeginMidnight() && dailyPlanDate.equals(frequencyResolverDate)) {
                frequencyResolverDates.add(getNextDateAsString(frequencyResolverDate));

                startZoned = startZoned.plusDays(1);
                startUTC = startZoned.toInstant();

                // Check
                isInDailyPlanPeriod = startUTC.isAfter(nowUTC) && !startUTC.isBefore(periodStartUTC) && startUTC.isBefore(periodEndUTC);
                // the next day is not a late entry
                canBeUsedAsLate = false;

                if (LOGGER.isDebugEnabled()) {
                    String msg = "";
                    if (!isInDailyPlanPeriod) {
                        if (!startUTC.isAfter(nowUTC)) {
                            msg = " <= now(" + format(nowUTC) + ")";
                        } else if (!(!startUTC.isBefore(periodStartUTC))) {
                            msg = " < DailyPlanPeriod start(" + format(periodStartUTC) + ")";
                        } else if (!(startUTC.isBefore(periodEndUTC))) {
                            msg = " > DailyPlanPeriod end(" + format(periodEndUTC) + ")";
                        }
                    }
                    LOGGER.debug(String.format(
                            "      [isInDailyPlanPeriod=%s][%s][start=%s%s]start time redefined (+1 day) and rechecked because period_begin=%s(%s %s)",
                            isInDailyPlanPeriod, isInDailyPlanPeriod ? "add" : "skip", format(startZoned, startUTC), msg, format(periodStartUTC),
                            settings.getPeriodBegin(), settings.getTimeZone()));
                }
            }
        }
        return new DailyPlanPeriodResult(startUTC.toEpochMilli(), isInDailyPlanPeriod, canBeUsedAsLate);
    }

    private String getNextDateAsString(String date) {
        try {
            return SOSDate.getDateAsString(SOSDate.add(SOSDate.getDate(date), 1, ChronoUnit.DAYS));
        } catch (SOSInvalidDataException e) {
            LOGGER.error(String.format("[getNextDateAsString][%s]%s", date, e.toString()), e);
            return "";
        }
    }

    /** Stores a start time for a period.
     * <p>
     * Example - DST change on 2026-10-25 in Europe/Berlin:
     * <ul>
     * <li>2026-10-25 02:00:00 +0200 (summer time) -> 00:00:00 UTC</li>
     * <li>2026-10-25 02:00:00 +0100 (winter time) -> 01:00:00 UTC</li>
     * </ul>
     * Storing only the local time ("HH:mm:ss") means both summer and winter time share the same key ("02:00:00").<br />
     * This leads to two possible behaviors:
     * <ul>
     * <li><b>Without offset (current behavior):</b> One entry per local time: results in a UTC gap (01:00-01:45 UTC missing).</li>
     * <li><b>With offset:</b> Two entries for "02:00:00": results in more start times than usual on the DST change day.</li>
     * </ul>
     *
     * @param startTime The configured start time (local time, e.g. "02:00:00")
     * @param period The period to associate with this start time */
    private void add(String startTime, Period period) {
        Period p = periods.get(startTime);
        if (p == null) {
            periods.put(startTime, period);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("  [add][added][start=" + startTime + "]" + SOSString.toString(period, true));
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("  [add][overlapping period for start][start=%s][already added=%s]current=%s", startTime, SOSString
                        .toString(p, true), SOSString.toString(period, true)));
            }
        }
    }

    private void addRepeat(Period period, String dailyPlanDate, String scheduleTimeZone) throws ParseException {
        if (period.getRepeat().isEmpty() || "00:00:00".equals(period.getRepeat())) {
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[addRepeat][dailyPlanDate=" + dailyPlanDate + "]begin=" + dailyPlanDate + " " + period.getBegin() + ", " + "end="
                    + dailyPlanDate + " " + period.getEnd() + "(" + scheduleTimeZone + ")");
        }

        ZoneId scheduleZone = ZoneId.of(scheduleTimeZone);
        ZonedDateTime startZoned = LocalDateTime.parse(dailyPlanDate + " " + period.getBegin(), DATE_TIME_FORMATTER).atZone(scheduleZone);
        ZonedDateTime endZoned = LocalDateTime.parse(dailyPlanDate + " " + period.getEnd(), DATE_TIME_FORMATTER).atZone(scheduleZone);
        long repeatSeconds = LocalTime.parse(period.getRepeat()).toSecondOfDay();

        // UTC for iteration (no gaps)
        ZonedDateTime currentUtc = startZoned.withZoneSameInstant(ZoneOffset.UTC);
        ZonedDateTime endUtc = endZoned.withZoneSameInstant(ZoneOffset.UTC);
        while (repeatSeconds > 0 && currentUtc.isBefore(endUtc)) {
            // switch back to the original time zone for the key
            add(currentUtc.withZoneSameInstant(scheduleZone).format(START_TIME_FORMATTER), period);

            // UTC iteration
            currentUtc = currentUtc.plusSeconds(repeatSeconds);
        }

        // UTC - see PeriodResolverRepeatBeginEndHandler comments
        period.setBegin(startZoned.withZoneSameInstant(ZoneOffset.UTC).toLocalTime().toString());
        period.setEnd(endZoned.withZoneSameInstant(ZoneOffset.UTC).toLocalTime().toString());
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

    private String format(LocalDateTime d, String timeZone) {
        return d.toString().replace('T', ' ') + "(" + timeZone + ")";
    }

    private String format(ZonedDateTime d, Instant utc) {
        return d.format(DATE_TIME_FORMATTER) + "(" + d.getZone() + ")=" + format(utc);
    }

    private String format(Instant utc) {
        return utc.toString().replace('T', ' ').replace("Z", "") + "(Etc/UTC)";
    }

    private String format(long utc) {
        return format(Instant.ofEpochMilli(utc));
    }

    private class DailyPlanPeriodResult {

        private final long startEpochMilli;
        private final boolean isInDailyPlanPeriod;
        private final boolean canBeUsedAsLate;

        private DailyPlanPeriodResult(long startEpochMilli, boolean isInDailyPlanPeriod, boolean canBeUsedAsLate) {
            this.startEpochMilli = startEpochMilli;
            this.isInDailyPlanPeriod = isInDailyPlanPeriod;
            this.canBeUsedAsLate = canBeUsedAsLate;
        }
    }
}
