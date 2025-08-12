package com.sos.joc.dailyplan.impl;

import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.UnitTestSimpleWSImplHelper;
import com.sos.joc.model.dailyplan.submissions.SubmissionsDeleteRequest;
import com.sos.joc.model.dailyplan.submissions.SubmissionsDeleteRequestFilter;

public class DailyPlanSubmissionsImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanSubmissionsImplTest.class);

    @Ignore
    @Test
    public void testPostDeleteDailyPlanSubmissions() throws Exception {
        SubmissionsDeleteRequest in = new SubmissionsDeleteRequest();
        in.setControllerId("js7.x");
        SubmissionsDeleteRequestFilter filter = new SubmissionsDeleteRequestFilter();
        filter.setDateFor("2026-03-29");
        in.setFilter(filter);

        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new DailyPlanSubmissionsImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");
        try {
            h.init();
            h.post("postDeleteDailyPlanSubmissions", in);

            TimeUnit.SECONDS.sleep(10);// sleep due to asynchronous call
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

}
