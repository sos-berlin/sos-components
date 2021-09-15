package com.sos.joc.db.inventory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.items.InventorySearchItem;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.search.RequestSearchAdvancedItem;

public class InventorySearchDBLayerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventorySearchDBLayerTest.class);

    @Ignore
    @Test
    public void testBasicSearchInventoryConfigurations() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            String search = null;
            List<String> folders = new ArrayList<>();
            folders.add("/x");
            folders.add("/ssh");

            factory = createFactory();
            session = factory.openStatelessSession();
            InventorySearchDBLayer dbLayer = new InventorySearchDBLayer(session);
            session.beginTransaction();

            List<InventorySearchItem> items = dbLayer.getBasicSearchInventoryConfigurations(ConfigurationType.WORKFLOW, search, folders);
            LOGGER.info("RESULT=" + items.size());
            for (InventorySearchItem item : items) {
                LOGGER.info(SOSString.toString(item));
            }

            session.commit();
        } catch (Exception e) {
            try {
                session.rollback();
            } catch (Throwable ex) {
            }
            e.printStackTrace();
            // throw e;
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
    public void testAdvancedSearchInventoryConfigurations() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            String search = null;
            List<String> folders = new ArrayList<>();
            // folders.add("/x");
            // folders.add("/ssh");

            RequestSearchAdvancedItem advanced = new RequestSearchAdvancedItem();
            // advanced.setJobCountFrom(Integer.valueOf(2));
            advanced.setJobName("Job");
            advanced.setJobNameExactMatch(true);
            advanced.setAgentName(null);
            // advanced.setJobCriticality(JobCriticality.NORMAL);
            advanced.setJobResources(null);
            advanced.setNoticeBoards(null);
            advanced.setLock(null);
            advanced.setArgumentName(null);
            advanced.setArgumentValue(null);
            advanced.setWorkflow("shell");

            factory = createFactory();
            session = factory.openStatelessSession();
            InventorySearchDBLayer dbLayer = new InventorySearchDBLayer(session);
            session.beginTransaction();

            List<InventorySearchItem> items = dbLayer.getAdvancedSearchInventoryConfigurations(ConfigurationType.WORKFLOW, search, folders, advanced);
            // List<InventorySearchItem> items = dbLayer.getAdvancedSearchDeployedOrReleasedConfigurations(ConfigurationType.WORKFLOW, search, folders,
            // advanced,"js7.x");
            LOGGER.info("RESULT=" + items.size());
            for (InventorySearchItem item : items) {
                LOGGER.info(SOSString.toString(item));
            }

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
