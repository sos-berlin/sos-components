package com.sos.joc.dailyplan.impl;

import java.util.HashSet;
import java.util.Set;
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
    public void testPostOrdersGenerate() throws Exception {
        String controllerId = "js7.x";
        controllerId = "js7-2.8.x-52899";

        Set<String> dailyPlanDates = new HashSet<>();
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
        dailyPlanDates.add("2026-09-12");

        // Schedule or Workflow paths
        PathItem item = new PathItem();
        // item.getSingles().add("/dailyplan/dailyplan_DST/CLI-DailyPlan-WorkflowDST-America-Anchorage");
        // item.getSingles().add("/dailyplan/dailyplan_DST/CLI-DailyPlan-WorkflowDST-Pacific-Norfolk");
        // item.getSingles().add("/dailyplan/dailyplan_DST/CLI-DailyPlan-WorkflowDST-Europe-Berlin");
        item.getSingles().add("/JOC-ISSUE/JOC-2280/JOC-2280-w1");

        // ------------------------------------------------------------------
        GenerateRequest in = new GenerateRequest();
        in.setControllerId(controllerId);
        in.setDailyPlanDates(dailyPlanDates);

        in.setSchedulePaths(null);
        in.setWorkflowPaths(item);
        in.setOverwrite(false);
        in.setWithSubmit(false);

        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new DailyPlanOrdersGenerateImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.pgsql.xml");
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
