package com.sos.joc.inventory.impl;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.inventory.common.ResponseItemDeployment;
import com.sos.joc.model.inventory.deploy.ResponseDeployableTreeItem;
import com.sos.joc.model.inventory.deploy.ResponseDeployableVersion;
import com.sos.joc.model.inventory.deploy.ResponseDeployables;

public class DeployablesResourceImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployablesResourceImplTest.class);

    private SOSHibernateFactory factory;

//    @Ignore
//    @Test
//    public void testDeployablesTreeWithMaxDeployment() throws Exception {
//        SOSHibernateSession session = null;
//        try {
//            session = factory.openStatelessSession();
//            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
//
//            session.beginTransaction();
//            String folder = "/";
//            List<InventoryDeployablesTreeFolderItem> list = dbLayer.getConfigurationsWithMaxDeployment(folder, true);
//            session.commit();
//            session = null;
//
//            for (InventoryDeployablesTreeFolderItem item : list) {
//                LOGGER.info(SOSString.toString(item));
//            }
//
//            DeployablesResourceImpl impl = new DeployablesResourceImpl();
//            ResponseDeployables result = impl.getDeployables(dbLayer, list, folder, false);
//            printTree(result);
//
//        } catch (Exception e) {
//            if (session != null && session.isTransactionOpened()) {
//                session.rollback();
//            }
//            throw e;
//        } finally {
//            if (session != null) {
//                session.close();
//            }
//        }
//    }

//    @Ignore
//    @Test
//    public void testDeployablesTreeWithAllDeployments() throws Exception {
//        SOSHibernateSession session = null;
//        try {
//            JOCResourceImpl r = new JOCResourceImpl();
//            String folder = r.normalizeFolder("/");
//
//            session = factory.openStatelessSession();
//            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
//
//            session.beginTransaction();
//            List<InventoryDeployablesTreeFolderItem> list = dbLayer.getConfigurationsWithAllDeployments(folder, ConfigurationType.WORKFLOW.intValue());
//            session.commit();
//            session = null;
//
//            for (InventoryDeployablesTreeFolderItem item : list) {
//                LOGGER.info(SOSString.toString(item));
//            }
//
//            DeployablesResourceImpl impl = new DeployablesResourceImpl();
//            ResponseDeployables result = impl.getDeployables(dbLayer, list, folder, true);
//            printTree(result);
//
//        } catch (Exception e) {
//            if (session != null && session.isTransactionOpened()) {
//                session.rollback();
//            }
//            throw e;
//        } finally {
//            if (session != null) {
//                session.close();
//            }
//        }
//    }

    private void printTree(ResponseDeployables result) {
        LOGGER.info("-----------------------------------------" + result.getDeployables().size());
        for (ResponseDeployableTreeItem item : result.getDeployables()) {
            LOGGER.info(String.format("[id=%s][deployed=%s][folder=%s][name=%s][type=%s]", item.getId(), item.getDeployed(), item.getFolder(), item.getObjectName(), item.getObjectType()));
            for (ResponseDeployableVersion version : item.getDeployablesVersions()) {
                LOGGER.info(String.format("   [versionDate=%s][id=%s][deploymentId=%s]", version.getVersionDate(), version.getId(), version
                        .getDeploymentId()));
                if (version.getVersions() != null) {
                    for (ResponseItemDeployment id : version.getVersions()) {
                        LOGGER.info(String.format("       [controllerId=%s][version=%s]", id.getControllerId(), id.getVersion()));
                    }
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
