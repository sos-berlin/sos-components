package com.sos.joc.dailyplan.common;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.calendar.CalendarType;
import com.sos.inventory.model.calendar.Frequencies;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.calendar.WeekDays;
import com.sos.joc.classes.calendar.FrequencyResolver;
import com.sos.joc.dailyplan.DailyPlanRunnerTest;

public class TestPeriodResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestPeriodResolver.class);

    @BeforeClass
    public static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone(SOSDate.TIMEZONE_UTC));
    }

    @Ignore
    @Test
    public void testAddStartTimes() throws SOSInvalidDataException, ParseException {
        PeriodResolver resolver = new PeriodResolver(null);
        Period period = new Period();
        period.setBegin("12:00");
        period.setEnd("15:00");
        period.setRepeat("10:00");
        resolver.addStartTimes(period, "2020-01-01", SOSDate.TIMEZONE_UTC);

        Map<Long, Period> l = resolver.getStartTimes("2020-01-01", "2020-01-01", "Europe/Berlin", false);
        assertEquals("testAddStartTimes", 18, l.size());
    }

    @Ignore
    @Test
    public void testFrequencyResolver() throws Exception {
        String actDateAsString = "2023-10-18";
        String nextDateAsString = "2023-10-19";

        com.sos.inventory.model.calendar.Calendar cal = new com.sos.inventory.model.calendar.Calendar();
        cal.setId(Long.valueOf(1));
        cal.setName("AnyDays");
        cal.setPath("/" + cal.getName());
        cal.setType(CalendarType.WORKINGDAYSCALENDAR);
        Frequencies includes = new Frequencies();
        List<WeekDays> weekDays = new ArrayList<>();
        WeekDays wd = new WeekDays();
        // wd.setDays(Arrays.asList(0, 1, 2, 3, 4, 5, 6));
        wd.setDays(Arrays.asList(3, 6));
        weekDays.add(wd);
        includes.setWeekdays(weekDays);
        cal.setIncludes(includes);

        cal.setFrom(actDateAsString);
        cal.setTo(nextDateAsString);

        com.sos.inventory.model.calendar.Calendar restrictions = new com.sos.inventory.model.calendar.Calendar();
        Map<String, com.sos.inventory.model.calendar.Calendar> restrictionsNonWorkingDayCalendars = Map.of();
        List<String> dates = new FrequencyResolver().resolveRestrictions(cal, restrictions, restrictionsNonWorkingDayCalendars, actDateAsString,
                nextDateAsString).getDates();
        LOGGER.info("dates=" + dates);
    }

    @Ignore
    @Test
    public void testDayIsInPlanPeriodSingleStart() throws Exception {
        List<String> dailyPlanDates = getDailyPlanDays(SOSDate.add(new Date(), 1, ChronoUnit.DAYS), 1); // the next day only
        // dailyPlanDates = getDailyPlanDays(3); // 3 days from the current day

        // same period_begin=00:00:00, same start times, different time zones
        executeTestDayIsInPlan(SOSDate.TIMEZONE_UTC, "00:00:00", dailyPlanDates, "America/Chicago", Arrays.asList(new TestPeriod("00:00:00"),
                new TestPeriod("05:00:00"), new TestPeriod("20:00:00"), new TestPeriod("23:00:00")));

        executeTestDayIsInPlan(SOSDate.TIMEZONE_UTC, "03:00:00", dailyPlanDates, "America/Chicago", Arrays.asList(new TestPeriod("00:00:00"),
                new TestPeriod("23:00:00")));

        executeTestDayIsInPlan("America/Chicago", "00:00:00", dailyPlanDates, "America/Chicago", Arrays.asList(new TestPeriod("00:00:00"),
                new TestPeriod("23:00:00")));

        executeTestDayIsInPlan("Europe/Berlin", "00:00:00", dailyPlanDates, "America/Chicago", Arrays.asList(new TestPeriod("00:00:00"),
                new TestPeriod("23:00:00")));

        executeTestDayIsInPlan(SOSDate.TIMEZONE_UTC, "00:00:00", dailyPlanDates, "Europe/Berlin", Arrays.asList(new TestPeriod("00:00:00"),
                new TestPeriod("23:00:00")));

        executeTestDayIsInPlan("Asia/Kolkata", "00:00:00", dailyPlanDates, "Europe/Berlin", Arrays.asList(new TestPeriod("00:00:00"), new TestPeriod(
                "23:00:00")));

        // different period_begin, same start times, same time zones
        executeTestDayIsInPlan("Europe/Berlin", "00:00:00", dailyPlanDates, "Europe/Berlin", Arrays.asList(new TestPeriod("00:00:00"), new TestPeriod(
                "01:00:00"), new TestPeriod("02:00:00"), new TestPeriod("03:00:00"), new TestPeriod("23:00:00"), new TestPeriod("23:59:59")));

        executeTestDayIsInPlan("Europe/Berlin", "05:00:00", dailyPlanDates, "Europe/Berlin", Arrays.asList(new TestPeriod("00:00:00"), new TestPeriod(
                "01:00:00"), new TestPeriod("02:00:00"), new TestPeriod("03:00:00"), new TestPeriod("23:00:00"), new TestPeriod("23:59:59")));

        // different period_begin, different time zones (daily plan / schedule)
        executeTestDayIsInPlan("Asia/Kolkata", "00:00:00", dailyPlanDates, "Europe/Berlin", Arrays.asList(new TestPeriod("00:00:00"), new TestPeriod(
                "23:00:00")));
        executeTestDayIsInPlan("Asia/Kolkata", "03:00:00", dailyPlanDates, "Europe/Berlin", Arrays.asList(new TestPeriod("00:00:00"), new TestPeriod(
                "01:00:00"), new TestPeriod("02:00:00"), new TestPeriod("23:00:00")));

    }

    @Ignore
    @Test
    public void testDayIsInPlanPeriodRepeat() throws Exception {
        String dailyPlanDate = SOSDate.getDateAsString(SOSDate.add(new Date(), 1, ChronoUnit.DAYS));

        executeTestDayIsInPlan(SOSDate.TIMEZONE_UTC, "03:00:00", Arrays.asList(dailyPlanDate), "America/Chicago", Arrays.asList(new TestPeriod(
                "00:00:00", "24:00:00", "01:00:00"), new TestPeriod("00:30:00", "24:00:00", "01:00:00")));

    }

    /** See {@link DailyPlanRunnerTest#testRunNowDST()}
     * 
     * @throws Exception */
    @Ignore
    @Test
    public void testDayIsInPlanPeriodDST() throws Exception {
        // 2026 - winter time - 2026-10-25
        // 2027 summer time - 2027-03-28

        String currentSystemDatetime = null;
        int dailyPlanDays = 4;

        boolean testSummer = true;
        try {
            if (testSummer) {
                currentSystemDatetime = "2027-03-27 05:30:00"; // summer
                // currentSystemDatetime = "2027-03-27 01:00:00"; // summer
                // currentSystemDatetime = "2027-03-27 22:00:00"; // summer
            } else {
                currentSystemDatetime = "2026-10-23 02:30:00"; // winter
                // currentSystemDatetime = "2026-10-24 22:00:00";
            }

            DailyPlanRunnerTest.setTimeTransformer(currentSystemDatetime);
            if (testSummer) {
                // executeTestDayIsInPlan("Europe/Berlin", "06:00:00", getDailyPlanDays(dailyPlanDays), "Europe/Berlin", Arrays.asList(new TestPeriod(
                // "01:59:59", "03:30:00", "00:15:00")));

                // executeTestDayIsInPlan("Europe/Berlin", "06:00:00", getDailyPlanDays(dailyPlanDays), "Europe/Berlin", Arrays.asList(new TestPeriod(
                // "02:00:00"), new TestPeriod("02:15:00"), new TestPeriod("02:30:00"), new TestPeriod("02:45:00"), new TestPeriod("03:00:00"),
                // new TestPeriod("03:30:00"), new TestPeriod("03:45:00"), new TestPeriod("04:00:00")));

                executeTestDayIsInPlan("Europe/Berlin", "06:00:00", getDailyPlanDays(dailyPlanDays), "Europe/Berlin", Arrays.asList(new TestPeriod(
                        "02:00:00", "04:00:00", "00:15:00")));

                // executeTestDayIsInPlan("Europe/Berlin", "00:00:00", getDailyPlanDays(dailyPlanDays), "Europe/Berlin", Arrays.asList(new TestPeriod(
                // "00:00:00", "24:00:00", "00:01:00")));

                // executeTestDayIsInPlan("Europe/Berlin", "00:00:00", getDailyPlanDays(dailyPlanDays), "Europe/Berlin", Arrays.asList(new TestPeriod(
                // "00:00:00"), new TestPeriod("01:00:00"), new TestPeriod("02:00:00"), new TestPeriod("23:00:00"), new TestPeriod("02:00:00")));

                // executeTestDayIsInPlan("Europe/Berlin", "06:00:00", getDailyPlanDays(dailyPlanDays), "Europe/Berlin", Arrays.asList(new TestPeriod(
                // "00:00:00"), new TestPeriod("01:00:00"), new TestPeriod("02:00:00"), new TestPeriod("23:00:00"), new TestPeriod("00:00:00",
                // "06:30:00", "00:15:00")));
            } else {

                executeTestDayIsInPlan("Europe/Berlin", "06:00:00", getDailyPlanDays(dailyPlanDays), "Europe/Berlin", Arrays.asList(new TestPeriod(
                        "00:00:00", "06:30:00", "00:15:00")));
            }

            LOGGER.info("[Note][!!!! Instant.now() not mockable]" + Instant.now() + " vs " + currentSystemDatetime);
            LOGGER.info("[Note]LocalDateTime.now()]" + LocalDateTime.now());

        } finally {
            DailyPlanRunnerTest.resetTimeTransformer();
        }
    }

    private List<String> getDailyPlanDays(int number) throws Exception {
        return getDailyPlanDays(null, number);
    }

    private List<String> getDailyPlanDays(Date from, int number) throws Exception {
        if (from == null) {
            from = new Date();
        }
        List<String> l = new ArrayList<>();
        l.add(SOSDate.getDateAsString(from));
        for (int i = 1; i < number; i++) {
            l.add(SOSDate.getDateAsString(SOSDate.add(from, i, ChronoUnit.DAYS)));
        }
        return l;
    }

    private void executeTestDayIsInPlan(String dailyPlanTimeZone, String dailyPlanPeriodBegin, List<String> dailyPlanDates, String scheduleTimeZone,
            List<TestPeriod> tp) throws Exception {
        doExecuteTestDayIsInPlan(dailyPlanTimeZone, dailyPlanPeriodBegin, dailyPlanDates, scheduleTimeZone, tp.stream().map(e -> e.period).collect(
                Collectors.toList()));
    }

    private void doExecuteTestDayIsInPlan(String dailyPlanTimeZone, String dailyPlanPeriodBegin, List<String> dailyPlanDates, String scheduleTimeZone,
            List<Period> periods) throws Exception {

        LOGGER.info("--------------------------------------------------");
        LOGGER.info(String.format("[DailyPlanDates]%s", String.join(", ", dailyPlanDates)));
        LOGGER.info(String.format("[DailyPlan]TimeZone=%s, PeriodBegin=%s", dailyPlanTimeZone, dailyPlanPeriodBegin));
        LOGGER.info(String.format("[Schedule]TimeZone=%s, Period(s)=%s", scheduleTimeZone, SOSString.toString(periods, true)));
        LOGGER.info("--------------------------------------------------");

        DailyPlanSettings s = new DailyPlanSettings();
        s.setTimeZone(dailyPlanTimeZone);
        s.setPeriodBegin(dailyPlanPeriodBegin);

        for (String dailyPlanDate : dailyPlanDates) {
            LOGGER.info(String.format("[DailyPlanDate]%s------------------", dailyPlanDate));

            // Date frd = SOSDate.getDate(dailyPlanDate);
            List<String> frequencyResolverDates = new ArrayList<>();
            frequencyResolverDates.add(dailyPlanDate);
            // frequencyResolverDates.add(SOSDate.getDateAsString(SOSDate.add(frd, 1, ChronoUnit.DAYS)));

            for (String frequencyResolverDate : frequencyResolverDates) {
                PeriodResolver pr = new PeriodResolver(s);
                for (Period period : periods) {
                    Period p = new Period();
                    p.setSingleStart(period.getSingleStart());
                    p.setBegin(period.getBegin());
                    p.setEnd(period.getEnd());
                    p.setRepeat(period.getRepeat());
                    p.setWhenHoliday(period.getWhenHoliday());

                    pr.addStartTimes(p, dailyPlanDate, scheduleTimeZone);
                }
                pr.getStartTimes(frequencyResolverDate, dailyPlanDate, scheduleTimeZone, false);
            }
        }
    }

    private class TestPeriod {

        private final Period period;

        private TestPeriod(String singleStart) {
            period = new Period();
            period.setSingleStart(singleStart);
        }

        private TestPeriod(String begin, String end, String repeat) {
            period = new Period();
            period.setBegin(begin);
            period.setEnd(end);
            period.setRepeat(repeat);
        }
    }

}
