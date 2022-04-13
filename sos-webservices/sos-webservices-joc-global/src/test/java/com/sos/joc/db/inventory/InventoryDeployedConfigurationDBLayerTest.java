package com.sos.joc.db.inventory;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class InventoryDeployedConfigurationDBLayerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryDeployedConfigurationDBLayerTest.class);

    @Ignore
    @Test
    public void testReleasedConfigurations() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            String controllerId = "js7.x";
            Set<Folder> folders = new HashSet<>();
            folders.add(newFolder("/x", true));
            folders.add(newFolder("/y", false));
            folders.add(newFolder("/z", false));

            factory = createFactory();
            session = factory.openStatelessSession();
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(session);

            Map<ConfigurationType, Long> result = dbLayer.getNumOfReleasedObjects(controllerId, folders);
            LOGGER.info("RESULT=" + result);
        } catch (Exception e) {
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

    private Folder newFolder(String path, boolean recursive) {
        Folder f = new Folder();
        f.setFolder(path);
        f.setRecursive(recursive);
        return f;
    }

    private SOSHibernateFactory createFactory() throws Exception {
        SOSHibernateFactory factory = new SOSHibernateFactory(Paths.get("src/test/resources/hibernate.cfg.xml"));
        factory.addClassMapping(DBLayer.getJocClassMapping());
        factory.build();
        return factory;
    }
}
