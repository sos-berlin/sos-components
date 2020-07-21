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
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IDeleteConfigurationResource;
import com.sos.joc.model.common.JobSchedulerObjectType;
import com.sos.joc.model.inventory.common.ConfigurationItem;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class DeleteConfigurationResourceImpl extends JOCResourceImpl implements IDeleteConfigurationResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteConfigurationResourceImpl.class);

    @Override
    public JOCDefaultResponse delete(final String accessToken, final byte[] inBytes) {
        try {
            JsonValidator.validateFailFast(inBytes, ConfigurationItem.class);
            ConfigurationItem in = Globals.objectMapper.readValue(inBytes, ConfigurationItem.class);

            checkRequiredParameter("objectType", in.getObjectType());
            checkRequiredParameter("path", in.getPath());

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

    private Date delete(ConfigurationItem in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            if (in.getObjectType().equals(JobSchedulerObjectType.FOLDER)) {
                deleteFolder(dbLayer, session, in);
            } else {
                deleteConfiguration(dbLayer, session, in);
            }
            return Date.from(Instant.now());
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private void deleteFolder(InventoryDBLayer dbLayer, SOSHibernateSession session, final ConfigurationItem in) throws Exception {
        if (in.getPath().equals(JocInventory.ROOT_FOLDER)) {
            dbLayer.deleteAll();
        } else {
            session.beginTransaction();
            List<DBItemInventoryConfiguration> result = dbLayer.getConfigurationsByFolder(in.getPath());
            if (result != null && result.size() > 0) {
                LOGGER.info("TO DELETE:" + result.size());
            }
            session.commit();
        }
    }

    private void deleteConfiguration(InventoryDBLayer dbLayer, SOSHibernateSession session, final ConfigurationItem in) throws Exception {
        session.beginTransaction();

        DBItemInventoryConfiguration result = null;
        if (in.getId() != null && in.getId() > 0L) {
            result = dbLayer.getConfiguration(in.getId(), JocInventory.getType(in.getObjectType()));
        }
        if (result == null) {// TODO temp
            result = dbLayer.getConfiguration(in.getPath(), JocInventory.getType(in.getObjectType()));
        }
        if (result == null) {
            throw new Exception(String.format("configuration not found: %s", SOSString.toString(in)));
        }

        ConfigurationType type = JocInventory.getType(result.getType());
        if (type == null) {
            throw new Exception(String.format("unsupported configuration type=%s", in.getObjectType()));
        }

        InventoryAudit audit = new InventoryAudit(in);
        logAuditMessage(audit);
        audit.setStartTime(result.getCreated());
        DBItemJocAuditLog auditItem = storeAuditLogEntry(audit);
        if (auditItem != null) {
            result.setAuditLogId(auditItem.getId());
        }
        session.commit();

        session.beginTransaction();
        InvertoryDeleteResult deleted = null;
        switch (type) {
        case WORKFLOW:
            deleted = dbLayer.deleteWorkflow(result.getId());
            break;
        case JOB:
            // TODO
            break;
        case JOBCLASS:
            deleted = dbLayer.deleteJobClass(result.getId());
            break;
        case AGENTCLUSTER:
            deleted = dbLayer.deleteAgentCluster(result.getId());
            break;
        case LOCK:
            deleted = dbLayer.deleteLock(result.getId());
            break;
        case JUNCTION:
            deleted = dbLayer.deleteJunction(result.getId());
            break;
        case ORDER:
            break;
        case CALENDAR:
            break;
        default:
            break;
        }
        session.commit();
        LOGGER.info(String.format("deleted", SOSString.toString(deleted)));
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, final ConfigurationItem in) throws Exception {
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
