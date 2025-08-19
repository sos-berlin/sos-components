package com.sos.joc.dailyplan.impl;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.UnitTestSimpleWSImplHelper;
import com.sos.joc.model.audit.AuditLog;

public class DailyPlanProjectionsImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanProjectionsImplTest.class);

    @Ignore
    @Test
    public void testRecreate() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new DailyPlanProjectionsImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");
        try {
            h.init();

            h.post("recreate", new AuditLog());

            TimeUnit.SECONDS.sleep(20);// sleep due to asynchronous call
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

    @Ignore
    @Test
    public void testCalendarProjections() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new DailyPlanProjectionsImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");
        try {
            h.init();

            h.post("calendarProjections", Paths.get(
                    "src/test/resources/ws/dailyplan/impl/request-DailyPlanProjectionsImpl-calendarProjections.json"));
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

}
