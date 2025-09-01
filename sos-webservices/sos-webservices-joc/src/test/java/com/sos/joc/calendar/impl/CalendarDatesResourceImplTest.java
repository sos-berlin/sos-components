package com.sos.joc.calendar.impl;

import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.UnitTestSimpleWSImplHelper;

public class CalendarDatesResourceImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarDatesResourceImplTest.class);

    @Ignore
    @Test
    public void testRead() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new CalendarDatesResourceImpl());
        try {
            h.init();
            h.post("read", Paths.get("src/test/resources/ws/calendar/impl/request-CalendarDatesResourceImpl-read.json"));

        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

}
