package com.sos.joc.inventory.impl;

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
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.InventoryMeta.ConfigurationType;
import com.sos.joc.db.inventory.items.InventoryDeployablesTreeFolderItem;
import com.sos.joc.model.inventory.common.ResponseItemDeployment;
import com.sos.joc.model.inventory.deploy.ResponseDeployableTreeItem;
import com.sos.joc.model.inventory.deploy.ResponseDeployableVersion;
import com.sos.joc.model.inventory.deploy.ResponseDeployables;

public class DeployablesResourceImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployablesResourceImplTest.class);

    private SOSHibernateFactory factory;

    @Ignore
    @Test
    public void testDeployablesTreeWithMaxDeployment() throws Exception {
        SOSHibernateSession session = null;
        try {
            session = factory.openStatelessSession();
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            session.beginTransaction();
            String folder = "/";
            List<InventoryDeployablesTreeFolderItem> list = dbLayer.getConfigurationsWithMaxDeployment(folder, true);
            session.commit();
            session = null;

            for (InventoryDeployablesTreeFolderItem item : list) {
                LOGGER.info(SOSString.toString(item));
            }

            DeployablesResourceImpl impl = new DeployablesResourceImpl();
            ResponseDeployables result = impl.getDeployables(list, folder, false);
            printTree(result);

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
    public void testDeployablesTreeWithAllDeployments() throws Exception {
        SOSHibernateSession session = null;
        try {
            JOCResourceImpl r = new JOCResourceImpl();
            String folder = r.normalizeFolder("/");

            session = factory.openStatelessSession();
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            session.beginTransaction();
            List<InventoryDeployablesTreeFolderItem> list = dbLayer.getConfigurationsWithAllDeployments(folder, ConfigurationType.WORKFLOW.value());
            session.commit();
            session = null;

            for (InventoryDeployablesTreeFolderItem item : list) {
                LOGGER.info(SOSString.toString(item));
            }

            DeployablesResourceImpl impl = new DeployablesResourceImpl();
            ResponseDeployables result = impl.getDeployables(list, folder, true);
            printTree(result);

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
    public void testReverseFolder() throws Exception {
        String folder = "/xxxx";

        String[] arr = folder.split("/");
        if (arr.length > 1) {
            String dir = folder;
            System.out.println("main)" + dir);
            for (int i = 2; i < arr.length; i++) {
                dir = folder.substring(0, dir.lastIndexOf("/"));
                System.out.println(i + ")" + dir);
            }
        }
    }

    private void printTree(ResponseDeployables result) {
        LOGGER.info("-----------------------------------------" + result.getDeployables().size());
        for (ResponseDeployableTreeItem item : result.getDeployables()) {
            LOGGER.info(String.format("[id=%s][deployed=%s][deploymentId=%s][folder=%s][name=%s][type=%s]", item.getId(), item.getDeployed(), item
                    .getDeploymentId(), item.getFolder(), item.getObjectName(), item.getObjectType()));
            for (ResponseDeployableVersion version : item.getDeployablesVersions()) {
                LOGGER.info(String.format("   [versionDate=%s][id=%s][deploymentId=%s]", version.getVersionDate(), version.getId(), version
                        .getDeploymentId()));

                for (ResponseItemDeployment id : version.getVersions()) {
                    LOGGER.info(String.format("       [controllerId=%s][version=%s]", id.getControllerId(), id.getVersion()));
                }
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
