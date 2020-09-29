package com.sos.joc.db.inventory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.items.InventoryDeployablesTreeFolderItem;
import com.sos.joc.db.inventory.items.InventoryDeploymentItem;
import com.sos.joc.db.inventory.items.InventoryTreeFolderItem;
import com.sos.joc.model.inventory.common.ArgumentType;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class InventoryDBLayerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryDBLayerTest.class);

    private SOSHibernateFactory factory;

    @Ignore
    @Test
    public void testTreeFolder() throws Exception {
        SOSHibernateSession session = null;
        try {
            session = factory.openStatelessSession();
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            session.beginTransaction();

            List<InventoryTreeFolderItem> items = dbLayer.getConfigurationsByFolder("/", false, Arrays.asList(ConfigurationType.WORKFLOW.intValue()));
            for (InventoryTreeFolderItem item : items) {
                LOGGER.info(SOSString.toString(item));
            }
            session.commit();
        } catch (Exception e) {
            if (session != null && session.isTransactionOpened()) {
                session.rollback();
            }
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Ignore
    @Test
    public void testDeployablesTreeFolder() throws Exception {
        SOSHibernateSession session = null;
        try {
            session = factory.openStatelessSession();
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            session.beginTransaction();

            List<InventoryDeployablesTreeFolderItem> deployables = dbLayer.getConfigurationsWithMaxDeployment("/", true);
            for (InventoryDeployablesTreeFolderItem item : deployables) {
                LOGGER.info(SOSString.toString(item));
            }

            InventoryDeploymentItem lastDeployment = dbLayer.getLastDeploymentHistory(1L);
            LOGGER.info("lastDeployment:" + SOSString.toString(lastDeployment));

            session.commit();
        } catch (Exception e) {
            if (session != null && session.isTransactionOpened()) {
                session.rollback();
            }
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Ignore
    @Test
    public void testGetConfigurationProperties() throws Exception {
        SOSHibernateSession session = null;
        try {
            session = factory.openStatelessSession();
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            session.beginTransaction();

            Set<Long> ids = new HashSet<Long>();
            ids.add(1L);
            ids.add(2L);

            List<Object[]> items = dbLayer.getConfigurationProperties(ids, "id,type");
            for (Object[] item : items) {
                Long id = (Long) item[0];
                Integer type = (Integer) item[1];
                LOGGER.info(String.format("id=%s, type=%s", id, ConfigurationType.fromValue(type).name()));
            }

            session.commit();
        } catch (Exception e) {
            if (session != null && session.isTransactionOpened()) {
                session.rollback();
            }
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Ignore
    @Test
    public void testJocInventoryDeleteConfigurations() throws Exception {
        SOSHibernateSession session = null;
        try {
            session = factory.openStatelessSession();
            session.setAutoCommit(false);

            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            Set<Long> ids = new HashSet<Long>();
            ids.add(1L);
            /// ids.add(2L);

            if (ids != null && ids.size() > 0) {
                session.beginTransaction();
                dbLayer.deleteConfigurations(ids);
                session.commit();
            }
        } catch (Exception e) {
            if (session != null && session.isTransactionOpened()) {
                session.rollback();
            }
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Ignore
    @Test
    public void testGetConfigurationsWithMaxDeployment() throws Exception {
        SOSHibernateSession session = null;
        try {
            session = factory.openStatelessSession();
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            session.beginTransaction();

            List<InventoryDeployablesTreeFolderItem> items = dbLayer.getConfigurationsWithMaxDeployment("/", true);
            if (items != null) {
                for (InventoryDeployablesTreeFolderItem item : items) {
                    LOGGER.info(SOSString.toString(item));
                }
            }

            session.commit();
        } catch (Exception e) {
            if (session != null && session.isTransactionOpened()) {
                session.rollback();
            }
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Ignore
    @Test
    public void testSave() throws Exception {
        SOSHibernateSession session = null;
        try {
            session = factory.openStatelessSession();
            session.beginTransaction();

            DBItemInventoryWorkflowOrderVariable v = new DBItemInventoryWorkflowOrderVariable();
            v.setCidWorkflowOrder(10000000L);
            v.setName("test");
            v.setType(ArgumentType.STRING);
            v.setValue("val");
            session.save(v);

            int result = session.executeUpdate("delete from " + DBLayer.DBITEM_INV_WORKFLOW_ORDER_VARIABLES + " where cidWorkflowOrder=" + v
                    .getCidWorkflowOrder());

            LOGGER.info("deleted: " + result);

            session.commit();
        } catch (Exception e) {
            if (session != null && session.isTransactionOpened()) {
                session.rollback();
            }
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Before
    public void setUp() throws Exception {
        factory = new SOSHibernateFactory("./src/test/resources/hibernate.cfg.xml");
        factory.addClassMapping(DBLayer.getJocClassMapping());
        factory.build();
    }

    @After
    public final void tearDown() {
        if (factory != null) {
            factory.close();
        }

    }
}
