package com.sos.joc.dailyplan.impl;

import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.UnitTestSimpleWSImplHelper;
import com.sos.joc.model.dailyplan.generate.GenerateRequest;
import com.sos.joc.model.dailyplan.generate.items.PathItem;

public class DailyPlanOrdersGenerateImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanOrdersGenerateImplTest.class);

    @Ignore
    @Test
    public void testPostOrdersGenerateDST() throws Exception {
        // DST
        // - America-Anchorage
        // -- 2026: 2026-03-08,2026-11-01
        // -- 2027: 2027-03-14,2027-11-07
        // - Pacific-Norfolk
        // -- 2026: 2026-04-05,2026-10-04
        // -- 2027: 2027-04-04,2027-10-03
        // - Europe-Berlin
        // -- 2026: 2026-03-29,2026-10-25
        // -- 2027: 2027-03-28,2027-10-31
        String dailyPlanDate = "2026-03-08";
        PathItem item = new PathItem();
        item.getSingles().add("/dailyplan/dailyplan_DST/CLI-DailyPlan-WorkflowDST-America-Anchorage");
        // item.getSingles().add("/dailyplan/dailyplan_DST/CLI-DailyPlan-WorkflowDST-Pacific-Norfolk");
        // item.getSingles().add("/dailyplan/dailyplan_DST/CLI-DailyPlan-WorkflowDST-Europe-Berlin");

        // ------------------------------------------------------------------
        GenerateRequest in = new GenerateRequest();
        in.setControllerId("js7.x");
        in.setDailyPlanDate(dailyPlanDate);

        in.setSchedulePaths(item);
        in.setOverwrite(false);
        in.setWithSubmit(false);

        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new DailyPlanOrdersGenerateImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");
        try {
            h.init();

            h.post("postOrdersGenerate", in);

            TimeUnit.SECONDS.sleep(10);// sleep due to asynchronous call
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

}
