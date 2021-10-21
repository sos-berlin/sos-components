package com.sos.js7.order.initiator.db;

import java.nio.file.Paths;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.db.dailyplan.DBItemDailyPlanWithHistory;
import com.sos.joc.exceptions.JocException;
import com.sos.js7.order.initiator.OrderInitiatorSettings;
 
// Test fails in nightly build
@Ignore
public class TestDBLayerDailyPlan {

	@Test
	public void testGetDailyPlanWithHistoryList() throws SOSHibernateException, JocException {
        Globals.sosCockpitProperties = new JocCockpitProperties();
        Globals.setProperties();
		OrderInitiatorSettings orderInitiatorSettings = new OrderInitiatorSettings();
		orderInitiatorSettings.setHibernateConfigurationFile(Paths.get("src/test/resources/hibernate_jobscheduler2.cfg.xml"));
		SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("OrderInitiatorRunner");

		FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
		DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(sosHibernateSession);
		List<DBItemDailyPlanWithHistory> l = dbLayer.getDailyPlanWithHistoryList(filter,0);
		System.out.println(l.get(0).getControllerId());
 	}

}
