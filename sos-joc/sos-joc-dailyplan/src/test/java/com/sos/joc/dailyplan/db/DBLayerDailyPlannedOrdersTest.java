package com.sos.joc.dailyplan.db;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.dailyplan.DBItemDailyPlanWithHistory;
import com.sos.joc.model.dailyplan.DailyPlanOrderStateText;

public class DBLayerDailyPlannedOrdersTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerDailyPlannedOrdersTest.class);

    @Ignore
    @Test
    public void testGetDailyPlanWithHistoryList() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;

        FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
        filter.setControllerId("js7.x");
        filter.setSubmissionForDate(getSubmissionDate(2022, 10, 12));
        filter.setStartMode(0);

        List<DailyPlanOrderStateText> states = new ArrayList<>();
        states.add(DailyPlanOrderStateText.SUBMITTED);
        filter.setStates(states);
        filter.setLate(true);
        int limit = 1_000;

        try {
            factory = createFactory();
            session = factory.openStatelessSession();
            DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);

            List<DBItemDailyPlanWithHistory> items = dbLayer.getDailyPlanWithHistoryList(filter, limit);
            for (DBItemDailyPlanWithHistory item : items) {
                LOGGER.info(SOSString.toString(item));
            }
            LOGGER.info("SIZE=" + items.size());
        } catch (Exception e) {
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
            if (factory != null) {
                factory.close();
            }
        }
    }

    private Date getSubmissionDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, 0, 0, 0);
        return cal.getTime();
    }

    private SOSHibernateFactory createFactory() throws Exception {
        SOSHibernateFactory factory = new SOSHibernateFactory(Paths.get("src/test/resources/hibernate.cfg.xml"));
        factory.addClassMapping(DBLayer.getJocClassMapping());
        factory.build();
        return factory;
    }
}
