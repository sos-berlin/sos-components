package com.sos.joc.dailyplan.common;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
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

        Map<Long, Period> l = resolver.getStartTimes("2020-01-01", "2020-01-01", "Europe/Berlin");
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

        List<String> dates = new FrequencyResolver().resolveRestrictions(cal, restrictions, actDateAsString, nextDateAsString).getDates();
        LOGGER.info("dates=" + dates);
    }

    @Ignore
    @Test
    public void testDayIsInPlanPeriodSingleStart() throws Exception {
        String dailyPlanDate = SOSDate.getDateAsString(SOSDate.add(new Date(), 1, ChronoUnit.DAYS));

        executeTestDayIsInPlanSinglestart(SOSDate.TIMEZONE_UTC, "00:00:00", dailyPlanDate, "America/Chicago", Arrays.asList("00:00:00"));
        executeTestDayIsInPlanSinglestart(SOSDate.TIMEZONE_UTC, "00:00:00", dailyPlanDate, "America/Chicago", Arrays.asList("05:00:00"));
        executeTestDayIsInPlanSinglestart(SOSDate.TIMEZONE_UTC, "00:00:00", dailyPlanDate, "America/Chicago", Arrays.asList("20:00:00"));
        executeTestDayIsInPlanSinglestart(SOSDate.TIMEZONE_UTC, "00:00:00", dailyPlanDate, "America/Chicago", Arrays.asList("23:00:00"));

        executeTestDayIsInPlanSinglestart(SOSDate.TIMEZONE_UTC, "03:00:00", dailyPlanDate, "America/Chicago", Arrays.asList("00:00:00"));
        executeTestDayIsInPlanSinglestart(SOSDate.TIMEZONE_UTC, "03:00:00", dailyPlanDate, "America/Chicago", Arrays.asList("23:00:00"));

        executeTestDayIsInPlanSinglestart("America/Chicago", "00:00:00", dailyPlanDate, "America/Chicago", Arrays.asList("00:00:00"));
        executeTestDayIsInPlanSinglestart("America/Chicago", "00:00:00", dailyPlanDate, "America/Chicago", Arrays.asList("23:00:00"));

        executeTestDayIsInPlanSinglestart("Europe/Berlin", "00:00:00", dailyPlanDate, "America/Chicago", Arrays.asList("00:00:00"));
        executeTestDayIsInPlanSinglestart("Europe/Berlin", "00:00:00", dailyPlanDate, "America/Chicago", Arrays.asList("23:00:00"));

        executeTestDayIsInPlanSinglestart(SOSDate.TIMEZONE_UTC, "00:00:00", dailyPlanDate, "Europe/Berlin", Arrays.asList("00:00:00"));
        executeTestDayIsInPlanSinglestart(SOSDate.TIMEZONE_UTC, "00:00:00", dailyPlanDate, "Europe/Berlin", Arrays.asList("23:00:00"));

        executeTestDayIsInPlanSinglestart("Asia/Kolkata", "00:00:00", dailyPlanDate, "Europe/Berlin", Arrays.asList("00:00:00"));
        executeTestDayIsInPlanSinglestart("Asia/Kolkata", "00:00:00", dailyPlanDate, "Europe/Berlin", Arrays.asList("23:00:00"));

        executeTestDayIsInPlanSinglestart("Europe/Berlin", "05:00:00", dailyPlanDate, "Europe/Berlin", Arrays.asList("00:00:00"));
        executeTestDayIsInPlanSinglestart("Europe/Berlin", "05:00:00", dailyPlanDate, "Europe/Berlin", Arrays.asList("01:00:00"));
        executeTestDayIsInPlanSinglestart("Europe/Berlin", "05:00:00", dailyPlanDate, "Europe/Berlin", Arrays.asList("02:00:00"));
        executeTestDayIsInPlanSinglestart("Europe/Berlin", "05:00:00", dailyPlanDate, "Europe/Berlin", Arrays.asList("03:00:00"));
        executeTestDayIsInPlanSinglestart("Europe/Berlin", "05:00:00", dailyPlanDate, "Europe/Berlin", Arrays.asList("23:00:00"));
        executeTestDayIsInPlanSinglestart("Europe/Berlin", "05:00:00", dailyPlanDate, "Europe/Berlin", Arrays.asList("23:59:59"));

        executeTestDayIsInPlanSinglestart("Europe/Berlin", "00:00:00", dailyPlanDate, "Europe/Berlin", Arrays.asList("00:00:00"));
        executeTestDayIsInPlanSinglestart("Europe/Berlin", "00:00:00", dailyPlanDate, "Europe/Berlin", Arrays.asList("01:00:00"));
        executeTestDayIsInPlanSinglestart("Europe/Berlin", "00:00:00", dailyPlanDate, "Europe/Berlin", Arrays.asList("02:00:00"));
        executeTestDayIsInPlanSinglestart("Europe/Berlin", "00:00:00", dailyPlanDate, "Europe/Berlin", Arrays.asList("03:00:00"));
        executeTestDayIsInPlanSinglestart("Europe/Berlin", "00:00:00", dailyPlanDate, "Europe/Berlin", Arrays.asList("23:00:00"));
        executeTestDayIsInPlanSinglestart("Europe/Berlin", "00:00:00", dailyPlanDate, "Europe/Berlin", Arrays.asList("23:59:59"));

        executeTestDayIsInPlanSinglestart("Asia/Kolkata", "00:00:00", dailyPlanDate, "Europe/Berlin", Arrays.asList("00:00:00", "23:00:00"));
        executeTestDayIsInPlanSinglestart("Asia/Kolkata", "03:00:00", dailyPlanDate, "Europe/Berlin", Arrays.asList("00:00:00", "01:00:00",
                "02:00:00", "23:00:00"));

    }

    @Ignore
    @Test
    public void testDayIsInPlanPeriodRepeat() throws Exception {
        String dailyPlanDate = SOSDate.getDateAsString(SOSDate.add(new Date(), 1, ChronoUnit.DAYS));

        executeTestDayIsInPlanRepeat(SOSDate.TIMEZONE_UTC, "03:00:00", dailyPlanDate, "America/Chicago", Arrays.asList(new TestPeriodRepeat(
                "00:00:00", "24:00:00", "01:00:00"), new TestPeriodRepeat("00:30:00", "24:00:00", "01:00:00")));

    }

    private void executeTestDayIsInPlanSinglestart(String dailyPlanTimeZone, String dailyPlanPeriodBegin, String dailyPlanDate,
            String scheduleTimeZone, List<String> schedulePeriodSingleStarts) throws Exception {
        List<Period> periods = new ArrayList<>();
        for (String singleStart : schedulePeriodSingleStarts) {
            Period period = new Period();
            period.setSingleStart(singleStart);
            periods.add(period);
        }
        executeTestDayIsInPlan(dailyPlanTimeZone, dailyPlanPeriodBegin, dailyPlanDate, scheduleTimeZone, periods);
    }

    private void executeTestDayIsInPlanRepeat(String dailyPlanTimeZone, String dailyPlanPeriodBegin, String dailyPlanDate, String scheduleTimeZone,
            List<TestPeriodRepeat> tp) throws Exception {
        executeTestDayIsInPlan(dailyPlanTimeZone, dailyPlanPeriodBegin, dailyPlanDate, scheduleTimeZone, tp.stream().map(e -> e.period).collect(
                Collectors.toList()));
    }

    private void executeTestDayIsInPlan(String dailyPlanTimeZone, String dailyPlanPeriodBegin, String dailyPlanDate, String scheduleTimeZone,
            List<Period> periods) throws Exception {

        LOGGER.info("--------------------------------------------------");
        LOGGER.info(String.format("[DailyPlanDate]%s", dailyPlanDate));
        LOGGER.info(String.format("[DailyPlan]TimeZone=%s, PeriodBegin=%s", dailyPlanTimeZone, dailyPlanPeriodBegin));
        LOGGER.info(String.format("[Schedule]TimeZone=%s, Period(s)=%s", scheduleTimeZone, SOSString.toString(periods, true)));
        LOGGER.info("--------------------------------------------------");

        DailyPlanSettings s = new DailyPlanSettings();
        s.setTimeZone(dailyPlanTimeZone);
        s.setPeriodBegin(dailyPlanPeriodBegin);

        PeriodResolver pr = new PeriodResolver(s);
        for (Period period : periods) {
            pr.addStartTimes(period, dailyPlanDate, scheduleTimeZone);
        }

        Date frd = SOSDate.getDate(dailyPlanDate);
        List<String> frequencyResolverDates = new ArrayList<>();
        frequencyResolverDates.add(dailyPlanDate);
        frequencyResolverDates.add(SOSDate.getDateAsString(SOSDate.add(frd, 1, ChronoUnit.DAYS)));

        for (String frequencyResolverDate : frequencyResolverDates) {
            pr.getStartTimes(frequencyResolverDate, dailyPlanDate, scheduleTimeZone);
        }
    }

    private class TestPeriodRepeat {

        private final Period period;

        private TestPeriodRepeat(String begin, String end, String repeat) {
            period = new Period();
            period.setBegin(begin);
            period.setEnd(end);
            period.setRepeat(repeat);
        }
    }

}
