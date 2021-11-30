package com.sos.joc.dailyplan.common;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.TimeZone;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.schedule.Schedule;

// Test fails in nightly build
@Ignore
public class TestSchedules {

    @BeforeClass
    public static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
    }

    @Test
    @Ignore
    public void testIsFillListOfSchedules() throws IOException, SOSHibernateException {
        ScheduleSource source = new ScheduleSourceFile("src/test/resources/schedules");
        List<Schedule> schedules = source.getSchedules();
        Schedule order = schedules.get(0);

        assertEquals("testIsFillListOfSchedules", "testorder", order.getPath());
    }

}
