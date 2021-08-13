package com.sos.joc.db.inventory;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.items.InventoryDeploymentItem;
import com.sos.joc.db.inventory.items.InventoryTreeFolderItem;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class InventoryDBLayerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryDBLayerTest.class);

    @Ignore
    @Test
    public void testTreeFolder() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = createFactory();
            session = factory.openStatelessSession();
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            session.beginTransaction();

            List<InventoryTreeFolderItem> items = dbLayer.getConfigurationsByFolder("/", false, Arrays.asList(ConfigurationType.WORKFLOW.intValue()),
                    false);
            for (InventoryTreeFolderItem item : items) {
                LOGGER.info(SOSString.toString(item));
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

    @Test
    public void testGetLastDeploymentHistory() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = createFactory();
            session = factory.openStatelessSession();
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            session.beginTransaction();

            Long configId = 100L;

            InventoryDeploymentItem item = dbLayer.getLastDeploymentHistory(configId);
            LOGGER.info(SOSString.toString(item));

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
    public void testGetConfigurationsByFolder() {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = createFactory();
            session = factory.openStatelessSession();
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            session.beginTransaction();

            String folder = "/";
            boolean recursive = true;
            Collection<Integer> configTypes = null;// Collections.singletonList(ConfigurationType.WORKFLOW.intValue());
            Boolean onlyValidObjects = false;
            boolean forTrash = false;

            List<InventoryTreeFolderItem> items = dbLayer.getConfigurationsByFolder(folder, recursive, configTypes, onlyValidObjects, forTrash);
            for (InventoryTreeFolderItem item : items) {
                LOGGER.info(SOSString.toString(item.toResponseFolderItem()));
            }
            LOGGER.info("SIZE=" + items.size());
            session.commit();
        } catch (Exception e) {
            try {
                session.rollback();
            } catch (Throwable ex) {
            }
            e.printStackTrace();
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
    public void testGetConfigurationsWithAllDeployments() {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = createFactory();
            session = factory.openStatelessSession();
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            session.beginTransaction();

            List<Long> configIds = Collections.singletonList(100L);

            Map<DBItemInventoryConfiguration, Set<InventoryDeploymentItem>> map = dbLayer.getConfigurationsWithAllDeployments(configIds);
            for (Map.Entry<DBItemInventoryConfiguration, Set<InventoryDeploymentItem>> entry : map.entrySet()) {
                LOGGER.info(SOSString.toString(entry.getKey()));

            }
            // for (InventoryDeployablesTreeFolderItem item : items) {
            // LOGGER.info(SOSString.toString(item));
            // }
            LOGGER.info("SIZE=" + map.size());
            session.commit();
        } catch (Exception e) {
            try {
                session.rollback();
            } catch (Throwable ex) {
            }
            e.printStackTrace();
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
