package com.sos.joc.inventory.impl;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.InventoryAudit;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.items.InventoryDeploymentItem;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IDeleteDraftResource;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.RequestFilter;
import com.sos.joc.model.inventory.delete.ResponseItem;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class DeleteDraftResourceImpl extends JOCResourceImpl implements IDeleteDraftResource {

    @Override
    public JOCDefaultResponse delete(final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of oneOf-Requirements
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFilter.class);
            RequestFilter in = Globals.objectMapper.readValue(inBytes, RequestFilter.class);
            JOCDefaultResponse response = initPermissions(null, getPermissonsJocCockpit("", accessToken).getInventory().getConfigurations().isEdit());
            if (response == null) {
                response = delete(in);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private JOCDefaultResponse delete(RequestFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            session.setAutoCommit(false);
            
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            DBItemInventoryConfiguration config = JocInventory.getConfiguration(dbLayer, in, folderPermissions);
            ConfigurationType type = config.getTypeAsEnum();

            if (config.getDeployed() || config.getReleased() || ConfigurationType.FOLDER.equals(type)) {
                throw new DBMissingDataException(String.format("[%s] can't be deleted - no draft exists", config.getPath()));
            }

            ResponseItem r = new ResponseItem();
            r.setDeleteFromTree(true);

            session.beginTransaction();
            if (JocInventory.isDeployable(type)) {
                InventoryDeploymentItem lastDeployment = dbLayer.getLastDeployedContent(config.getId());
                if (lastDeployment == null) {
                    // never deployed before or deleted or without content
                    JocInventory.deleteConfiguration(dbLayer, config);
                } else {
                    // deployed
                    r.setDeleteFromTree(false);
                    config.setValid(true);
                    config.setReleased(false);
                    config.setDeployed(true);
                    config.setContent(lastDeployment.getContent());
                    config.setModified(lastDeployment.getDeploymentDate());
                    JocInventory.updateConfiguration(dbLayer, config);
                }
            } else if (JocInventory.isReleasable(type)) {
                DBItemInventoryReleasedConfiguration releasedItem = dbLayer.getReleasedItemByConfigurationId(config.getId());
                if (releasedItem == null || releasedItem.getContent() == null) {
                    // never released before or without content
                    dbLayer.getSession().delete(config);
                } else {
                    // released
                    r.setDeleteFromTree(false);
                    config.setValid(true);
                    config.setReleased(true);
                    config.setDeployed(false);
                    config.setContent(releasedItem.getContent());
                    config.setModified(releasedItem.getModified());
                    dbLayer.getSession().update(config);
                }
            }
            session.commit();

            // TODO consider other Inventory tables
            storeAuditLog(type, config.getPath(), config.getFolder());

            return JOCDefaultResponse.responseStatus200(r);
        } catch (SOSHibernateException e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private void storeAuditLog(ConfigurationType objectType, String path, String folder) {
        InventoryAudit audit = new InventoryAudit(objectType, path, folder);
        logAuditMessage(audit);
        storeAuditLogEntry(audit);
    }

}
