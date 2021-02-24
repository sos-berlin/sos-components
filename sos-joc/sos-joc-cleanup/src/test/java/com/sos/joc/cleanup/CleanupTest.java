package com.sos.joc.cleanup;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleanupTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTest.class);

    @Ignore
    @Test
    public void testDateTimeParse() throws Exception {
        String dt = "2021-02-24T10:55+01:00[Europe/Berlin]->2021-02-24T10:55+01:00[Europe/Berlin]";

        String[] arr = dt.split("->");

        ZonedDateTime zdt = ZonedDateTime.parse(arr[0], DateTimeFormatter.ISO_ZONED_DATE_TIME);

        Date now = new Date();

        long l = now.getTime() * 1_000 + 999;

        LOGGER.info("" + now.getTime());
        LOGGER.info("" + now.getTime() * 1_000);
        LOGGER.info("" + l);
    }
}
