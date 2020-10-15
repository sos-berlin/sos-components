package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
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
import com.sos.joc.model.publish.OperationType;
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
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            DBItemInventoryConfiguration config = JocInventory.getConfiguration(dbLayer, in, folderPermissions);
            ConfigurationType type = config.getTypeAsEnum();

            if (config.getDeployed() || config.getReleased() || ConfigurationType.FOLDER.equals(type)) {
                throw new DBMissingDataException(String.format("[%s] can't be deleted - no draft exists", config.getPath()));
            }
            
            ResponseItem r = new ResponseItem();
            r.setDeleteFromTree(true);
            
            if (JocInventory.isDeployable(type)) {
                InventoryDeploymentItem lastDeployment = dbLayer.getLastDeploymentHistory(config.getId());
                if (lastDeployment == null || OperationType.DELETE.value() == lastDeployment.getOperation() || lastDeployment.getContent() == null) {
                    // never deployed before or deleted or without content
                    dbLayer.getSession().delete(config);
                } else { 
                    // deployed
                    r.setDeleteFromTree(false);
                    config.setDeployed(true);
                    config.setContent(lastDeployment.getContent());
                    config.setModified(Date.from(Instant.now()));
                    dbLayer.getSession().update(config);
                }
            } else if (JocInventory.isReleasable(type)) {
                DBItemInventoryReleasedConfiguration releasedItem = dbLayer.getReleasedConfiguration(config.getId());
                if (releasedItem == null || releasedItem.getContent() == null) {
                    // never released before or without content
                    dbLayer.getSession().delete(config);
                } else {
                    // released
                    r.setDeleteFromTree(false);
                    config.setReleased(true);
                    config.setContent(releasedItem.getContent());
                    config.setModified(Date.from(Instant.now()));
                    dbLayer.getSession().update(config);
                }
            }

            // TODO consider other Inventory tables 
            storeAuditLog(type, config.getPath(), config.getFolder());

            return JOCDefaultResponse.responseStatus200(r);
        } finally {
            Globals.disconnect(session);
        }
    }

//    private InventoryDeleteResult deleteConfiguration(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, ConfigurationType type)
//            throws Exception {
//        InventoryDeleteResult result = null;
//        switch (type) {
//        case WORKFLOW:
//            result = dbLayer.deleteWorkflow(config.getId());
//            break;
//        // case JOB:
//        // result = dbLayer.deleteWorkflowJob(config.getId());
//        // break;
//        case JOBCLASS:
//            result = dbLayer.deleteJobClass(config.getId());
//            break;
//        case AGENTCLUSTER:
//            result = dbLayer.deleteAgentCluster(config.getId());
//            break;
//        case LOCK:
//            result = dbLayer.deleteLock(config.getId());
//            break;
//        case JUNCTION:
//            result = dbLayer.deleteJunction(config.getId());
//            break;
//        case FOLDER:
//            result = dbLayer.deleteConfiguration(config.getId());
//            break;
//        case ORDER:
//            result = dbLayer.deleteWorkflowOrder(config.getId());
//            break;
//        case WORKINGDAYSCALENDAR:
//        case NONWORKINGDAYSCALENDAR:
//            break;
//        default:
//            break;
//        }
//        return result;
//    }

    private void storeAuditLog(ConfigurationType objectType, String path, String folder) {
        InventoryAudit audit = new InventoryAudit(objectType, path, folder);
        logAuditMessage(audit);
        storeAuditLogEntry(audit);
    }

}
