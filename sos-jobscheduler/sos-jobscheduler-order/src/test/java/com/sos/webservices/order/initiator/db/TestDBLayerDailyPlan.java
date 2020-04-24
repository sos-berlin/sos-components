package com.sos.webservices.order.initiator.db;

import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.db.orders.DBItemDailyPlanWithHistory;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.webservices.order.initiator.OrderInitiatorSettings;
import com.sos.webservices.order.initiator.classes.OrderInitiatorGlobals;

public class TestDBLayerDailyPlan {

	@Test
	public void testGetDailyPlanWithHistoryList() throws JocConfigurationException, DBConnectionRefusedException,
			SOSHibernateException, DBOpenSessionException {

		OrderInitiatorSettings orderInitiatorSettings = new OrderInitiatorSettings();
		orderInitiatorSettings.setHibernateConfigurationFile(Paths.get("src/test/resources/hibernate_jobscheduler2.cfg.xml"));
		OrderInitiatorGlobals.orderInitiatorSettings = orderInitiatorSettings;
		SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("OrderInitiatorRunner");

		FilterDailyPlan filter = new FilterDailyPlan();
		DBLayerDailyPlan dbLayer = new DBLayerDailyPlan(sosHibernateSession);
		List<DBItemDailyPlanWithHistory> l = dbLayer.getDailyPlanWithHistoryList(filter,0);
		System.out.println(l.get(0).getDbItemDailyPlan().getJobschedulerId());
 	}

}
