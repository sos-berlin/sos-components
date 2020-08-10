package com.sos.joc.inventory.impl;

import java.util.Date;

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
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IReadConfigurationResource;
import com.sos.joc.model.inventory.common.ItemStateEnum;
import com.sos.joc.model.inventory.common.ResponseItemDeployment;
import com.sos.joc.model.inventory.read.RequestFilter;
import com.sos.joc.model.inventory.read.ResponseItem;
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

            session.beginTransaction();
            DBItemInventoryConfiguration config = dbLayer.getConfiguration(in.getId());
            if (config == null) {
                throw new Exception(String.format("configuration not found: %s", SOSString.toString(in)));
            }
            InventoryDeploymentItem lastDeployment = dbLayer.getLastDeploymentHistory(config.getId());
            session.commit();

            if (!folderPermissions.isPermittedForFolder(config.getFolder())) {
                return accessDeniedResponse();
            }

            ResponseItem item = new ResponseItem();
            item.setId(config.getId());
            item.setDeliveryDate(new Date());
            item.setPath(config.getPath());
            item.setObjectType(JocInventory.getJobSchedulerType(config.getType()));

            if (config.getDeployed()) {
                if (lastDeployment == null) {
                    throw new Exception(String.format("[id=%s][%s][%s]deployment not found", in.getId(), config.getPath(), config.getTypeAsEnum()
                            .name()));
                }
                item.setState(ItemStateEnum.DRAFT_NOT_EXIST);
                item.setConfigurationDate(lastDeployment.getDeploymentDate());
                item.setConfiguration(JocInventory.convertDeployableContent2Joc(lastDeployment.getContent(), JocInventory.getType(config.getType())));
                item.setDeployed(true);
                
                ResponseItemDeployment d = new ResponseItemDeployment();
                d.setDeploymentId(lastDeployment.getId());
                d.setVersion(lastDeployment.getVersion());
                d.setDeploymentDate(lastDeployment.getDeploymentDate());
                d.setControllerId(lastDeployment.getControllerId());
                item.setDeployment(d);
                
            } else {
                String content = null;
                if (SOSString.isEmpty(config.getContentJoc())) {
                    content = JocInventory.convertDeployableContent2Joc(config.getContent(), JocInventory.getType(config.getType()));
                } else {
                    content = config.getContentJoc();
                }
                item.setConfigurationDate(config.getModified());
                item.setConfiguration(content);
                item.setDeployed(false);

                if (lastDeployment == null) {
                    item.setState(ItemStateEnum.DEPLOYMENT_NOT_EXIST);
                } else {
                    ResponseItemDeployment d = new ResponseItemDeployment();
                    d.setDeploymentId(lastDeployment.getId());
                    d.setVersion(lastDeployment.getVersion());
                    d.setDeploymentDate(lastDeployment.getDeploymentDate());
                    d.setControllerId(lastDeployment.getControllerId());
                    if (d.getDeploymentDate().after(config.getModified())) {
                        item.setState(ItemStateEnum.DEPLOYMENT_IS_NEWER);
                    } else {
                        item.setState(ItemStateEnum.DRAFT_IS_NEWER);
                    }
                }
            }
            return JOCDefaultResponse.responseStatus200(item);
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
        boolean permission = permissions.getJS7Controller().getAdministration().getConfigurations().isEdit();
        return init(IMPL_PATH, in, accessToken, "", permission);
    }

}
