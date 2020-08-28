package com.sos.joc.classes;

import java.time.Instant;
import java.util.Optional;
import java.util.TimeZone;

import org.junit.BeforeClass;
import org.junit.Test;


public class JobSchedulerDateTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Test
    public void testGetScheduledForInUTC() {
        System.out.println(Instant.now());
        Optional<Instant> scheduledFor = JobSchedulerDate.getScheduledForInUTC("now+10", "Europe/Berlin");
        Optional<Instant> scheduledFor2 = JobSchedulerDate.getScheduledForInUTC("2020-08-27 12:00", "Europe/Berlin");
        if (scheduledFor.isPresent()) {
            System.out.println(scheduledFor.get());
        }
        if (scheduledFor2.isPresent()) {
            System.out.println(scheduledFor2.get());
        }
    }

}
