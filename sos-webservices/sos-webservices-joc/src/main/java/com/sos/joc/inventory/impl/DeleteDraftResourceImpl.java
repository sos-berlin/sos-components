package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.sos.joc.db.inventory.InventoryDBLayer.InvertoryDeleteResult;
import com.sos.joc.db.inventory.items.InventoryDeploymentItem;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IDeleteDraftResource;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.delete.RequestFilter;
import com.sos.joc.model.inventory.delete.ResponseItem;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class DeleteDraftResourceImpl extends JOCResourceImpl implements IDeleteDraftResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteDraftResourceImpl.class);

    @Override
    public JOCDefaultResponse delete(final String accessToken, final byte[] inBytes) {
        try {
            JsonValidator.validateFailFast(inBytes, RequestFilter.class);
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
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            Instant startTime = Instant.now();
            DBItemInventoryConfiguration config = null;
            session.beginTransaction();
            if (in.getId() != null) {
                config = dbLayer.getConfiguration(in.getId());
            } else if (in.getPath() != null && in.getObjectType() != null) {
                config = dbLayer.getConfiguration(in.getPath(), JocInventory.getType(in.getObjectType()));
            }
            session.commit();

            if (config == null) {
                throw new Exception(String.format("configuration not found: %s", SOSString.toString(in)));
            }
            if (config.getDeployed()) {
                throw new Exception(String.format("[%s]can't be deleted - no draft exists", config.getPath()));
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

            storeAuditLog(in, session, config, startTime);

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

    private InvertoryDeleteResult deleteConfiguration(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, ConfigurationType type)
            throws Exception {
        InvertoryDeleteResult result = null;
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
        case CALENDAR:
            result = dbLayer.deleteCalendar(config.getId());
            break;
        default:
            break;
        }
        return result;
    }

    private void storeAuditLog(RequestFilter in, SOSHibernateSession session, DBItemInventoryConfiguration config, Instant startTime) {
        if (config != null) {
            try {
                session.beginTransaction();
                InventoryAudit audit = new InventoryAudit(JocInventory.getJobSchedulerType(config.getType()), config.getPath());
                logAuditMessage(audit);
                audit.setStartTime(Date.from(startTime));
                storeAuditLogEntry(audit);
                session.commit();
            } catch (Throwable e) {
                LOGGER.warn(e.toString(), e);
                Globals.rollback(session);
            }
        }
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, final RequestFilter in) throws Exception {
        SOSPermissionJocCockpit permissions = getPermissonsJocCockpit("", accessToken);
        boolean permission = permissions.getInventory().getConfigurations().isEdit();

        return init(IMPL_PATH, in, accessToken, "", permission);
    }

}
