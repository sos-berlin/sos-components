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
            if (in.getPath() != null) {
                in.setPath(normalizeFolder(in.getPath()));
            }
            JOCDefaultResponse response = checkPermissions(accessToken, inBytes);
            if (response == null) {
                response = JOCDefaultResponse.responseStatus200(deployables(in));
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private Deployables deployables(Item in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            session.beginTransaction();
            List<InventoryDeployablesTreeFolderItem> list = null;
            if (in.getPath() == null && in.getObjectType() == null) {
                list = dbLayer.getConfigurationsWithMaxDeployment();
            } else {
                list = dbLayer.getConfigurationsWithAllDeployments(in.getPath(), in.getObjectType() == null ? null : JocInventory.getType(in
                        .getObjectType()));
            }
            session.commit();
            session = null;

            return getDeployables(list);
        } catch (Throwable e) {
            if (session != null && session.isTransactionOpened()) {
                Globals.rollback(session);
            }
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    public Deployables getDeployables(List<InventoryDeployablesTreeFolderItem> list) throws Exception {
        Deployables result = new Deployables();
        if (list == null || list.size() == 0) {
            result.setDeliveryDate(new Date());
            result.setDeployables(new HashSet<DeployableTreeItem>());
            return result;
        }

        Long configId = 0L;
        DeployableTreeItem treeItem = null;
        for (InventoryDeployablesTreeFolderItem item : list) {
            if (configId.equals(item.getId())) {
                continue;
            }

            ConfigurationType type = ConfigurationType.fromValue(item.getType());
            if (type.equals(ConfigurationType.FOLDER)) {
                continue;
            }

            JobSchedulerObjectType.fromValue(type.name());// throws exception if not exists
            if (folderPermissions != null && !folderPermissions.isPermittedForFolder(getParent(item.getPath()))) {
                LOGGER.info(String.format("[skip][%s]due to folder permissions", item.getPath()));
                continue;
            }

            treeItem = new DeployableTreeItem();
            treeItem.setId(item.getId());
            treeItem.setFolder(item.getFolder());
            treeItem.setObjectName(item.getName());
            treeItem.setObjectType(JobSchedulerObjectType.fromValue(ConfigurationType.fromValue(item.getType()).name()));
            treeItem.setDeployed(item.getDeployed());

            if (item.getDeployment() != null) {
                List<InventoryDeployablesTreeFolderItem> deployments = getDeployments(list, treeItem.getId());

                Collections.sort(deployments, new Comparator<InventoryDeployablesTreeFolderItem>() {

                    public int compare(InventoryDeployablesTreeFolderItem d1, InventoryDeployablesTreeFolderItem d2) {// deploymentDate descending
                        return d2.getDeployment().getDeploymentDate().compareTo(d1.getDeployment().getDeploymentDate());
                    }
                });

                if (treeItem.getDeployed()) {
                    treeItem.setDeploymentId(deployments.get(0).getDeployment().getId());
                } else {
                    DeployableVersion draft = new DeployableVersion();
                    draft.setId(item.getId());
                    draft.setVersionDate(item.getModified());
                    treeItem.getDeployablesVersions().add(draft);
                }

                Date date = null;
                DeployableVersion dv = null;
                for (InventoryDeployablesTreeFolderItem deployment : deployments) {
                    if (date == null || !date.equals(deployment.getDeployment().getDeploymentDate())) {
                        dv = new DeployableVersion();
                        dv.setId(deployment.getId());
                        dv.setVersionDate(deployment.getDeployment().getDeploymentDate());
                        dv.setDeploymentId(deployment.getDeployment().getId());
                        treeItem.getDeployablesVersions().add(dv);
                    }
                    ItemDeployment id = new ItemDeployment();
                    id.setVersion(deployment.getDeployment().getVersion());
                    id.setControllerId(deployment.getDeployment().getControllerId());
                    dv.getVersions().add(id);

                    date = deployment.getDeployment().getDeploymentDate();
                }

            }
            result.getDeployables().add(treeItem);
            configId = item.getId();
        }

        result.setDeliveryDate(new Date());
        return result;
    }

    private List<InventoryDeployablesTreeFolderItem> getDeployments(List<InventoryDeployablesTreeFolderItem> items, final Long configId) {
        return items.stream().filter(config -> {
            if (config.getId().equals(configId) && config.getDeployment() != null) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
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
