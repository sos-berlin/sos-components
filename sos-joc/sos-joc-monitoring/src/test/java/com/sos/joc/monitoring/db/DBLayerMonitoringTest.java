package com.sos.joc.monitoring.db;

import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.joc.db.DBLayer;
import com.sos.monitoring.notification.NotificationRange;

public class DBLayerMonitoringTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerMonitoringTest.class);

    @Ignore
    @Test
    public void test() throws Exception {

        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = createFactory();
            session = factory.openStatelessSession();

            DBLayerMonitoring dbLayer = new DBLayerMonitoring("test", null);
            dbLayer.setSession(session);

            LOGGER.info(SOSString.toString(dbLayer.getLastNotification("1", NotificationRange.WORKFLOW, 663L)));

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
        factory.addClassMapping(DBLayer.getMonitoringClassMapping());
        factory.build();
        return factory;
    }

}
