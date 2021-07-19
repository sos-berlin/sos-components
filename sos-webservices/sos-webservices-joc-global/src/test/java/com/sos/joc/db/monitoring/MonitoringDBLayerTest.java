package com.sos.joc.db.monitoring;

import java.nio.file.Paths;
import java.util.Date;

import org.hibernate.ScrollableResults;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.db.DBLayer;

public class MonitoringDBLayerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringDBLayerTest.class);

    @Ignore
    @Test
    public void test() throws Exception {

        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        ScrollableResults sr = null;
        try {
            factory = createFactory();
            session = factory.openStatelessSession();

            Date dateFrom = null;
            String controllerId = "js7.x";
            Integer limit = null;

            MonitoringDBLayer dbLayer = new MonitoringDBLayer(session);
            sr = dbLayer.getNotifications(dateFrom, controllerId, null, limit);
            int size = 0;
            while (sr.next()) {
                NotificationDBItemEntity item = (NotificationDBItemEntity) sr.get(0);
                LOGGER.info(SOSHibernate.toString(item));
                size++;
            }
            LOGGER.info("SIZE=" + size);
        } catch (Exception e) {
            throw e;
        } finally {
            if (sr != null) {
                sr.close();
            }
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
        factory.addClassMapping(DBLayer.getMonitoringClassMapping());
        factory.build();
        return factory;
    }

}
