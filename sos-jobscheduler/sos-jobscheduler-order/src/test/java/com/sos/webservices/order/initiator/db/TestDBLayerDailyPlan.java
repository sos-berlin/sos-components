package com.sos.webservices.order.initiator.db;

import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.db.orders.DBItemDailyPlanWithHistory;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.webservices.order.initiator.OrderInitiatorSettings;
import com.sos.webservices.order.initiator.classes.OrderInitiatorGlobals;

public class TestDBLayerDailyPlan {

	@Test
	public void testGetDailyPlanWithHistoryList() throws SOSHibernateException, JocException {
        Globals.sosCockpitProperties = new JocCockpitProperties();
        Globals.setProperties();
		OrderInitiatorSettings orderInitiatorSettings = new OrderInitiatorSettings();
		orderInitiatorSettings.setHibernateConfigurationFile(Paths.get("src/test/resources/hibernate_jobscheduler2.cfg.xml"));
		OrderInitiatorGlobals.orderInitiatorSettings = orderInitiatorSettings;
		SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("OrderInitiatorRunner");

		FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
		DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(sosHibernateSession);
		List<DBItemDailyPlanWithHistory> l = dbLayer.getDailyPlanWithHistoryList(filter,0);
		System.out.println(l.get(0).getDbItemDailyPlannedOrders().getJobschedulerId());
 	}

}
