package com.sos.joc.inventory.impl;

import java.util.Date;

import javax.ws.rs.Path;

import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.InventoryMeta.ConfigurationType;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IReadConfigurationResource;
import com.sos.joc.model.inventory.common.Filter;
import com.sos.joc.model.inventory.common.Item;
import com.sos.joc.model.inventory.common.ItemDeployment;
import com.sos.joc.model.inventory.common.ItemStateEnum;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class ReadConfigurationResourceImpl extends JOCResourceImpl implements IReadConfigurationResource {

    @Override
    public JOCDefaultResponse read(final String accessToken, final byte[] inBytes) {
        try {
            JsonValidator.validateFailFast(inBytes, Filter.class);
            Filter in = Globals.objectMapper.readValue(inBytes, Filter.class);

            checkRequiredParameter("objectType", in.getObjectType());
            checkRequiredParameter("path", in.getPath());// for check permissions
            in.setPath(Globals.normalizePath(in.getPath()));

            JOCDefaultResponse response = checkPermissions(accessToken, in);
            if (response == null) {
                response = JOCDefaultResponse.responseStatus200(read(in));
            }
            return response;

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private Item read(Filter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            session.beginTransaction();
            DBItemInventoryConfiguration config = null;
            if (in.getId() != null && in.getId() > 0L) {
                config = dbLayer.getConfiguration(in.getId(), JocInventory.getType(in.getObjectType()));
            }
            if (config == null) {// TODO temp
                config = dbLayer.getConfiguration(in.getPath(), JocInventory.getType(in.getObjectType()));
            }
            if (config == null) {
                throw new Exception(String.format("configuration not found: %s", SOSString.toString(in)));
            }
            ConfigurationType type = JocInventory.getType(config.getType());
            if (type == null) {
                throw new Exception(String.format("unsupported configuration type: %s (%s)", config.getType(), SOSHibernate.toString(config)));
            }

            DBItemDeploymentHistory lastDeployment = dbLayer.getLastDeploymentHistory(config.getId(), config.getType());
            session.commit();

            Item item = new Item();
            item.setId(config.getId());
            item.setDeliveryDate(new Date());
            item.setPath(config.getPath());
            item.setObjectType(in.getObjectType());

            if (config.getDeployed()) {
                if (lastDeployment == null) {
                    throw new Exception(String.format("[id=%s][%s][%s]deployment not found", in.getId(), in.getPath(), config.getTypeAsEnum()
                            .name()));
                }
                item.setState(ItemStateEnum.DRAFT_NOT_EXIST);
                item.setConfigurationDate(lastDeployment.getDeploymentDate());
                item.setConfiguration(JocInventory.convertDeployableContent2Joc(lastDeployment.getContent(), type));

                ItemDeployment d = new ItemDeployment();
                d.setVersion(lastDeployment.getVersion());
                d.setDeploymentDate(lastDeployment.getDeploymentDate());
                item.setDeployment(d);
            } else {
                String content = null;
                if (SOSString.isEmpty(config.getContentJoc())) {
                    content = JocInventory.convertDeployableContent2Joc(config.getContent(), type);
                } else {
                    content = config.getContentJoc();
                }
                item.setConfigurationDate(config.getModified());
                item.setConfiguration(content);

                if (lastDeployment == null) {
                    item.setState(ItemStateEnum.DEPLOYMENT_NOT_EXIST);
                } else {
                    ItemDeployment d = new ItemDeployment();
                    d.setVersion(lastDeployment.getVersion());
                    d.setDeploymentDate(lastDeployment.getDeploymentDate());
                    if (d.getDeploymentDate().after(config.getModified())) {
                        item.setState(ItemStateEnum.DEPLOYMENT_IS_NEWER);
                    } else {
                        item.setState(ItemStateEnum.DRAFT_IS_NEWER);
                    }
                }
            }
            return item;
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, final Filter in) throws Exception {
        SOSPermissionJocCockpit permissions = getPermissonsJocCockpit("", accessToken);
        boolean permission = permissions.getJobschedulerMaster().getAdministration().getConfigurations().isEdit();

        JOCDefaultResponse response = init(IMPL_PATH, in, accessToken, "", permission);
        if (response == null) {
            String path = normalizePath(in.getPath());
            if (!folderPermissions.isPermittedForFolder(getParent(path))) {
                return accessDeniedResponse();
            }
        }
        return response;
    }

}
