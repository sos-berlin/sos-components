package com.sos.js7.converter.js1.output.js7;

import java.time.LocalTime;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.js7.converter.js1.output.js7.JS7RunTimeConverter.TimeHelper;

public class JS7RunTimeConverterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JS7RunTimeConverterTest.class);

    @Ignore
    @Test
    public void testAdmissionTime() {
        LocalTime daily = LocalTime.of(12, 0);
        LOGGER.info("DAILY=" + daily.toSecondOfDay());

        LocalTime weekly = LocalTime.of(8, 0);
        for (int i = 1; i <= 7; i++) {
            LOGGER.info("WEEKLY=" + i + "=" + JS7RunTimeConverter.weekdayToSeconds(i, weekly));
        }
    }

    @Ignore
    @Test
    public void testNormalizeTime() {
        String time = "1:12";
        LOGGER.info("JS time[" + time + "]" + JS7RunTimeConverter.normalizeTime(time));

        time = "0:0:3";
        LOGGER.info("JS time[" + time + "]" + JS7RunTimeConverter.normalizeTime(time));

        time = "08:30";
        LOGGER.info("JS time[" + time + "]" + JS7RunTimeConverter.normalizeTime(time));

        time = "120";
        LOGGER.info("JS time[" + time + "]" + JS7RunTimeConverter.normalizeTime(time));

        time = "121";
        LOGGER.info("JS time[" + time + "]" + JS7RunTimeConverter.normalizeTime(time));

        time = "3600";
        LOGGER.info("JS time[" + time + "]" + JS7RunTimeConverter.normalizeTime(time));
    }

    @Ignore
    @Test
    public void testCalculateSeconds() {
        String time = "24:00:00";
        TimeHelper th = JS7RunTimeConverter.newTimeHelper(time);
        LOGGER.info("JS time[" + time + "]" + JS7RunTimeConverter.normalizeTime(time) + "=" + th.toSeconds());

    }
}
