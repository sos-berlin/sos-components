package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.InventoryAudit;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.items.InventoryDeployablesTreeFolderItem;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IDeleteConfigurationResource;
import com.sos.joc.model.common.JobSchedulerObjectType;
import com.sos.joc.model.inventory.delete.RequestFilter;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class DeleteConfigurationResourceImpl extends JOCResourceImpl implements IDeleteConfigurationResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteConfigurationResourceImpl.class);

    @Override
    public JOCDefaultResponse delete(final String accessToken, final byte[] inBytes) {
        try {
            JsonValidator.validateFailFast(inBytes, RequestFilter.class);
            RequestFilter in = Globals.objectMapper.readValue(inBytes, RequestFilter.class);
            if (in.getPath() != null) {
                in.setPath(normalizeFolder(in.getPath()));
            }

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
            Instant startTime = Instant.now();
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            JobSchedulerObjectType objectType = null;
            String path = null;
            if (in.getId() != null) {
                InventoryDeployablesTreeFolderItem config = getSingle(dbLayer, in.getId());
                if (config == null) {
                    throw new Exception(String.format("configuration not found: %s", in.getId()));
                }
                if (!folderPermissions.isPermittedForFolder(config.getFolder())) {
                    return accessDeniedResponse();
                }
                objectType = JocInventory.getJobSchedulerType(config.getType());
                path = config.getPath();
                deleteSingle(dbLayer, config);
            } else if (in.getPath() != null) {
                if (!folderPermissions.isPermittedForFolder(in.getPath())) {
                    return accessDeniedResponse();
                }
                objectType = JobSchedulerObjectType.FOLDER;
                path = in.getPath();
                deleteFolder(dbLayer, in.getPath());
            }

            storeAuditLog(session, startTime, path, objectType);

            return JOCDefaultResponse.responseStatus200(new Date());
        } catch (Throwable e) {
            if (session != null && session.isTransactionOpened()) {
                Globals.rollback(session);
            }
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private InventoryDeployablesTreeFolderItem getSingle(InventoryDBLayer dbLayer, Long id) throws Exception {
        dbLayer.getSession().beginTransaction();
        InventoryDeployablesTreeFolderItem config = dbLayer.getConfigurationWithMaxDeployment(id);
        dbLayer.getSession().commit();
        return config;
    }

    private void deleteSingle(InventoryDBLayer dbLayer, InventoryDeployablesTreeFolderItem config) throws Exception {
        dbLayer.getSession().beginTransaction();
        handleSingle(dbLayer, config);
        dbLayer.getSession().commit();
    }

    private void handleSingle(InventoryDBLayer dbLayer, InventoryDeployablesTreeFolderItem config) throws Exception {
        dbLayer.markConfigurationAsDeleted(config.getId(), true);
    }

    private void deleteFolder(InventoryDBLayer dbLayer, String folder) throws Exception {
        dbLayer.getSession().beginTransaction();
        List<InventoryDeployablesTreeFolderItem> items = dbLayer.getConfigurationsWithMaxDeployment(folder, true);
        if (items != null) {
            for (InventoryDeployablesTreeFolderItem item : items) {
                handleSingle(dbLayer, item);
            }
        }
        dbLayer.getSession().commit();
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, final RequestFilter in) throws Exception {
        SOSPermissionJocCockpit permissions = getPermissonsJocCockpit("", accessToken);
        boolean permission = permissions.getInventory().getConfigurations().isEdit();

        return init(IMPL_PATH, in, accessToken, "", permission);
    }

    private void storeAuditLog(SOSHibernateSession session, Instant startTime, String path, JobSchedulerObjectType objectType) {
        try {
            session.beginTransaction();
            InventoryAudit audit = new InventoryAudit(objectType, path);
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
