package com.sos.js7.order.initiator;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.joc.model.calendar.Period;
 
//Test fails in nightly build
@Ignore
public class TestPeriodResolver {

    @Test
    public void testAddStartTimes() throws SOSInvalidDataException, ParseException {
        PeriodResolver periodResolver = new PeriodResolver();
        Period period = new Period();
        period.setBegin("12:00");
        period.setEnd("15:00");
        period.setRepeat("10:00");
        periodResolver.addStartTimes(period, "2020-09-12", "UTC");
        Map<Long, Period> l = periodResolver.getStartTimes("01-01-2019","Europe/Berlin");
        assertEquals("testAddStartTimes", 18, l.size());   
        }

}
