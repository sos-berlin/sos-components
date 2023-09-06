package com.sos.joc.dailyplan.common;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.inventory.model.calendar.Period;
import com.sos.joc.classes.calendar.FrequencyResolver;

public class PeriodHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(PeriodHelper.class);

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_INSTANT;

    public static List<Period> getPeriods(List<Period> periods, List<String> holidays, String date, ZoneId timezone) {
        if (periods == null) {
            return new ArrayList<>();
        }
        return periods.stream().map(p -> getPeriod(p, holidays, date, timezone)).filter(Objects::nonNull).sorted(Comparator.comparing(p -> p
                .getSingleStart() == null ? p.getBegin() : p.getSingleStart())).collect(Collectors.toList());
    }

    private static Period getPeriod(Period period, List<String> holidays, String date, ZoneId timezone) {
        Period p = new Period();

        if (holidays.contains(date)) {
            if (period.getWhenHoliday() != null) {
                switch (period.getWhenHoliday()) {
                case SUPPRESS:
                    return null;
                case NEXTNONWORKINGDAY:
                    try {
                        java.util.Calendar dateCal = FrequencyResolver.getCalendarFromString(date);
                        dateCal.add(java.util.Calendar.DATE, 1);
                        date = DailyPlanHelper.DATE_FORMATTER.format(dateCal.toInstant());
                        while (holidays.contains(date)) {
                            dateCal.add(java.util.Calendar.DATE, 1);
                            date = DailyPlanHelper.DATE_FORMATTER.format(dateCal.toInstant());
                        }
                    } catch (SOSInvalidDataException e) {
                        LOGGER.error(String.format("[%s] %s", period.toString(), e.toString()));
                        return null;
                    }
                    break;
                case PREVIOUSNONWORKINGDAY:
                    try {
                        java.util.Calendar dateCal = FrequencyResolver.getCalendarFromString(date);
                        dateCal.add(java.util.Calendar.DATE, -1);
                        date = DailyPlanHelper.DATE_FORMATTER.format(dateCal.toInstant());
                        while (holidays.contains(date)) {
                            dateCal.add(java.util.Calendar.DATE, -1);
                            date = DailyPlanHelper.DATE_FORMATTER.format(dateCal.toInstant());
                        }
                    } catch (SOSInvalidDataException e) {
                        LOGGER.error(String.format("[%s] %s", period.toString(), e.toString()));
                        return null;
                    }
                    break;
                case IGNORE:
                    break;
                }
            } else {
                return null;
            }
        }

        if (period.getSingleStart() != null) {
            p.setSingleStart(isoFormatter.format(ZonedDateTime.of(LocalDateTime.parse(date + "T" + normalizeTime(period.getSingleStart()),
                    dateTimeFormatter), timezone)));
            return p;
        }
        if (period.getRepeat() != null && !period.getRepeat().isEmpty()) {
            p.setRepeat(period.getRepeat());
            String begin = period.getBegin();
            if (begin == null || begin.isEmpty()) {
                begin = "00:00:00";
            } else {
                begin = normalizeTime(begin);
            }

            p.setBegin(isoFormatter.format(ZonedDateTime.of(LocalDateTime.parse(date + "T" + begin, dateTimeFormatter), timezone)));
            String end = period.getEnd();
            if (end == null || end.isEmpty()) {
                end = "24:00:00";
            } else {
                end = normalizeTime(end);
            }
            if (end.startsWith("24:00")) {
                p.setEnd(isoFormatter.format(ZonedDateTime.of(LocalDateTime.parse(date + "T23:59:59", dateTimeFormatter).plusSeconds(1L), timezone)));
            } else {
                p.setEnd(isoFormatter.format(ZonedDateTime.of(LocalDateTime.parse(date + "T" + end, dateTimeFormatter), timezone)));
            }
            return p;
        }
        return null;
    }

    private static String normalizeTime(String time) {
        String[] ss = (time + ":00:00:00").split(":", 3);
        ss[2] = ss[2].substring(0, 2);
        return String.format("%2s:%2s:%2s", ss[0], ss[1], ss[2]).replace(' ', '0');
    }

}
