package com.sos.joc.dailyplan.impl;

import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.UnitTestSimpleWSImplHelper;
import com.sos.joc.model.dailyplan.history.MainRequest;

public class DailyPlanHistoryImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanHistoryImplTest.class);

    @Ignore
    @Test
    public void testPostDates() throws Exception {
        MainRequest in = new MainRequest();
        in.setControllerId("js7.x");
        in.setLimit(Integer.valueOf(1));

        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new DailyPlanHistoryImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");
        try {
            h.init();
            h.post("postDates", in);

            TimeUnit.SECONDS.sleep(10);// sleep due to asynchronous call
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

}
