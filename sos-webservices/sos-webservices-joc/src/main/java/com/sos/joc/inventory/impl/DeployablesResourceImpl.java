package com.sos.joc.inventory.impl;

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
import com.sos.joc.model.inventory.deploy.Deployable;
import com.sos.joc.model.inventory.deploy.Deployables;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class DeployablesResourceImpl extends JOCResourceImpl implements IDeployablesResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployablesResourceImpl.class);

    @Override
    public JOCDefaultResponse deployables(final String accessToken, final byte[] inBytes) {
        try {
            JsonValidator.validateFailFast(inBytes, Item.class);

            JOCDefaultResponse response = checkPermissions(accessToken, inBytes);
            if (response == null) {
                response = JOCDefaultResponse.responseStatus200(deployables());
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
                result.setDeployables(new HashSet<Deployable>());
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
                    Deployable item = new Deployable();
                    // item.setAccount(config.getAccount());
                    item.setId(config.getId());
                    item.setFolder(getParent(config.getPath()));
                    item.setModified(config.getModified());
                    item.setObjectName(config.getName());
                    item.setObjectType(JobSchedulerObjectType.fromValue(ConfigurationType.fromValue(config.getType()).name()));
                    if (config.getDeployed() && config.getDeployment() != null) {
                        ItemDeployment d = new ItemDeployment();
                        d.setId(config.getDeployment().getId());
                        d.setVersion(config.getDeployment().getVersion());
                        d.setDeploymentDate(config.getDeployment().getDeploymentDate());
                        d.setControllerId(config.getDeployment().getControllerId());
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
