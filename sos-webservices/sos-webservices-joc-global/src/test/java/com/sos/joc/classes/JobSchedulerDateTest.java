package com.sos.joc.classes;

import java.time.Instant;
import java.util.Optional;
import java.util.TimeZone;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JobSchedulerDateTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerDateTest.class);
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Test
    public void testGetScheduledForInUTC() {
        LOGGER.trace(Instant.now().toString());
        Optional<Instant> scheduledFor = JobSchedulerDate.getScheduledForInUTC("now+10", "Europe/Berlin");
        Optional<Instant> scheduledFor2 = JobSchedulerDate.getScheduledForInUTC("2020-08-27 12:00", "Europe/Berlin");
        if (scheduledFor.isPresent()) {
            LOGGER.trace(scheduledFor.get().toString());
        }
        if (scheduledFor2.isPresent()) {
            LOGGER.trace(scheduledFor2.get().toString());
        }
    }

}
