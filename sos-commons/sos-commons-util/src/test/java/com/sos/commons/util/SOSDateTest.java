package com.sos.commons.util;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSDateTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSDateTest.class);

    @Ignore
    @Test
    public void test() throws Exception {
        Date d = new Date();
        LOGGER.info("[date][" + SOSDate.getDateTimeAsString(d) + "]" + d);
        LOGGER.info(" [week]" + SOSDate.getWeek(d));
        LOGGER.info(" [month]" + SOSDate.getMonth(d));
        LOGGER.info(" [year]" + SOSDate.getYear(d));
        LOGGER.info(" [format][PST]" + SOSDate.format(d, "yyyy-MM-dd HH:mm:ss.SSSZZZZ", TimeZone.getTimeZone("PST")));
        LOGGER.info(" [format][Europe/Berlin]" + SOSDate.format(d, "yyyy-MM-dd HH:mm:ss.SSSZZZZ", TimeZone.getTimeZone("Europe/Berlin")));
        LOGGER.info(" [DurationOfSeconds][0]" + SOSDate.getDurationOfSeconds(0));
        LOGGER.info(" [DurationOfSeconds][60]" + SOSDate.getDurationOfSeconds(60));
        LOGGER.info(" [DurationOfSeconds][100_000]" + SOSDate.getDurationOfSeconds(100_000));
        LOGGER.info(" [add][3 days]" + SOSDate.add(new Date(), 3, ChronoUnit.DAYS));
        LOGGER.info(" [add][-3 days]" + SOSDate.add(new Date(), -3, ChronoUnit.DAYS));

        List<String> times = Arrays.asList("00:02", "04:01", "04:30", "05:00", "05:02", "23:00", "23:01");
        LOGGER.info(" [FilteredTimesInRange]" + SOSDate.getFilteredTimesInRange("05:00", "23:00", times));
        LOGGER.info(" [FilteredTimesInRange]" + SOSDate.getFilteredTimesInRange("05:00", "04:01", times));

    }

    @Ignore
    @Test
    public void testTimeZones() throws Exception {
        String timeZone = SOSDate.TIMEZONE_UTC;
        // timeZone = "Europe/Berlin";

        TimeZone.setDefault(TimeZone.getTimeZone(timeZone));

        try {
            Date d = new Date();
            String dateTime = SOSDate.getDateTimeAsString(d);
            LOGGER.info("[date][" + dateTime + "]" + d);
            dateTime = "2026-03-29 02:00:00";
            LOGGER.info("[getDate][" + dateTime + "][" + timeZone + "]" + SOSDate.getDate(dateTime));
            // LOGGER.info("[getDate2][" + dateTime + "][" + timeZone + "]" + SOSDate.parseZ(dateTime,SOSDate.DATE_FORMAT,null));
            LOGGER.info("[getDateTime][" + dateTime + "][" + timeZone + "]" + SOSDate.getDateTime(dateTime));

            timeZone = "Europe/Berlin";

            LOGGER.info("[getDateTime][" + dateTime + "][" + timeZone + "]" + SOSDate.getDateTime(dateTime, TimeZone.getTimeZone(timeZone)));
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

}
