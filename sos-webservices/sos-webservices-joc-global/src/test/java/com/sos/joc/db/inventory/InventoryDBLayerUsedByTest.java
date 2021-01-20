package com.sos.joc.db.inventory;

import java.nio.file.Paths;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.db.DBLayer;

public class InventoryDBLayerUsedByTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryDBLayerUsedByTest.class);

    @Ignore
    @Test
    public void testUsedLocks() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        String lockId = "lock_1";
        try {
            factory = createFactory();
            session = factory.openStatelessSession();
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            session.beginTransaction();

            List<DBItemInventoryConfiguration> items = dbLayer.getUsedWorkflowsByLockId(lockId);

            LOGGER.info(String.format("[testUsedLocks][lockId=%s]found=%s", lockId, items.size()));
            for (DBItemInventoryConfiguration item : items) {
                LOGGER.info("---" + SOSHibernate.toString(item));
            }

            session.commit();
        } catch (Exception e) {
            try {
                session.rollback();
            } catch (Throwable ex) {
            }
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

    @Ignore
    @Test
    public void testUsedSchedules() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        String workflowPath = "/workflow1";
        String calendarPath = "/calendar1";
        try {
            factory = createFactory();
            session = factory.openStatelessSession();
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            session.beginTransaction();

            List<DBItemInventoryConfiguration> items = dbLayer.getUsedSchedulesByWorkflowPath(workflowPath);
            LOGGER.info(String.format("[testUsedSchedules][workflowPath=%s]found=%s", workflowPath, items.size()));
            for (DBItemInventoryConfiguration item : items) {
                LOGGER.info("---" + SOSHibernate.toString(item));
            }

            items = dbLayer.getUsedSchedulesByCalendarPath(calendarPath);
            LOGGER.info(String.format("[testUsedSchedules][calendarPath=%s]found=%s", calendarPath, items.size()));
            for (DBItemInventoryConfiguration item : items) {
                LOGGER.info("---" + SOSHibernate.toString(item));
            }

            session.commit();
        } catch (Exception e) {
            try {
                session.rollback();
            } catch (Throwable ex) {
            }
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
        factory.addClassMapping(DBLayer.getJocClassMapping());
        factory.build();
        return factory;
    }
}
