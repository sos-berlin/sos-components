package com.sos.joc.inventory.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
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
    
    private List<RequestFilter> updated = new ArrayList<>();
    private List<RequestFilter> deleted = new ArrayList<>();
    private SortedSet<String> folderToDelete = new TreeSet<>(Comparator.reverseOrder());

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
            
            ResponseItem entity = new ResponseItem();
            
            session.beginTransaction();
            
            if (ConfigurationType.FOLDER.equals(type)) {
                List<DBItemInventoryConfiguration> dbFolderContent = dbLayer.getFolderContent(config.getPath(), true, null);
                for (DBItemInventoryConfiguration item : dbFolderContent) {
                    if (!item.getDeployed() && !item.getReleased() && !ConfigurationType.FOLDER.intValue().equals(item.getType())) {
                        deleteUpdateDraft(item.getTypeAsEnum(), dbLayer, item);
                    }
                }
                // delete empty folders
                if (!folderToDelete.isEmpty()) {
                    Map<String, DBItemInventoryConfiguration> folderMap = dbFolderContent.stream().filter(item -> ConfigurationType.FOLDER.intValue()
                            .equals(item.getType())).collect(Collectors.toMap(DBItemInventoryConfiguration::getPath, Function.identity()));
                    for (String folder : folderToDelete) {
                        List<DBItemInventoryConfiguration> folderContent = dbLayer.getFolderContent(folder, true, null);
                        if (folderContent == null || folderContent.isEmpty()) {
                            DBItemInventoryConfiguration f = folderMap.get(folder);
                            RequestFilter r = new RequestFilter();
                            r.setId(f.getId());
                            r.setObjectType(ConfigurationType.FOLDER);
                            r.setPath(f.getPath());
                            session.delete(f);
                            deleted.add(r);
                        } else {
                            //break;
                        }
                    }
                }
            } else {
                if (config.getDeployed() || config.getReleased()) {
                    throw new DBMissingDataException(String.format("[%s] can't be deleted - no draft exists", config.getPath()));
                }
                deleteUpdateDraft(type, dbLayer, config);
            }
            
            session.commit();

            entity.setDeleted(deleted);
            entity.setUpdated(updated);
            entity.setDeliveryDate(Date.from(Instant.now()));
            
            // TODO consider other Inventory tables?
            storeAuditLog(type, config.getPath(), config.getFolder());

            return JOCDefaultResponse.responseStatus200(entity);
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
    
    private void deleteUpdateDraft(ConfigurationType type, InventoryDBLayer dbLayer, DBItemInventoryConfiguration item) throws SOSHibernateException,
            JsonParseException, JsonMappingException, JsonProcessingException, IOException {
        RequestFilter r = new RequestFilter();
        r.setId(item.getId());
        r.setObjectType(item.getTypeAsEnum());
        r.setPath(item.getPath());
        if (JocInventory.isDeployable(type)) {
            InventoryDeploymentItem lastDeployment = dbLayer.getLastDeployedContent(item.getId());
            if (lastDeployment == null) {
                // never deployed before or deleted or without content
                JocInventory.deleteConfiguration(dbLayer, item);
                deleted.add(r);
                folderToDelete.add(item.getFolder());
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
                folderToDelete.add(item.getFolder());
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
