package com.sos.joc.classes.calendar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.joc.Globals;
import com.sos.joc.model.calendar.Dates;

public class FrequencyResolverTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrequencyResolverTest.class);

    @Before
    public void setUp() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Ignore
    @Test
    public void restrictionsTest() throws Exception {
        String from = "2022-02-03";
        String to = "2022-02-04";
        Calendar baseCalendar = getCalendar(Paths.get("src/test/resources/calendar/calendar.json"));
        Calendar restrictions = getCalendar(Paths.get("src/test/resources/calendar/restrictions.json"));

        FrequencyResolver fr = new FrequencyResolver();
        Dates dates = fr.resolveRestrictions(baseCalendar, restrictions, from, to);
        LOGGER.info("DATES: " + String.join(",", dates.getDates()));
    }

    @Ignore
    @Test
    public void calendarEveryDailyTest() throws Exception {
        String from = "2022-04-16";
        String to = "2022-05-31";

        Calendar calendar = getCalendar(Paths.get("src/test/resources/calendar/calendar_every_daily.json"));

        FrequencyResolver fr = new FrequencyResolver();
        Dates dates = fr.resolve(calendar, from, to);
        LOGGER.info("DATES: " + String.join(",", dates.getDates()));
    }

    @Ignore
    @Test
    public void restrictionsEveryDailyTest() throws Exception {
        String from = "2022-02-03";
        String to = "2022-05-04";
        from = null;
        // to = null;

        Calendar baseCalendar = getCalendar(Paths.get("src/test/resources/calendar/calendar.json"));
        Calendar restrictions = getCalendar(Paths.get("src/test/resources/calendar/restrictions_every_daily.json"));
        // restrictions = null;

        FrequencyResolver fr = new FrequencyResolver();
        Dates dates = fr.resolveRestrictions(baseCalendar, restrictions, from, to);
        LOGGER.info("DATES: " + String.join(",", dates.getDates()));
    }

    @Ignore
    @Test
    public void restrictionsCalendarHolidaysEveryDailyTest() throws Exception {
        String from = "2022-04-18";
        String to = "2022-12-31";
        from = null;
        // to = null;

        Calendar baseCalendar = getCalendar(Paths.get("src/test/resources/calendar/calendar_holidays.json"));
        Calendar restrictions = getCalendar(Paths.get("src/test/resources/calendar/restrictions_every_daily.json"));
        // restrictions = null;

        FrequencyResolver fr = new FrequencyResolver();
        Dates dates = fr.resolveRestrictions(baseCalendar, restrictions, from, to);
        LOGGER.info("DATES: " + String.join(",", dates.getDates()));
    }

    @Ignore
    @Test
    public void restrictionsEveryWeeklyTest() throws Exception {
        String from = "2022-05-02";
        String to = "2022-05-04";
        // to = null;
        Calendar baseCalendar = getCalendar(Paths.get("src/test/resources/calendar/calendar.json"));
        Calendar restrictions = getCalendar(Paths.get("src/test/resources/calendar/restrictions_every_weekly.json"));
        // restrictions = null;

        FrequencyResolver fr = new FrequencyResolver();
        Dates dates = fr.resolveRestrictions(baseCalendar, restrictions, from, to);
        LOGGER.info("DATES: " + String.join(",", dates.getDates()));
    }

    @Ignore
    @Test
    public void restrictionsEveryMonthlyTest() throws Exception {
        String from = "2022-04-17";
        String to = "2022-05-31";
        Calendar baseCalendar = getCalendar(Paths.get("src/test/resources/calendar/calendar.json"));
        Calendar restrictions = getCalendar(Paths.get("src/test/resources/calendar/restrictions_every_monthly.json"));
        // restrictions = null;

        FrequencyResolver fr = new FrequencyResolver();
        Dates dates = fr.resolveRestrictions(baseCalendar, restrictions, from, to);
        LOGGER.info("DATES: " + String.join(",", dates.getDates()));
    }

    @Ignore
    @Test
    public void restrictionsEveryYearlyTest() throws Exception {
        String from = "2022-04-17";
        String to = "2025-08-04";
        Calendar baseCalendar = getCalendar(Paths.get("src/test/resources/calendar/calendar.json"));
        Calendar restrictions = getCalendar(Paths.get("src/test/resources/calendar/restrictions_every_yearly.json"));
        // restrictions = null;

        FrequencyResolver fr = new FrequencyResolver();
        Dates dates = fr.resolveRestrictions(baseCalendar, restrictions, from, to);
        LOGGER.info("DATES: " + String.join(",", dates.getDates()));
    }

    @Ignore
    @Test
    public void testJavaCalendar() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.set(2022, 2, 1);
        int date = c.get(java.util.Calendar.DATE);
        int month = c.get(java.util.Calendar.MONTH) + 1;
        int dayOfWeek = c.get(java.util.Calendar.DAY_OF_WEEK) - 1;

        LOGGER.info(String.format("Calendar now: weekNo=%s, date=%s, month=%s, dayOfWeek=%s", getWeekOfMonth(c, false), date, month, dayOfWeek));
        LOGGER.info(String.format("Calendar now[calculate for ultimos]: weekNo=%s, date=%s, month=%s, dayOfWeek=%s", getWeekOfMonth(c, true), date,
                month, dayOfWeek));
    }

    private int getWeekOfMonth(java.util.Calendar c, boolean isUltimo) {
        int result = 1;
        int month = c.get(java.util.Calendar.MONTH);
        int weekStep = isUltimo ? 7 : -7;
        java.util.Calendar copy = (java.util.Calendar) c.clone();

        boolean run = true;
        while (run) {
            java.util.Calendar prev = (java.util.Calendar) copy.clone();
            prev.add(java.util.Calendar.DATE, weekStep);
            copy = prev;
            if (month == prev.get(java.util.Calendar.MONTH)) {
                result++;
            } else {
                run = false;
            }
            // max 5 - e.g. 2022-05-30 is a 5th Monday in month
            if (result > 5) {// avoid endless loop
                result = 5;
                run = false;
            }
        }
        return result;
    }

    private Calendar getCalendar(Path json) throws JsonParseException, JsonMappingException, IOException {
        return Globals.objectMapper.readValue(Files.readAllBytes(json), Calendar.class);
    }

}
