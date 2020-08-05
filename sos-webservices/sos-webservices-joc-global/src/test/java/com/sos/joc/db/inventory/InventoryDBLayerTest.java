package com.sos.joc.db.inventory;

import java.util.List;

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
import com.sos.joc.db.inventory.InventoryMeta.ArgumentType;
import com.sos.joc.db.inventory.InventoryMeta.ConfigurationType;
import com.sos.joc.db.inventory.items.InventoryDeployablesTreeFolderItem;
import com.sos.joc.db.inventory.items.InventoryDeploymentItem;
import com.sos.joc.db.inventory.items.InventoryTreeFolderItem;

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

            List<InventoryTreeFolderItem> items = dbLayer.getConfigurationsByFolder("/", false, ConfigurationType.WORKFLOW.value(), null);
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

            List<InventoryDeployablesTreeFolderItem> deployables = dbLayer.getConfigurationsWithMaxDeployment();
            for (InventoryDeployablesTreeFolderItem item : deployables) {
                LOGGER.info(SOSString.toString(item));
            }

            InventoryDeploymentItem lastDeployment = dbLayer.getLastDeploymentHistory(1L, ConfigurationType.WORKFLOW.value());
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
