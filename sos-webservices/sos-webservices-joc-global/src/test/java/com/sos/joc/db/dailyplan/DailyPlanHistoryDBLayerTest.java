package com.sos.joc.db.dailyplan;

import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.db.DBLayer;

public class DailyPlanHistoryDBLayerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanHistoryDBLayerTest.class);

    @Ignore
    @Test
    public void testGetDates() throws Exception {

        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = createFactory();
            session = factory.openStatelessSession();

            String controllerId = "js7.x";
            Date dateFrom = null;
            Boolean submitted = null;
            int limit = 0;

            DailyPlanHistoryDBLayer dbLayer = new DailyPlanHistoryDBLayer(session);
            List<Object[]> sr = dbLayer.getDates(controllerId, dateFrom, dateFrom, submitted, limit);
            int size = 0;
            for (int i = 0; i < sr.size(); i++) {
                Object[] item = (Object[]) sr.get(i);
                LOGGER.info(SOSHibernate.toString(item));
                size++;
            }
            LOGGER.info("SIZE=" + size);
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

    private SOSHibernateFactory createFactory() throws Exception {
        SOSHibernateFactory factory = new SOSHibernateFactory(Paths.get("src/test/resources/hibernate.cfg.xml"));
        factory.addClassMapping(DBLayer.getOrderInitatorClassMapping());
        factory.build();
        return factory;
    }

}
