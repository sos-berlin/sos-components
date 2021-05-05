package com.sos.joc.inventory.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.items.InventoryDeploymentItem;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IDeleteDraftResource;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.RequestFilter;
import com.sos.joc.model.inventory.common.RequestFilters;
import com.sos.joc.model.inventory.common.RequestFolder;
import com.sos.joc.model.inventory.delete.ResponseItem;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class DeleteDraftResourceImpl extends JOCResourceImpl implements IDeleteDraftResource {
    
    private List<RequestFilter> updated = new ArrayList<>();
    private List<RequestFilter> deleted = new ArrayList<>();

    @Override
    public JOCDefaultResponse delete(final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of oneOf-Requirements
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFilters.class);
            RequestFilters in = Globals.objectMapper.readValue(inBytes, RequestFilters.class);
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
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
    
    @Override
    public JOCDefaultResponse deleteFolder(final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of oneOf-Requirements
            initLogging(IMPL_PATH_FOLDER, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFolder.class);
            RequestFolder in = Globals.objectMapper.readValue(inBytes, RequestFolder.class);
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (response == null) {
                response = deleteFolder(in, true);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private JOCDefaultResponse delete(RequestFilters in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            session.setAutoCommit(false);

            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            Globals.beginTransaction(session);

            Predicate<RequestFilter> isFolder = r -> ConfigurationType.FOLDER.equals(r.getObjectType());
            if (in.getObjects().stream().parallel().anyMatch(isFolder)) {
                // throw new
            }
            Set<String> foldersForEvent = new HashSet<>();
            ResponseItem entity = new ResponseItem();
            Set<RequestFilter> requests = in.getObjects().stream().filter(isFolder.negate()).collect(Collectors.toSet());
            Long auditLogId = JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog());
            for (RequestFilter r : requests) {
                DBItemInventoryConfiguration config = JocInventory.getConfiguration(dbLayer, r, folderPermissions);
                if (config.getDeployed() || config.getReleased()) {
                    if (requests.size() == 1) {
                        throw new DBMissingDataException(String.format("Draft of [%s] doesn't exist", config.getPath()));
                    } else {
                        continue;
                    }
                }
                deleteUpdateDraft(config.getTypeAsEnum(), dbLayer, config, auditLogId);
                foldersForEvent.add(config.getFolder());
            }
            Globals.commit(session);

            entity.setDeleted(deleted);
            entity.setUpdated(updated);
            entity.setDeliveryDate(Date.from(Instant.now()));

            if (deleted.size() + updated.size() > 0) {
                for (String folder : foldersForEvent) {
                    JocInventory.postEvent(folder);
                }
            }

            return JOCDefaultResponse.responseStatus200(entity);
        } catch (SOSHibernateException e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private JOCDefaultResponse deleteFolder(RequestFolder in, boolean withDeletionOfEmptyFolders) throws Exception {
        SOSHibernateSession session = null;
        try {
            checkRequiredComment(in.getAuditLog());
            
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH_FOLDER);
            session.setAutoCommit(false);

            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            Globals.beginTransaction(session);

            DBItemInventoryConfiguration config = JocInventory.getConfiguration(dbLayer, null, in.getPath(), ConfigurationType.FOLDER,
                    folderPermissions);
            ResponseItem entity = new ResponseItem();

            List<DBItemInventoryConfiguration> dbFolderContent = dbLayer.getFolderContent(config.getPath(), true, null);
            DBItemJocAuditLog auditItem = storeAuditLog(in.getAuditLog(), CategoryType.INVENTORY);
            Long auditLogId = auditItem != null ? auditItem.getId() : 0L;
            for (DBItemInventoryConfiguration item : dbFolderContent) {
                if (!item.getDeployed() && !item.getReleased() && !ConfigurationType.FOLDER.intValue().equals(item.getType())) {
                    deleteUpdateDraft(item.getTypeAsEnum(), dbLayer, item, auditLogId);
                }
            }
            if (withDeletionOfEmptyFolders) {
                deleted.addAll(JocInventory.deleteEmptyFolders(dbLayer, config).stream().map(i -> {
                    RequestFilter r = new RequestFilter();
                    r.setId(i.getId());
                    r.setPath(i.getPath());
                    r.setObjectType(ConfigurationType.FOLDER);
                    return r;
                }).collect(Collectors.toSet()));
            }
            Globals.commit(session);

            entity.setDeleted(deleted);
            entity.setUpdated(updated);
            entity.setDeliveryDate(Date.from(Instant.now()));

            if (deleted.size() + updated.size() > 0) {
                JocInventory.postEvent(config.getFolder());
            }

            return JOCDefaultResponse.responseStatus200(entity);
        } catch (SOSHibernateException e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private void deleteUpdateDraft(ConfigurationType type, InventoryDBLayer dbLayer, DBItemInventoryConfiguration item, Long auditLogId)
            throws SOSHibernateException, JsonParseException, JsonMappingException, JsonProcessingException, IOException {
        RequestFilter r = new RequestFilter();
        r.setId(item.getId());
        r.setObjectType(item.getTypeAsEnum());
        r.setPath(item.getPath());
        item.setAuditLogId(auditLogId);
        if (JocInventory.isDeployable(type)) {
            InventoryDeploymentItem lastDeployment = dbLayer.getLastDeployedContent(item.getId());
            if (lastDeployment == null) {
                // never deployed before or deleted or without content
                JocInventory.deleteConfiguration(dbLayer, item);
                deleted.add(r);
            } else {
                // deployed
                item.setValid(true);
                item.setReleased(false);
                item.setDeployed(true);
                item.setContent(lastDeployment.getContent());
                item.setModified(lastDeployment.getDeploymentDate());
                JocInventory.updateConfiguration(dbLayer, item);
                updated.add(r);
            }
        } else if (JocInventory.isReleasable(type)) {
            DBItemInventoryReleasedConfiguration releasedItem = dbLayer.getReleasedItemByConfigurationId(item.getId());
            if (releasedItem == null || releasedItem.getContent() == null) {
                // never released before or without content
                JocInventory.deleteConfiguration(dbLayer, item);
                deleted.add(r);
            } else {
                // released
                item.setValid(true);
                item.setReleased(true);
                item.setDeployed(false);
                item.setContent(releasedItem.getContent());
                item.setModified(releasedItem.getModified());
                JocInventory.updateConfiguration(dbLayer, item);
                updated.add(r);
            }
        }
    }

}
