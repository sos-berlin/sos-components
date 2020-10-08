package com.sos.joc.inventory.impl;

import javax.ws.rs.Path;

import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.InventoryAudit;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.InventoryDBLayer.InventoryDeleteResult;
import com.sos.joc.db.inventory.items.InventoryDeploymentItem;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.inventory.resource.IDeleteDraftResource;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.delete.RequestFilter;
import com.sos.joc.model.inventory.delete.ResponseItem;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class DeleteDraftResourceImpl extends JOCResourceImpl implements IDeleteDraftResource {

    @Override
    public JOCDefaultResponse delete(final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of oneOf-Requirements
            JsonValidator.validate(inBytes, RequestFilter.class);
            RequestFilter in = Globals.objectMapper.readValue(inBytes, RequestFilter.class);

            JOCDefaultResponse response = checkPermissions(accessToken, in);
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
            
            boolean idIsDefined = in.getId() != null;
            boolean pathAndTypeIsDefined = !SOSString.isEmpty(in.getPath()) && in.getObjectType() != null;
            if (!idIsDefined && !pathAndTypeIsDefined) {
                throw new JocMissingRequiredParameterException("'id' or ('path' and 'objectType') parameter is required");
            }
            
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            DBItemInventoryConfiguration config = null;
            session.beginTransaction();
            if (in.getId() != null) {
                config = dbLayer.getConfiguration(in.getId());
            } else if (in.getPath() != null && in.getObjectType() != null) {
                config = dbLayer.getConfiguration(in.getPath(), in.getObjectType().intValue());
            }
            session.commit();

            if (config == null) {
                throw new DBMissingDataException(String.format("configuration not found: %s", SOSString.toString(in)));
            }
            if (config.getDeployed()) {
                throw new DBMissingDataException(String.format("[%s]can't be deleted - no draft exists", config.getPath()));
            }
            if (!folderPermissions.isPermittedForFolder(config.getFolder())) {
                return accessDeniedResponse();
            }

            ResponseItem r = new ResponseItem();
            session.beginTransaction();
            InventoryDeploymentItem lastDeployment = dbLayer.getLastDeploymentHistory(config.getId());
            if (lastDeployment == null) {
                deleteConfiguration(dbLayer, config, JocInventory.getType(config.getType()));
                r.setDeleteFromTree(true);
            } else {
                dbLayer.resetConfigurationDraft(config.getId());
                r.setDeleteFromTree(false);
            }
            session.commit();

            storeAuditLog(in, config);

            return JOCDefaultResponse.responseStatus200(r);
        } catch (Throwable e) {
            if (session != null && session.isTransactionOpened()) {
                Globals.rollback(session);
            }
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private InventoryDeleteResult deleteConfiguration(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, ConfigurationType type)
            throws Exception {
        InventoryDeleteResult result = null;
        switch (type) {
        case WORKFLOW:
            result = dbLayer.deleteWorkflow(config.getId());
            break;
        // case JOB:
        // result = dbLayer.deleteWorkflowJob(config.getId());
        // break;
        case JOBCLASS:
            result = dbLayer.deleteJobClass(config.getId());
            break;
        case AGENTCLUSTER:
            result = dbLayer.deleteAgentCluster(config.getId());
            break;
        case LOCK:
            result = dbLayer.deleteLock(config.getId());
            break;
        case JUNCTION:
            result = dbLayer.deleteJunction(config.getId());
            break;
        case FOLDER:
            result = dbLayer.deleteConfiguration(config.getId());
            break;
        case ORDER:
            result = dbLayer.deleteWorkflowOrder(config.getId());
            break;
        case WORKINGDAYSCALENDAR:
        case NONWORKINGDAYSCALENDAR:
            break;
        default:
            break;
        }
        return result;
    }

    private void storeAuditLog(RequestFilter in, DBItemInventoryConfiguration config) {
        if (config != null) {
            InventoryAudit audit = new InventoryAudit(in.getObjectType(), config.getPath(), config.getFolder());
            logAuditMessage(audit);
            storeAuditLogEntry(audit);
        }
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, final RequestFilter in) throws Exception {
        SOSPermissionJocCockpit permissions = getPermissonsJocCockpit("", accessToken);
        boolean permission = permissions.getInventory().getConfigurations().isEdit();

        return init(IMPL_PATH, in, accessToken, "", permission);
    }

}
