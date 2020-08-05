package com.sos.joc.inventory.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.InventoryMeta.ConfigurationType;
import com.sos.joc.db.inventory.items.InventoryDeployablesTreeFolderItem;
import com.sos.joc.db.inventory.items.InventoryDeploymentItem;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IDeployablesResource;
import com.sos.joc.model.common.JobSchedulerId;
import com.sos.joc.model.common.JobSchedulerObjectType;
import com.sos.joc.model.inventory.common.Item;
import com.sos.joc.model.inventory.common.ItemDeployment;
import com.sos.joc.model.inventory.deploy.DeployableTreeItem;
import com.sos.joc.model.inventory.deploy.DeployableVersion;
import com.sos.joc.model.inventory.deploy.Deployables;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class DeployablesResourceImpl extends JOCResourceImpl implements IDeployablesResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployablesResourceImpl.class);

    @Override
    public JOCDefaultResponse deployables(final String accessToken, final byte[] inBytes) {
        try {
            JsonValidator.validateFailFast(inBytes, Item.class);
            Item in = Globals.objectMapper.readValue(inBytes, Item.class);

            JOCDefaultResponse response = checkPermissions(accessToken, inBytes);
            if (response == null) {
                if (in.getId() == null) {
                    response = JOCDefaultResponse.responseStatus200(deployables());
                } else {
                    response = JOCDefaultResponse.responseStatus200(deploayblesVersions(in.getId()));
                }
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private Deployables deployables() throws Exception {
        Deployables result = new Deployables();
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            session.beginTransaction();
            List<InventoryDeployablesTreeFolderItem> list = dbLayer.getDeployablesConfigurationsWithMaxDeployment();
            session.commit();

            if (list == null || list.size() == 0) {
                result.setDeployables(new HashSet<DeployableTreeItem>());
            } else {
                result.setDeployables(list.stream().filter(config -> {
                    ConfigurationType type = ConfigurationType.fromValue(config.getType());
                    if (type.equals(ConfigurationType.FOLDER)) {
                        return false;
                    } else {
                        JobSchedulerObjectType.fromValue(type.name());// throws exception if not exists
                        if (!folderPermissions.isPermittedForFolder(getParent(config.getPath()))) {
                            LOGGER.info(String.format("[skip][%s]due to folder permissions", config.getPath()));
                            return false;
                        }
                    }
                    return true;
                }).map(config -> {
                    DeployableTreeItem item = new DeployableTreeItem();
                    // item.setAccount(config.getAccount());
                    item.setId(config.getId());
                    item.setFolder(getParent(config.getPath()));
                    item.setModified(config.getModified());
                    item.setObjectName(config.getName());
                    item.setObjectType(JobSchedulerObjectType.fromValue(ConfigurationType.fromValue(config.getType()).name()));
                    item.setDeployed(config.getDeployed());
                    if (config.getDeployed() && config.getLastDeployment() != null) {
                        ItemDeployment d = new ItemDeployment();
                        d.setDeploymentId(config.getLastDeployment().getId());
                        d.setVersion(config.getLastDeployment().getVersion());
                        d.setDeploymentDate(config.getLastDeployment().getDeploymentDate());
                        d.setControllerId(config.getLastDeployment().getControllerId());
                        item.setDeployment(d);
                    }
                    return item;
                }).collect(Collectors.toSet()));
            }

            return result;
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private Deployables deploayblesVersions(Long configId) throws Exception {
        Deployables result = new Deployables();
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            session.beginTransaction();
            Object[] config = dbLayer.getConfigurationProperties(configId, "deployed,modified");
            if (config == null) {
                throw new Exception(String.format("no configuration found for id=%s", configId));
            }
            List<InventoryDeploymentItem> deployments = dbLayer.getDeploymentHistory(configId);
            session.commit();

            if (!((Boolean) config[0])) {// deployed
                DeployableVersion draft = new DeployableVersion();
                draft.setId(1l);
                draft.setFolder((Date) config[1]);// modified
                result.getDeployablesVersions().add(draft);
            }
            if (deployments != null && deployments.size() > 0) {
                Collections.sort(deployments, new Comparator<InventoryDeploymentItem>() {

                    public int compare(InventoryDeploymentItem d1, InventoryDeploymentItem d2) {// deploymentDate descending
                        return d2.getDeploymentDate().compareTo(d1.getDeploymentDate());
                    }
                });

                Date date = null;
                DeployableVersion dv = null;
                for (InventoryDeploymentItem deployment : deployments) {
                    if (date == null || !date.equals(deployment.getDeploymentDate())) {
                        dv = new DeployableVersion();
                        dv.setId(configId);
                        dv.setFolder(deployment.getDeploymentDate());
                        dv.setDeploymentId(deployment.getId());
                        result.getDeployablesVersions().add(dv);
                    }
                    ItemDeployment id = new ItemDeployment();
                    id.setVersion(deployment.getVersion());
                    id.setControllerId(deployment.getControllerId());
                    dv.getDeployments().add(id);

                    date = deployment.getDeploymentDate();
                }
            }

            return result;
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, final byte[] inBytes) throws Exception {
        // TODO check jobscheduler???
        JobSchedulerId in = Globals.objectMapper.readValue(inBytes, JobSchedulerId.class);
        SOSPermissionJocCockpit permissions = getPermissonsJocCockpit(in.getJobschedulerId(), accessToken);
        boolean permission = permissions.getJobschedulerMaster().getAdministration().getConfigurations().getDeploy().isWorkflow() || permissions
                .getJobschedulerMaster().getAdministration().getConfigurations().getDeploy().isLock();
        // TODO extends to isAgentCluster, isJunction etc

        return init(IMPL_PATH, in, accessToken, in.getJobschedulerId(), permission);
    }

}
