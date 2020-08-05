package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;

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
import com.sos.joc.db.inventory.InventoryMeta.ConfigurationType;
import com.sos.joc.db.inventory.items.InventoryDeploymentItem;
import com.sos.joc.db.inventory.items.InventoryTreeFolderItem;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IDeleteDraftResource;
import com.sos.joc.model.common.JobSchedulerObjectType;
import com.sos.joc.model.inventory.delete.draft.FilterDeleteDraft;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class DeleteDraftResourceImpl extends JOCResourceImpl implements IDeleteDraftResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteConfigurationResourceImpl.class);

    @Override
    public JOCDefaultResponse delete(final String accessToken, final byte[] inBytes) {
        try {
            JsonValidator.validateFailFast(inBytes, FilterDeleteDraft.class);
            FilterDeleteDraft in = Globals.objectMapper.readValue(inBytes, FilterDeleteDraft.class);

            checkRequiredParameter("path", in.getPath());
            checkRequiredParameter("objectType", in.getObjectType());
            in.setPath(Globals.normalizePath(in.getPath()));

            JOCDefaultResponse response = checkPermissions(accessToken, in);
            if (response == null) {
                response = JOCDefaultResponse.responseStatus200(delete(in));
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private Date delete(FilterDeleteDraft in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            Instant startTime = Instant.now();
            InvertoryDeleteResult result = null;
            if (in.getObjectType().equals(JobSchedulerObjectType.FOLDER)) {
                // result = deleteFolder(dbLayer, session, in);
                throw new Exception("draft delete folder not yet supported");
            } else {
                result = deleteDraft(dbLayer, session, in);
            }
            storeAuditLog(in, session, result, startTime);

            return Date.from(Instant.now());
        } catch (Throwable e) {
            if (session != null && session.isTransactionOpened()) {
                Globals.rollback(session);
            }
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    @SuppressWarnings("unused")
    private InvertoryDeleteResult deleteFolder(InventoryDBLayer dbLayer, SOSHibernateSession session, FilterDeleteDraft in) throws Exception {
        InvertoryDeleteResult result = dbLayer.new InvertoryDeleteResult();
        session.beginTransaction();
        if (in.getPath().equals(JocInventory.ROOT_FOLDER)) {
            dbLayer.deleteAll();
            result.setConfigurations(1);
        } else {
            List<InventoryTreeFolderItem> items = dbLayer.getConfigurationsByFolder(in.getPath(), true);
            if (items != null && items.size() > 0) {
                // TODO optimize ...
                for (InventoryTreeFolderItem config : items) {
                    // deleteConfiguration(dbLayer, config);
                }
                result.setConfigurations(1);
            }
        }
        session.commit();
        return result;
    }

    private InvertoryDeleteResult deleteDraft(InventoryDBLayer dbLayer, SOSHibernateSession session, final FilterDeleteDraft in) throws Exception {

        session.beginTransaction();
        DBItemInventoryConfiguration config = null;
        if (in.getId() != null && in.getId() > 0L) {
            config = dbLayer.getConfiguration(in.getId(), JocInventory.getType(in.getObjectType()));
        }
        if (config == null) {// TODO temp
            config = dbLayer.getConfiguration(in.getPath(), JocInventory.getType(in.getObjectType()));
        }
        session.commit();

        if (config == null) {
            throw new Exception(String.format("configuration not found: %s", SOSString.toString(in)));
        }

        ConfigurationType type = JocInventory.getType(config.getType());
        if (type == null) {
            throw new Exception(String.format("unsupported configuration type=%s", in.getObjectType()));
        }
        if (config.getDeployed()) {
            throw new Exception(String.format("[%s]can't be deleted - no draft exists", config.getPath()));
        }

        session.beginTransaction();
        InventoryDeploymentItem lastDeployment = dbLayer.getLastDeploymentHistory(config.getId(), config.getType());
        if (lastDeployment == null) {
            deleteConfiguration(dbLayer, config, type);
        } else {
            dbLayer.resetConfigurationDraft(config.getId(), config.getTypeAsEnum());
        }
        session.commit();
        return null;
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

    private void storeAuditLog(FilterDeleteDraft in, SOSHibernateSession session, InvertoryDeleteResult result, Instant startTime) {
        if (result != null && result.deleted()) {
            try {
                session.beginTransaction();
                InventoryAudit audit = new InventoryAudit(in);
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

    private JOCDefaultResponse checkPermissions(final String accessToken, final FilterDeleteDraft in) throws Exception {
        SOSPermissionJocCockpit permissions = getPermissonsJocCockpit("", accessToken);
        boolean permission = permissions.getJobschedulerMaster().getAdministration().getConfigurations().isEdit();

        JOCDefaultResponse response = init(IMPL_PATH, in, accessToken, "", permission);
        if (response == null) {
            if (!folderPermissions.isPermittedForFolder(in.getPath())) {
                return accessDeniedResponse();
            }
        }
        return response;
    }

}
