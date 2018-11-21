package com.sos.webservices.order.initiator.db;

import static org.junit.Assert.*;

import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.db.orders.DBItemDailyPlanWithHistory;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.webservices.order.initiator.OrderInitiatorSettings;
import com.sos.webservices.order.initiator.classes.Globals;

public class TestDBLayerDailyPlan {

	@Test
	public void testGetDailyPlanWithHistoryList() throws JocConfigurationException, DBConnectionRefusedException, SOSHibernateException {

		OrderInitiatorSettings orderInitiatorSettings = new OrderInitiatorSettings();
		orderInitiatorSettings.setHibernateConfigurationFile(Paths.get("src/test/resources/hibernate_jobscheduler2.cfg.xml"));
		Globals.orderInitiatorSettings = orderInitiatorSettings;
		SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("OrderInitiatorRunner");

		DBLayerDailyPlan dbLayer = new DBLayerDailyPlan(sosHibernateSession);
		 List<DBItemDailyPlanWithHistory> l = dbLayer.getDailyPlanWithHistoryList(0);
		 System.out.println(l.get(0).getDbItemDailyPlan().getMasterId());
 	}

}
