package com.sos.joc.dailyplan.common;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSCollection;
import com.sos.inventory.model.calendar.Period;
import com.sos.joc.dailyplan.db.DBLayerDailyPlannedOrders;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;

/** Not used - currently under development.<br />
 *
 * Currently, the Daily Plan writes UTC ISO date-times to DPL_ORDERS.PERIOD_BEGIN/PERIOD_END, simply based on the dailyPlanDate and UTC time.<br/>
 * - see {@link DBLayerDailyPlannedOrders#store(PlannedOrder, String, Integer, Integer)},<br />
 * - see {@link DBItemDailyPlanOrder#setPeriodBegin(java.util.Date, String)}<br />
 *
 * In some cases, this can produce unexpected results, e.g.:<br />
 * - Repeat 00:00:00-24:00:00:<br />
 * -- PERIOD_BEGIN/PERIOD_END (Europe/Berlin) will be set to the same value, e.g. 2026-08-21 22:00:00.000.<br />
 * - DailyPlan period_begin setting != 00:00:00.<br />
 * -- additionally, some start times may fall outside the period range because they are shifted to the next day.<br />
 * - DST - time maybe not switched...<br />
 * - ...<br />
 *
 * This handler can be applied to PeriodResolver.getStartTimes() before returning. */
public class PeriodResolverRepeatBeginEndHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PeriodResolverRepeatBeginEndHandler.class);

    public void process(String dailyPlanDate, Map<Long, Period> startTimes, String timeZone) {

        try {
            Map<PeriodKey, Map<Long, Period>> grouped = startTimes.entrySet().stream().filter(e -> e.getValue().getBegin() != null).collect(Collectors
                    .groupingBy(e -> new PeriodKey(e.getValue().getBegin(), e.getValue().getEnd(), e.getValue().getRepeat()), Collectors.toMap(
                            Map.Entry::getKey, Map.Entry::getValue)));

            if (SOSCollection.isEmpty(grouped)) {
                return;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[process][dailyPlanDate=" + dailyPlanDate + "]groups=" + grouped.size());
            }

            grouped.forEach((periodKey, periods) -> {
                Optional<Long> min = periods.keySet().stream().min(Long::compareTo); // use dailyPlanDate + begin instead ?
                Optional<Long> max = periods.keySet().stream().max(Long::compareTo);

                if (min.isPresent() && max.isPresent()) {
                    // simply use min-max instead of the calculation below?

                    // local time in the specified time zone
                    LocalDateTime begin = replaceUtcTimestampWithTimezoneLocalTime(min.get(), "begin", periodKey.begin, timeZone);
                    LocalDateTime end = replaceUtcTimestampWithTimezoneLocalTime(max.get(), "end", periodKey.end, timeZone);

                    if (begin.equals(end)) {
                        begin = begin.minusDays(1);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("[RepeatBeginEndHandler][process][begin][changed -1day][because the begin equals the end]begin=" + begin
                                    + ", end=" + end);
                        }

                    } else {
                        int diff = end.getDayOfYear() - begin.getDayOfYear();
                        if (diff == 2) { // because of DST + period_begin != 00:00:00
                            begin = begin.plusDays(1);
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("[RepeatBeginEndHandler][process][begin][changed +1day][because the end date differs by 2 days]begin="
                                        + begin + ", end=" + end);
                            }
                        }
                    }

                    // UTC time
                    // begin = end.atZone(ZoneId.of(timeZone)).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
                    // end = end.atZone(ZoneId.of(timeZone)).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
                    begin = begin.atZone(ZoneId.of(timeZone)).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
                    end = end.atZone(ZoneId.of(timeZone)).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("[RepeatBeginEndHandler][process][UTC]begin=" + begin + ", end=" + end);
                    }

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String newMinTime = begin.format(formatter);
                    String newMaxTime = end.format(formatter);

                    periods.entrySet().forEach(ce -> {
                        ce.getValue().setBegin(newMinTime);
                        ce.getValue().setEnd(newMaxTime);
                    });
                }
            });
        } catch (Exception e) {
            LOGGER.warn("[RepeatBeginEndHandler][process]" + e.toString(), e);
        }
    }

    /** Replaces the UTC timestamp's time with a local time in the specified time zone
     * 
     * @param dateTime
     * @param repeatBeginOrEnd
     * @param timeZone
     * @return */
    private LocalDateTime replaceUtcTimestampWithTimezoneLocalTime(long dateTime, String range, String repeatBeginOrEnd, String timeZone) {
        ZonedDateTime origUtc = Instant.ofEpochMilli(dateTime).atZone(ZoneOffset.UTC);
        LocalDate origDate = origUtc.toLocalDate();
        LocalDateTime replaced = null;
        try {
            if (repeatBeginOrEnd.startsWith("24:")) {
                replaced = LocalDateTime.of(origDate, LocalTime.parse("00:00:00")).plusDays(1);
            } else {
                replaced = LocalDateTime.of(origDate, LocalTime.parse(repeatBeginOrEnd));
            }
        } catch (Exception e) {
            LOGGER.warn("[RepeatBeginEndHandler][" + range + "=" + repeatBeginOrEnd + "][startTime=" + dateTime + "(" + origUtc + ")][repeat(" + range
                    + ") is set to midnight because " + repeatBeginOrEnd + " is invalid]" + e.toString(), e);
            replaced = LocalDateTime.of(origDate, LocalTime.parse("00:00:00")).plusDays(1);
        }
        // replaced = replaced.atZone(ZoneId.of(timeZone)).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        replaced = replaced.atZone(ZoneId.of(timeZone)).toLocalDateTime();

        if (LOGGER.isDebugEnabled()) {
            String scope = "end".equals(range) ? "max" : "min";
            LOGGER.debug("[RepeatBeginEndHandler][" + range + "=" + repeatBeginOrEnd + "][" + scope + " repeat value=" + origUtc + "]replaced="
                    + replaced);
        }
        return replaced;
    }

    private class PeriodKey {

        private final String begin;
        private final String end;
        private final String repeat;

        public PeriodKey(String begin, String end, String repeat) {
            this.begin = begin;
            this.end = end;
            this.repeat = repeat;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof PeriodKey)) {
                return false;
            }

            PeriodKey a = (PeriodKey) o;
            return repeat == a.repeat && Objects.equals(begin, a.begin) && Objects.equals(end, a.end);
        }

        @Override
        public int hashCode() {
            return Objects.hash(begin, end, repeat);
        }

    }
}
