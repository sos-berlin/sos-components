package com.sos.joc.dailyplan.db;

import java.nio.file.Paths;
import java.util.List;
import java.util.TimeZone;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.dailyplan.common.DailyPlanSettings;
import com.sos.joc.db.dailyplan.DBItemDailyPlanWithHistory;
import com.sos.joc.exceptions.JocException;

// Test fails in nightly build
@Ignore
public class TestDBLayerDailyPlan {

    @BeforeClass
    public static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
    }

    @Test
    public void testGetDailyPlanWithHistoryList() throws SOSHibernateException, JocException {
        Globals.sosCockpitProperties = new JocCockpitProperties();
        Globals.setProperties();
        DailyPlanSettings settings = new DailyPlanSettings();
        settings.setHibernateConfigurationFile(Paths.get("src/test/resources/hibernate_jobscheduler2.cfg.xml"));
        SOSHibernateSession session = Globals.createSosHibernateStatelessConnection("OrderInitiatorRunner");

        FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
        DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
        List<DBItemDailyPlanWithHistory> l = dbLayer.getDailyPlanWithHistoryList(filter, 0);
        System.out.println(l.get(0).getControllerId());
    }

}
