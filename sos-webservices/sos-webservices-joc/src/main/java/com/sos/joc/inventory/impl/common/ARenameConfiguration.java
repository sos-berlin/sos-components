package com.sos.joc.inventory.impl.common;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.audit.JocAuditObjectsLog;
import com.sos.joc.classes.dependencies.DependencyResolver;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryTagging;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.InventoryJobTagDBLayer;
import com.sos.joc.db.inventory.InventoryTagDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.db.joc.DBItemJocAuditLogDetails;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.deploy.DeploymentHistoryMoveEvent;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.exceptions.JocObjectAlreadyExistException;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.ResponseNewPath;
import com.sos.joc.model.inventory.rename.RequestFilter;

public abstract class ARenameConfiguration extends JOCResourceImpl {

    public JOCDefaultResponse rename(RequestFilter in, String request) throws Exception {
        SOSHibernateSession session = null;
        List<DBItemInventoryConfiguration> updated = new ArrayList<DBItemInventoryConfiguration>(); 
        try {
            session = Globals.createSosHibernateStatelessConnection(request);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            
            session.beginTransaction();
            DBItemInventoryConfiguration config = JocInventory.getConfiguration(dbLayer, in, folderPermissions);
            ConfigurationType type = config.getTypeAsEnum();
            
            if (JocInventory.isFolder(type) && JocInventory.ROOT_FOLDER.equals(config.getPath())) {
                throw new JocFolderPermissionsException("Root folder cannot be renamed");
            }
            
            final java.nio.file.Path oldPath = Paths.get(config.getPath());
            final String oldFolder = config.getFolder();
            final java.nio.file.Path p = Paths.get(oldFolder).resolve(in.getNewPath()).normalize();
            boolean newFolderIsRootFolder = JocInventory.ROOT_FOLDER.equals(p.toString().replace('\\', '/'));
            String newFolder = newFolderIsRootFolder ? JocInventory.ROOT_FOLDER : p.getParent().toString().replace('\\', '/');
            String newPath = p.toString().replace('\\', '/');
            
            Set<String> events = new HashSet<>();
            Set<String> folderEvents = new HashSet<>();
            List<Long> workflowInvIds = new ArrayList<>();
            events.add(config.getPath());
            folderEvents.add(oldFolder);
            if (JocInventory.isWorkflow(config.getType())) {
                workflowInvIds.add(config.getId());
            }
            
            ResponseNewPath response = new ResponseNewPath();
            response.setObjectType(type);
            
            if (config.getPath().equals(newPath)) { // Nothing to do
                response.setPath(newPath);
                response.setId(config.getId());
                response.setDeliveryDate(Date.from(Instant.now()));
                return responseStatus200(Globals.objectMapper.writeValueAsBytes(response));
            }
            
            // Check folder permissions
            if (JocInventory.isFolder(type)) {
                if (!folderPermissions.isPermittedForFolder(newPath)) {
                    throw new JocFolderPermissionsException("Access denied for folder: " + newPath);
                }
            } else {
                if (!folderPermissions.isPermittedForFolder(newFolder)) {
                    throw new JocFolderPermissionsException("Access denied for folder: " + newFolder);
                }
            }
            
            // Check Java variable name rules
            for (int i = 0; i < p.getNameCount(); i++) {
                if (i == p.getNameCount() - 1) {
                    SOSCheckJavaVariableName.test("name", p.getName(i).toString());
                } else {
                    SOSCheckJavaVariableName.test("folder", p.getName(i).toString());
                }
            }
            DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog());

            if (JocInventory.isFolder(type)) {
                List<AuditLogDetail> auditLogDetails = new ArrayList<>();
                List<DBItemInventoryConfiguration> oldDBFolderContent = dbLayer.getFolderContent(config.getPath(), true, null, JocInventory
                        .isDescriptor(config.getTypeAsEnum()));
                oldDBFolderContent = oldDBFolderContent.stream().map(oldItem -> {
                    auditLogDetails.add(new AuditLogDetail(oldItem.getPath(), oldItem.getType()));
                    if (JocInventory.isWorkflow(oldItem.getType())) {
                        workflowInvIds.add(oldItem.getId());
                    }
                    setItem(oldItem, p.resolve(oldPath.relativize(Paths.get(oldItem.getPath()))), dbAuditLog.getId());
                    return oldItem;
                }).collect(Collectors.toList());
                
                JocAuditLog.storeAuditLogDetails(auditLogDetails, session, dbAuditLog);
                DBItemInventoryConfiguration newItem = dbLayer.getConfiguration(newPath, ConfigurationType.FOLDER.intValue());
                List<DBItemInventoryConfiguration> newDBFolderContent = dbLayer.getFolderContent(newPath, true, null, JocInventory.isDescriptor(config
                        .getTypeAsEnum()));
                Set<Long> deletedIds = new HashSet<>();

                if (newDBFolderContent != null && !newDBFolderContent.isEmpty()) {
                    newDBFolderContent.retainAll(oldDBFolderContent);
                    if (!newDBFolderContent.isEmpty()) {
                        Map<Boolean, List<DBItemInventoryConfiguration>> map = newDBFolderContent.stream().collect(Collectors.groupingBy(
                                item -> ConfigurationType.FOLDER.intValue() == item.getType()));
                        if (!map.getOrDefault(false, Collections.emptyList()).isEmpty()) { // all not folder items
                            throw new JocObjectAlreadyExistException("Cannot move to " + newPath + ": common objects are " + map.get(false).stream()
                                    .map(DBItemInventoryConfiguration::getPath).collect(Collectors.joining("', '", "'", "'")));
                        }
                        // delete all folder items
                        for (DBItemInventoryConfiguration targetItem : map.getOrDefault(true, Collections.emptyList())) {
                            deletedIds.add(targetItem.getId());
                            JocInventory.deleteConfiguration(dbLayer, targetItem);
                        }
                    }
                }
                
                if (newItem != null) {
                    deletedIds.add(newItem.getId());
                    JocInventory.deleteConfiguration(dbLayer, newItem);
                }
                
                setItem(config, p, dbAuditLog.getId());
                if (deletedIds.remove(config.getId())) {
                    config.setId(null);
                    JocInventory.insertConfiguration(dbLayer, config);
                    updated.add(config);
                } else {
                    JocInventory.updateConfiguration(dbLayer, config);
                }
                if(config.getTypeAsEnum().equals(ConfigurationType.DEPLOYMENTDESCRIPTOR) 
                        || config.getTypeAsEnum().equals(ConfigurationType.DESCRIPTORFOLDER)) {
                    JocInventory.makeParentDirs(dbLayer, p.getParent(), config.getAuditLogId(), ConfigurationType.DESCRIPTORFOLDER);
                } else {
                    JocInventory.makeParentDirs(dbLayer, p.getParent(), config.getAuditLogId(), ConfigurationType.FOLDER);
                }
                for (DBItemInventoryConfiguration item : oldDBFolderContent) {
                    if (deletedIds.remove(item.getId())) {
                        config.setId(null);
                        JocInventory.insertConfiguration(dbLayer, item);
                        updated.add(item);
                    } else {
                        JocInventory.updateConfiguration(dbLayer, item);
                    }
                }
                DependencyResolver.updateDependencies(updated);
                response.setPath(config.getPath());
                response.setId(config.getId());
                
                events.add(newPath);
                folderEvents.add(newFolder);
                
            } else {
                if (!newPath.equalsIgnoreCase(config.getPath())) { // if not only upper-lower case is changed then check if target exists
                    DBItemInventoryConfiguration targetItem = dbLayer.getConfiguration(newPath, config.getType());
                    
                    if (targetItem != null) {
                        throw new JocObjectAlreadyExistException(String.format("%s %s already exists", 
                                ConfigurationType.fromValue(config.getType()).value().toLowerCase(), targetItem.getPath()));
                    } else {
                        // check unique name
                        List<DBItemInventoryConfiguration> namedItems = dbLayer.getConfigurationByName(p.getFileName().toString(), config.getType());
                        if (namedItems != null) {
                            namedItems.remove(config);
                            if (!namedItems.isEmpty()) {
                                throw new JocObjectAlreadyExistException(String.format("The name has to be unique: '%s' is already used in '%s'", p
                                        .getFileName().toString(), namedItems.get(0).getPath()));
                            }
                        }
                    }
                }
                
                events.addAll(JocInventory.deepCopy(config, p.getFileName().toString(), dbLayer));
                
                DBItemJocAuditLogDetails auditLogDetail = JocAuditLog.storeAuditLogDetail(new AuditLogDetail(config.getPath(), config.getType()),
                        session, dbAuditLog);
                setItem(config, p, dbAuditLog.getId());
                
                //rename TAGGINGS
                boolean isRename = !oldPath.getFileName().toString().equals(p.getFileName().toString());
                if (isRename && ConfigurationType.WORKFLOW.equals(config.getTypeAsEnum())) {
                    String newName = p.getFileName().toString();
                    Date now = Date.from(Instant.now());
                    InventoryTagDBLayer dbTagLayer = new InventoryTagDBLayer(session);
                    for (DBItemInventoryTagging tagging : dbTagLayer.getTaggings(config.getId())) {
                        tagging.setName(newName);
                        tagging.setModified(now);
                        dbTagLayer.getSession().update(tagging);
                    }
                    new InventoryJobTagDBLayer(session).renameWorkflow(config.getId(), newName, now);
                }
                
                JocInventory.updateConfiguration(dbLayer, config);
                JocAuditObjectsLog.log(auditLogDetail, dbAuditLog.getId());
                
                if(config.getTypeAsEnum().equals(ConfigurationType.DEPLOYMENTDESCRIPTOR) 
                        || config.getTypeAsEnum().equals(ConfigurationType.DESCRIPTORFOLDER)) {
                    JocInventory.makeParentDirs(dbLayer, p.getParent(), config.getAuditLogId(), ConfigurationType.DESCRIPTORFOLDER);
                } else {
                    JocInventory.makeParentDirs(dbLayer, p.getParent(), config.getAuditLogId(), ConfigurationType.FOLDER);
                }
                response.setPath(config.getPath());
                response.setId(config.getId());
                
                events.add(newFolder);
            }
            
            Globals.commit(session);
            events.forEach(JocInventory::postEvent);
            folderEvents.forEach(JocInventory::postFolderEvent);
            
            // post event: InventoryTaggingUpdated
            if (workflowInvIds != null && !workflowInvIds.isEmpty()) {
                InventoryTagDBLayer dbTagLayer = new InventoryTagDBLayer(session);
                dbTagLayer.getTags(workflowInvIds).stream().distinct().forEach(JocInventory::postTaggingEvent);
            }

            response.setDeliveryDate(Date.from(Instant.now()));
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(response));
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private static void setItem(DBItemInventoryConfiguration oldItem, java.nio.file.Path newItem, Long auditLogId) {
        boolean itemIsRenamed = false;
        boolean itemIsMoved = false;
        if(!oldItem.getName().equals(newItem.getFileName().toString())) {
            itemIsRenamed = true;
        }
        if(!oldItem.getFolder().equals(newItem.getParent().toString().replace('\\', '/'))) {
            itemIsMoved = true;
        }
        oldItem.setPath(newItem.toString().replace('\\', '/'));
        oldItem.setFolder(newItem.getParent().toString().replace('\\', '/'));
        oldItem.setName(newItem.getFileName().toString());
        if(itemIsRenamed) {
            oldItem.setDeployed(false);
            oldItem.setReleased(false);
        }
        oldItem.setAuditLogId(auditLogId);
        oldItem.setModified(Date.from(Instant.now()));
        if(itemIsMoved && !itemIsRenamed) {
            // TODO: post events
            postNewDepHistoryEntryEvent(oldItem.getName(), newItem.getParent().toString().replace('\\', '/'), oldItem.getType(), oldItem.getId(), auditLogId);
        }
    }

    private static void postNewDepHistoryEntryEvent(String name, String folder, Integer objectType, Long inventoryId, Long auditLogId) {
        EventBus.getInstance().post(new DeploymentHistoryMoveEvent(name, folder, objectType, inventoryId, auditLogId));
    }
    
}
