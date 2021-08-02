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

public class InventorySearchDBLayerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventorySearchDBLayerTest.class);

    @Ignore
    @Test
    public void testBasicAllConfigurations() throws Exception {
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

            List<InventorySearchItem> items = dbLayer.getInventoryConfigurations(ConfigurationType.WORKFLOW, search, folders);
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

    private SOSHibernateFactory createFactory() throws Exception {
        SOSHibernateFactory factory = new SOSHibernateFactory(Paths.get("src/test/resources/hibernate.cfg.xml"));
        factory.addClassMapping(DBLayer.getJocClassMapping());
        factory.build();
        return factory;
    }
}
