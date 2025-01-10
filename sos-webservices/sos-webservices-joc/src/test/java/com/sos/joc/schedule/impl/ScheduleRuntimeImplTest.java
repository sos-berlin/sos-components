package com.sos.joc.schedule.impl;

import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.UnitTestSimpleWSImplHelper;

public class ScheduleRuntimeImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleRuntimeImplTest.class);

    @Ignore
    @Test
    public void testPostScheduleRuntime() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new ScheduleRuntimeImpl());
        try {
            h.init();

            // for (int i = 0; i < 2; i++) { // Multithreading
            // h.post("postScheduleRuntime", "{}");
            h.post("postScheduleRuntime", Paths.get("src/test/resources/ws/schedule/impl/request-ScheduleRuntimeImpl-postScheduleRuntime.json"));
            // }
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

}
