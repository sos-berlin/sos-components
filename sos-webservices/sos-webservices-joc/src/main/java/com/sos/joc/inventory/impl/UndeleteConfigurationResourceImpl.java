package com.sos.joc.inventory.impl;

import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

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
import com.sos.joc.inventory.resource.IUndeleteConfigurationResource;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.delete.RequestFilter;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class UndeleteConfigurationResourceImpl extends JOCResourceImpl implements IUndeleteConfigurationResource {

    @Override
    public JOCDefaultResponse undelete(final String accessToken, final byte[] inBytes) {
        try {
            JsonValidator.validateFailFast(inBytes, RequestFilter.class);
            RequestFilter in = Globals.objectMapper.readValue(inBytes, RequestFilter.class);
            if (in.getPath() != null) {
                in.setPath(normalizeFolder(in.getPath()));
            }

            JOCDefaultResponse response = checkPermissions(accessToken, in);
            if (response == null) {
                response = undelete(in);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }

    }

    private JOCDefaultResponse undelete(RequestFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            ConfigurationType objectType = null;
            String path = null;
            String folder = null;
            if (in.getId() != null) {
                InventoryDeployablesTreeFolderItem config = getSingle(dbLayer, in.getId());
                if (config == null) {
                    throw new Exception(String.format("configuration not found: %s", in.getId()));
                }
                if (!folderPermissions.isPermittedForFolder(config.getFolder())) {
                    return accessDeniedResponse();
                }
                objectType = JocInventory.getType(config.getType());
                path = config.getPath();
                folder = getParent(path);
                undeleteSingle(dbLayer, config);
                undeleteParentFolders(dbLayer, config);
            } else if (in.getPath() != null) {
                if (!folderPermissions.isPermittedForFolder(in.getPath())) {
                    return accessDeniedResponse();
                }
                objectType = ConfigurationType.FOLDER;
                path = in.getPath();
                folder = path;
                undeleteFolder(dbLayer, in.getPath());
            }

            storeAuditLog(objectType, path, folder);

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

    private void undeleteSingle(InventoryDBLayer dbLayer, InventoryDeployablesTreeFolderItem config) throws Exception {
        dbLayer.getSession().beginTransaction();
        handleSingle(dbLayer, config);
        dbLayer.getSession().commit();
    }

    private void handleSingle(InventoryDBLayer dbLayer, InventoryDeployablesTreeFolderItem config) throws Exception {
        dbLayer.markConfigurationAsDeleted(config.getId(), false);
    }

    private void undeleteParentFolders(InventoryDBLayer dbLayer, InventoryDeployablesTreeFolderItem config) throws Exception {
        dbLayer.getSession().beginTransaction();
        handleParentFolders(dbLayer, config.getFolder());
        dbLayer.getSession().commit();
    }

    private void handleParentFolders(InventoryDBLayer dbLayer, final String folder) throws Exception {
        if (folder != null && !folder.equals(JocInventory.ROOT_FOLDER)) {
            String[] arr = folder.split("/");
            if (arr.length > 1) {
                dbLayer.markFolderAsDeleted(folder, false);

                String dir = folder;
                for (int i = 2; i < arr.length; i++) {
                    dir = folder.substring(0, dir.lastIndexOf("/"));
                    dbLayer.markFolderAsDeleted(dir, false);
                }
            }
        }
    }

    private void undeleteFolder(InventoryDBLayer dbLayer, String folder) throws Exception {
        dbLayer.getSession().beginTransaction();
        List<InventoryDeployablesTreeFolderItem> items = dbLayer.getConfigurationsWithMaxDeployment(folder, true);
        if (items != null) {
            for (InventoryDeployablesTreeFolderItem item : items) {
                handleSingle(dbLayer, item);
            }
            handleParentFolders(dbLayer, folder);
        }
        dbLayer.getSession().commit();
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, final RequestFilter in) throws Exception {
        SOSPermissionJocCockpit permissions = getPermissonsJocCockpit("", accessToken);
        boolean permission = permissions.getInventory().getConfigurations().isEdit();

        return init(IMPL_PATH, in, accessToken, "", permission);
    }

    private void storeAuditLog(ConfigurationType objectType, String path, String folder) {
        InventoryAudit audit = new InventoryAudit(objectType, path, folder);
        logAuditMessage(audit);
        storeAuditLogEntry(audit);
    }

}
