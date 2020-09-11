package com.sos.joc.inventory.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.items.InventoryDeploymentItem;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IReadConfigurationResource;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ItemStateEnum;
import com.sos.joc.model.inventory.deploy.ResponseDeployableVersion;
import com.sos.joc.model.inventory.read.configuration.RequestFilter;
import com.sos.joc.model.publish.OperationType;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class ReadConfigurationResourceImpl extends JOCResourceImpl implements IReadConfigurationResource {

    @Override
    public JOCDefaultResponse read(final String accessToken, final byte[] inBytes) {
        try {
            JsonValidator.validateFailFast(inBytes, RequestFilter.class);
            RequestFilter in = Globals.objectMapper.readValue(inBytes, RequestFilter.class);

            checkRequiredParameter("id", in.getId());

            JOCDefaultResponse response = checkPermissions(accessToken, in);
            if (response == null) {
                response = read(in);
            }
            return response;

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private JOCDefaultResponse read(RequestFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            List<InventoryDeploymentItem> deployments = null;
            InventoryDeploymentItem lastDeployment = null;

            session.beginTransaction();
            DBItemInventoryConfiguration config = dbLayer.getConfiguration(in.getId());
            if (config != null) {
                deployments = dbLayer.getDeploymentHistory(config.getId());
            }
            session.commit();

            if (config == null) {
                throw new DBMissingDataException(String.format("configuration not found: %s", SOSString.toString(in)));
            }

            if (!folderPermissions.isPermittedForFolder(config.getFolder())) {
                return accessDeniedResponse();
            }

            if (deployments != null && deployments.size() > 0) {
                Collections.sort(deployments, new Comparator<InventoryDeploymentItem>() {

                    public int compare(InventoryDeploymentItem d1, InventoryDeploymentItem d2) {// deploymentDate descending
                        return d2.getDeploymentDate().compareTo(d1.getDeploymentDate());
                    }
                });
                lastDeployment = deployments.get(0);
            }

            ConfigurationObject item = new ConfigurationObject();
            item.setId(config.getId());
            item.setDeliveryDate(new Date());
            item.setPath(config.getPath());
            item.setObjectType(JocInventory.getType(config.getType()));
            item.setValid(config.getValide());
            item.setDeleted(config.getDeleted());

            if (config.getDeployed()) {
                if (lastDeployment == null) {
                    throw new DBMissingDataException(String.format("[id=%s][%s][%s]deployments not found", in.getId(), config.getPath(), config
                            .getTypeAsEnum().name()));
                }

                item.setState(ItemStateEnum.DRAFT_NOT_EXIST);
                item.setConfigurationDate(lastDeployment.getDeploymentDate());
                item.setConfiguration(JocInventory.content2IJSObject(lastDeployment.getContent(), JocInventory.getType(config.getType())));
                item.setDeployed(true);

                // ResponseItemDeployment d = new ResponseItemDeployment();
                // d.setDeploymentId(lastDeployment.getId());
                // d.setVersion(lastDeployment.getVersion());
                // d.setDeploymentDate(lastDeployment.getDeploymentDate());
                // d.setControllerId(lastDeployment.getControllerId());
                // d.setPath(lastDeployment.getPath());
                // item.setDeployment(d);

            } else {
                item.setConfigurationDate(config.getModified());
                item.setConfiguration(JocInventory.content2IJSObject(config.getContent(), JocInventory.getType(config.getType())));
                item.setDeployed(false);

                if (lastDeployment == null) {
                    item.setState(ItemStateEnum.DEPLOYMENT_NOT_EXIST);
                } else {
                    // ResponseItemDeployment d = new ResponseItemDeployment();
                    // d.setDeploymentId(lastDeployment.getId());
                    // d.setVersion(lastDeployment.getVersion());
                    // d.setDeploymentDate(lastDeployment.getDeploymentDate());
                    // d.setControllerId(lastDeployment.getControllerId());
                    // d.setPath(lastDeployment.getPath());
                    if (lastDeployment.getDeploymentDate().after(config.getModified())) {
                        item.setState(ItemStateEnum.DEPLOYMENT_IS_NEWER);
                    } else {
                        item.setState(ItemStateEnum.DRAFT_IS_NEWER);
                    }
                    // item.setDeployment(d);
                }
            }
            if (lastDeployment != null) {
                for (InventoryDeploymentItem d : deployments) {
                    ResponseDeployableVersion v = new ResponseDeployableVersion();
                    v.setId(config.getId());
                    v.setDeploymentId(d.getId());
                    v.setDeploymentOperation(OperationType.fromValue(d.getOperation()).name().toLowerCase());
                    v.setVersionDate(d.getDeploymentDate());
                    item.getDeployments().add(v);
                }
            } else {
                item.setDeployments(null);
            }
            
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(item));
        } catch (Throwable e) {
            if (session != null && session.isTransactionOpened()) {
                Globals.rollback(session);
            }
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, final RequestFilter in) throws Exception {
        SOSPermissionJocCockpit permissions = getPermissonsJocCockpit("", accessToken);
        boolean permission = permissions.getInventory().getConfigurations().isEdit();
        return init(IMPL_PATH, in, accessToken, "", permission);
    }

}
