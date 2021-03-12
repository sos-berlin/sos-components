package com.sos.joc.inventory.impl;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.InventoryAudit;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryConfigurationTrash;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IRestoreConfigurationResource;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.restore.RequestFilter;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class RestoreConfigurationResourceImpl extends JOCResourceImpl implements IRestoreConfigurationResource {

    @Override
    public JOCDefaultResponse restore(final String accessToken, final byte[] inBytes) {
        try {
            initLogging(TRASH_IMPL_PATH, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFilter.class);
            RequestFilter in = Globals.objectMapper.readValue(inBytes, RequestFilter.class);

            JOCDefaultResponse response = initPermissions(null, getPermissonsJocCockpit("", accessToken).getInventory().getConfigurations().isEdit());
            if (response == null) {
                response = restore(in);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private JOCDefaultResponse restore(RequestFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            checkRequiredComment(in.getAuditLog());
            
            session = Globals.createSosHibernateStatelessConnection(TRASH_IMPL_PATH);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            
            session.beginTransaction();
            DBItemInventoryConfigurationTrash config = JocInventory.getTrashConfiguration(dbLayer, in, folderPermissions);
            ConfigurationType type = config.getTypeAsEnum();
            
            final java.nio.file.Path pWithoutFix = Paths.get(config.getPath());
            
            final List<String> replace = JocInventory.getSearchReplace(JocInventory.getSuffixPrefix(in.getSuffix(), in.getPrefix(),
                    Globals.restoreSuffixPrefix, JocInventory.DEFAULT_RESTORE_SUFFIX, config.getName(), type, dbLayer));

            Set<String> events = new HashSet<>();
            
            if (JocInventory.isFolder(type)) {
                
                List<ConfigurationType> restoreOrder = Arrays.asList(ConfigurationType.LOCK, ConfigurationType.NONWORKINGDAYSCALENDAR,
                        ConfigurationType.WORKINGDAYSCALENDAR, ConfigurationType.WORKFLOW, ConfigurationType.SCHEDULE);

                List<DBItemInventoryConfigurationTrash> trashDBFolderContent = dbLayer.getTrashFolderContent(config.getPath(), true, null);
                Map<ConfigurationType, List<DBItemInventoryConfigurationTrash>> trashMap = trashDBFolderContent.stream().collect(Collectors
                        .groupingBy(DBItemInventoryConfigurationTrash::getTypeAsEnum));
                List<DBItemInventoryConfiguration> curDBFolderContent = dbLayer.getFolderContent(config.getPath(), true, null);
                Set<String> folderPaths = curDBFolderContent.stream().filter(i -> ConfigurationType.FOLDER.intValue() == i.getType()).map(
                        DBItemInventoryConfiguration::getPath).collect(Collectors.toSet());
                Long auditLogId = createAuditLog(config, in.getAuditLog());
                
                for (ConfigurationType objType : restoreOrder) {
                    for (DBItemInventoryConfigurationTrash trashItem : trashMap.getOrDefault(objType, Collections.emptyList())) {
                        java.nio.file.Path invItemPath = Paths.get(trashItem.getPath());
                        if (ConfigurationType.FOLDER.intValue() == trashItem.getType()) {
                            if (!folderPaths.contains(trashItem.getPath())) {
                                DBItemInventoryConfiguration item = createItem(trashItem, invItemPath, auditLogId, dbLayer);
                                JocInventory.insertConfiguration(dbLayer, item);
                            }
                        } else {
                            List<DBItemInventoryConfiguration> targetItems = dbLayer.getConfigurationByName(trashItem.getName(), trashItem.getType());
                            if (targetItems.isEmpty()) {
                                JocInventory.insertConfiguration(dbLayer, createItem(trashItem, invItemPath, auditLogId, dbLayer));
                            } else {
                                JocInventory.insertConfiguration(dbLayer, createItem(trashItem, invItemPath.getParent().resolve(trashItem.getName()
                                        .replaceFirst(replace.get(0), replace.get(1))), auditLogId, dbLayer));
                            }
                        }
                    }
                }

                if (!JocInventory.ROOT_FOLDER.equals(config.getPath())) {
                    DBItemInventoryConfiguration newItem = dbLayer.getConfiguration(config.getPath(), ConfigurationType.FOLDER.intValue());

                    if (newItem == null) {
                        DBItemInventoryConfiguration newDbItem = createItem(config, pWithoutFix, auditLogId, dbLayer);
                        JocInventory.insertConfiguration(dbLayer, newDbItem);
                        JocInventory.makeParentDirs(dbLayer, pWithoutFix.getParent(), auditLogId);
                    }
                }
                
                dbLayer.deleteTrashFolder(config.getPath());
                
                events.add(config.getFolder());
                
            } else {
                
                Long auditLogId = createAuditLog(config, in.getAuditLog());
                if (dbLayer.getConfigurationByName(config.getName(), config.getType()).isEmpty()) {
                    JocInventory.insertConfiguration(dbLayer, createItem(config, pWithoutFix, auditLogId, dbLayer));
                } else {
                    JocInventory.insertConfiguration(dbLayer, createItem(config, pWithoutFix.getParent().resolve(pWithoutFix.getFileName().toString()
                            .replaceFirst(replace.get(0), replace.get(1))), auditLogId, dbLayer));
                }
                JocInventory.makeParentDirs(dbLayer, pWithoutFix.getParent(), auditLogId);
                session.delete(config);
                events.add(config.getFolder());
            }

            session.commit();
            for (String event : events) {
                JocInventory.postEvent(event);
                JocInventory.postTrashEvent(event);
            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private static boolean validate(DBItemInventoryConfiguration item, InventoryDBLayer dbLayer) {
        if (ConfigurationType.FOLDER.intValue() == item.getType()) {
            return true;
        }
        try {
            Validator.validate(item.getTypeAsEnum(), item.getContent().getBytes(StandardCharsets.UTF_8), dbLayer, null);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
    
    private Long createAuditLog(DBItemInventoryConfigurationTrash config, AuditParams auditParams) throws Exception {
        InventoryAudit audit = new InventoryAudit(config.getTypeAsEnum(), config.getPath(), config.getFolder(), auditParams);
        logAuditMessage(audit);
        DBItemJocAuditLog auditItem = storeAuditLogEntry(audit);
        if (auditItem != null) {
            return auditItem.getId();
        }
        return 0L;
    }

    private static DBItemInventoryConfiguration createItem(DBItemInventoryConfigurationTrash oldItem, java.nio.file.Path newItem, Long auditLogId, InventoryDBLayer dbLayer) {
        DBItemInventoryConfiguration item = new DBItemInventoryConfiguration();
        item.setId(null);
        item.setPath(newItem.toString().replace('\\', '/'));
        item.setFolder(newItem.getParent().toString().replace('\\', '/'));
        item.setName(newItem.getFileName().toString());
        item.setDeployed(false);
        item.setReleased(false);
        item.setModified(Date.from(Instant.now()));
        item.setCreated(item.getModified());
        item.setDeleted(false);
        item.setAuditLogId(auditLogId);
        item.setDocumentationId(oldItem.getDocumentationId());
        item.setTitle(oldItem.getTitle());
        item.setType(oldItem.getType());
        item.setContent(oldItem.getContent());
        item.setValid(validate(item, dbLayer));
        return item;
    }

}
