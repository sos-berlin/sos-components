package com.sos.joc.inventory.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
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
import com.sos.joc.model.inventory.common.RequestFilter;
import com.sos.joc.model.inventory.deploy.ResponseDeployableVersion;
import com.sos.joc.model.publish.OperationType;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class ReadConfigurationResourceImpl extends JOCResourceImpl implements IReadConfigurationResource {

    @Override
    public JOCDefaultResponse read(final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFilter.class);
            RequestFilter in = Globals.objectMapper.readValue(inBytes, RequestFilter.class);

            JOCDefaultResponse response = initPermissions(null, getPermissonsJocCockpit("", accessToken).getInventory().getConfigurations().isEdit());
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

            DBItemInventoryConfiguration config = JocInventory.getConfiguration(dbLayer, in, folderPermissions);
            
            if (JocInventory.isDeployable(config.getTypeAsEnum())) {
                deployments = dbLayer.getDeploymentHistory(config.getId());
            }

            if (deployments != null && !deployments.isEmpty()) {
                Collections.sort(deployments, Comparator.comparing(InventoryDeploymentItem::getDeploymentDate).reversed());
                lastDeployment = deployments.get(0);
            }

            ConfigurationObject item = new ConfigurationObject();
            item.setId(config.getId());
            item.setDeliveryDate(new Date());
            item.setPath(config.getPath());
            item.setObjectType(JocInventory.getType(config.getType()));
            item.setValid(config.getValid());
            item.setDeleted(config.getDeleted());

            if (config.getDeployed()) {
                if (lastDeployment == null) {
                    throw new DBMissingDataException(String.format("[id=%s][%s][%s]deployments not found", in.getId(), config.getPath(), config
                            .getTypeAsEnum().name()));
                }

                item.setState(ItemStateEnum.DRAFT_NOT_EXIST);
                item.setConfigurationDate(lastDeployment.getDeploymentDate());
                item.setConfiguration(JocInventory.content2IJSObject(lastDeployment.getContent(), config.getType()));
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
                item.setConfiguration(JocInventory.content2IJSObject(config.getContent(), config.getType()));
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
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

}
