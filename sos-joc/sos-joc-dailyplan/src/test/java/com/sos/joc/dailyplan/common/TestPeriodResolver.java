package com.sos.joc.dailyplan.common;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.Map;
import java.util.TimeZone;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.inventory.model.calendar.Period;

// Test fails in nightly build
@Ignore
public class TestPeriodResolver {

    @BeforeClass
    public static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
    }

    @Test
    @Ignore
    public void testAddStartTimes() throws SOSInvalidDataException, ParseException {
        PeriodResolver resolver = new PeriodResolver(null);
        Period period = new Period();
        period.setBegin("12:00");
        period.setEnd("15:00");
        period.setRepeat("10:00");
        resolver.addStartTimes(period, "2020-01-01", "UTC");

        Map<Long, Period> l = resolver.getStartTimes("2020-01-01", "2020-01-01", "Europe/Berlin");
        assertEquals("testAddStartTimes", 18, l.size());
    }

}
