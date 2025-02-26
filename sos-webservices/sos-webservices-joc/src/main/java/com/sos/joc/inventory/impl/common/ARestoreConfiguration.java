package com.sos.joc.inventory.impl.common;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.dependencies.DependencyResolver;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryConfigurationTrash;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.InventoryTagDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.ResponseNewPath;
import com.sos.joc.model.inventory.restore.RequestFilter;

public abstract class ARestoreConfiguration extends JOCResourceImpl {

    public JOCDefaultResponse restore(RequestFilter in, String request, boolean forDescriptors) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(request);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            InventoryTagDBLayer tagDbLayer = new InventoryTagDBLayer(session);
            
            session.beginTransaction();
            DBItemInventoryConfigurationTrash config = JocInventory.getTrashConfiguration(dbLayer, in, folderPermissions);
            ConfigurationType type = config.getTypeAsEnum();
            
            final java.nio.file.Path oldPath = Paths.get(config.getPath());
            java.nio.file.Path pWithoutFix = oldPath;
            String newFolder = config.getFolder();
            String newPathWithoutFix = config.getPath();
            // without any prefix/suffix
            if (in.getNewPath() != null && !in.getNewPath().isEmpty()) {
                pWithoutFix = Paths.get(config.getFolder()).resolve(in.getNewPath()).normalize();
                boolean newFolderIsRootFolder = JocInventory.ROOT_FOLDER.equals(pWithoutFix.toString().replace('\\', '/'));
                newFolder = newFolderIsRootFolder ? JocInventory.ROOT_FOLDER : pWithoutFix.getParent().toString().replace('\\', '/');
                newPathWithoutFix = pWithoutFix.toString().replace('\\', '/');
            }
            
            ConfigurationGlobalsJoc clusterSettings = Globals.getConfigurationGlobalsJoc();
            final List<String> replace = JocInventory.getSearchReplace(JocInventory.getSuffixPrefix(in.getSuffix(), in.getPrefix(), ClusterSettings
                    .getRestoreSuffixPrefix(clusterSettings), clusterSettings.getRestoreSuffix().getDefault(), config.getName(), type, dbLayer));

            Set<String> events = Collections.emptySet();
            List<Long> workflowInvIds = new ArrayList<>();
            ResponseNewPath response = new ResponseNewPath();
            response.setObjectType(type);
            List<DBItemInventoryConfiguration> updated = new ArrayList<DBItemInventoryConfiguration>();
            if (JocInventory.isFolder(type)) {
                
                if (!folderPermissions.isPermittedForFolder(newPathWithoutFix)) {
                    throw new JocFolderPermissionsException("Access denied for folder: " + newPathWithoutFix);
                }
                
                List<ConfigurationType> restoreOrder = Arrays.asList(ConfigurationType.FOLDER, ConfigurationType.DESCRIPTORFOLDER,
                        ConfigurationType.DEPLOYMENTDESCRIPTOR, ConfigurationType.REPORT, ConfigurationType.LOCK, ConfigurationType.INCLUDESCRIPT,
                        ConfigurationType.NOTICEBOARD, ConfigurationType.JOBRESOURCE, ConfigurationType.NONWORKINGDAYSCALENDAR,
                        ConfigurationType.WORKINGDAYSCALENDAR, ConfigurationType.JOBTEMPLATE, ConfigurationType.WORKFLOW,
                        ConfigurationType.FILEORDERSOURCE, ConfigurationType.SCHEDULE);

                List<DBItemInventoryConfigurationTrash> trashDBFolderContent = 
                        dbLayer.getTrashFolderContent(config.getPath(), true, null, JocInventory.isDescriptor(config.getTypeAsEnum()));
                Map<ConfigurationType, List<DBItemInventoryConfigurationTrash>> trashMap = trashDBFolderContent.stream().collect(Collectors
                        .groupingBy(DBItemInventoryConfigurationTrash::getTypeAsEnum));
                
                List<DBItemInventoryConfiguration> curDBFolderContent = 
                        dbLayer.getFolderContent(newPathWithoutFix, true, null, JocInventory.isDescriptor(config.getTypeAsEnum()));
                Set<String> folderPaths = curDBFolderContent.stream()
                        .filter(i -> ConfigurationType.FOLDER.intValue() == i.getType() 
                            || ConfigurationType.DESCRIPTORFOLDER.intValue() == i.getType())
                        .map(DBItemInventoryConfiguration::getPath).collect(Collectors.toSet());
                DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog());
                
                for (ConfigurationType objType : restoreOrder) {
                    for (DBItemInventoryConfigurationTrash trashItem : trashMap.getOrDefault(objType, Collections.emptyList())) {
                        java.nio.file.Path oldItemPath = Paths.get(trashItem.getPath());
                        DBItemInventoryConfiguration item = null;
                        if (ConfigurationType.FOLDER.intValue() == trashItem.getType() 
                                || ConfigurationType.DESCRIPTORFOLDER.intValue() == trashItem.getType()) {
                            if (!folderPaths.contains(trashItem.getPath())) {
                                item = createItem(trashItem, pWithoutFix.resolve(oldPath.relativize(oldItemPath)), dbAuditLog.getId(), dbLayer);
                                JocInventory.insertConfiguration(dbLayer, item);
                                updated.add(item);
                            }
                        } else {
                            List<DBItemInventoryConfiguration> targetItems = dbLayer.getConfigurationByName(trashItem.getName(), trashItem.getType());
                            if (targetItems.isEmpty()) {
                                if (in.getPrefix() != null || in.getSuffix() != null) {
                                    item = createItem(trashItem, pWithoutFix.resolve(oldPath.relativize(oldItemPath.getParent().resolve(trashItem
                                            .getName().replaceFirst(replace.get(0), replace.get(1))))), dbAuditLog.getId(), dbLayer);
                                    JocInventory.insertConfiguration(dbLayer, item);
                                } else {
                                    item = createItem(trashItem, pWithoutFix.resolve(oldPath.relativize(oldItemPath)), dbAuditLog.getId(), dbLayer);
                                    JocInventory.insertConfiguration(dbLayer, item);
                                }
                                restoreTaggings(item, pWithoutFix.getFileName().toString(), tagDbLayer, workflowInvIds);
                            } else {
                                item = createItem(trashItem, pWithoutFix.resolve(oldPath.relativize(oldItemPath.getParent().resolve(trashItem
                                        .getName().replaceFirst(replace.get(0), replace.get(1))))), dbAuditLog.getId(), dbLayer);
                                JocInventory.insertConfiguration(dbLayer, item);
                                restoreTaggings(item, tagDbLayer, workflowInvIds);
                            }
                            updated.add(item);
                        }
                    }
                }

                if (!JocInventory.ROOT_FOLDER.equals(config.getPath())) {
                    DBItemInventoryConfiguration newItem = null;
                    if(forDescriptors) {
                        newItem = dbLayer.getConfiguration(newPathWithoutFix, ConfigurationType.DESCRIPTORFOLDER.intValue());
                    } else {
                        newItem = dbLayer.getConfiguration(newPathWithoutFix, ConfigurationType.FOLDER.intValue());
                    }

                    if (newItem == null) {
                        DBItemInventoryConfiguration newDbItem = createItem(config, pWithoutFix, dbAuditLog.getId(), dbLayer);
                        JocInventory.insertConfiguration(dbLayer, newDbItem);
                        restoreTaggings(newDbItem, tagDbLayer, workflowInvIds);
                        if (newDbItem.getTypeAsEnum().equals(ConfigurationType.DEPLOYMENTDESCRIPTOR) 
                                || newDbItem.getTypeAsEnum().equals(ConfigurationType.DESCRIPTORFOLDER)) {
                            JocInventory.makeParentDirs(dbLayer, pWithoutFix.getParent(), dbAuditLog.getId(), ConfigurationType.DESCRIPTORFOLDER);
                        } else {
                            JocInventory.makeParentDirs(dbLayer, pWithoutFix.getParent(), dbAuditLog.getId(), ConfigurationType.FOLDER);
                        }
                        response.setId(newDbItem.getId());
                        response.setPath(newDbItem.getPath());
                    } else {
                        response.setId(newItem.getId());
                        response.setPath(newItem.getPath());
                    }
                } else {
                    response.setId(0L);
                    response.setPath("/");
                }
                
                dbLayer.deleteTrashFolder(config.getPath());
                
            } else {
                
                if (!folderPermissions.isPermittedForFolder(newFolder)) {
                    throw new JocFolderPermissionsException("Access denied for folder: " + newFolder);
                }
                
                // Check Java variable name rules
                for (int i = 0; i < pWithoutFix.getNameCount(); i++) {
                    if (i == pWithoutFix.getNameCount() - 1) {
                        SOSCheckJavaVariableName.test("name", pWithoutFix.getName(i).toString());
                    } else {
                        SOSCheckJavaVariableName.test("folder", pWithoutFix.getName(i).toString());
                    }
                }
                
                DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog());
                if (dbLayer.getConfigurationByName(pWithoutFix.getFileName().toString(), config.getType()).isEmpty()) {
                    DBItemInventoryConfiguration dbItem = null;
                    if(in.getPrefix() != null || in.getSuffix() != null) {
                        dbItem = createItem(config, pWithoutFix.getParent().resolve(pWithoutFix.getFileName().toString()
                                .replaceFirst(replace.get(0), replace.get(1))), dbAuditLog.getId(), dbLayer);
                    } else {
                        dbItem = createItem(config, pWithoutFix, dbAuditLog.getId(), dbLayer);
                    }
                    JocInventory.insertConfiguration(dbLayer, dbItem);
                    updated.add(dbItem);
                    restoreTaggings(dbItem, pWithoutFix.getFileName().toString(), tagDbLayer, workflowInvIds);
                    response.setId(dbItem.getId());
                    response.setPath(dbItem.getPath());
                } else {
                    DBItemInventoryConfiguration dbItem = createItem(config, pWithoutFix.getParent().resolve(pWithoutFix.getFileName().toString()
                            .replaceFirst(replace.get(0), replace.get(1))), dbAuditLog.getId(), dbLayer);
                    JocInventory.insertConfiguration(dbLayer, dbItem);
                    updated.add(dbItem);
                    restoreTaggings(dbItem, tagDbLayer, workflowInvIds);
                    response.setId(dbItem.getId());
                    response.setPath(dbItem.getPath());
                }
                if (config.getTypeAsEnum().equals(ConfigurationType.DEPLOYMENTDESCRIPTOR)) {
                    JocInventory.makeParentDirs(dbLayer, pWithoutFix.getParent(), dbAuditLog.getId(), ConfigurationType.DESCRIPTORFOLDER);
                } else {
                    JocInventory.makeParentDirs(dbLayer, pWithoutFix.getParent(), dbAuditLog.getId(), ConfigurationType.FOLDER);
                }
                session.delete(config);
                events = Collections.singleton(config.getFolder());
            }

            session.commit();
            if (JocInventory.isFolder(type)) {
                JocInventory.postFolderEvent(newFolder);
                JocInventory.postTrashFolderEvent(config.getFolder());
                JocInventory.postEvent(newPathWithoutFix);
            } else {
                for (String event : events) {
                    JocInventory.postEvent(event);
                    JocInventory.postTrashEvent(event);
                }
            }
            if (workflowInvIds != null && !workflowInvIds.isEmpty()) {
                tagDbLayer.getTags(workflowInvIds).stream().distinct().forEach(JocInventory::postTaggingEvent);
            }
            DependencyResolver.updateDependencies(updated);
            response.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(response);
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private static void restoreTaggings(DBItemInventoryConfiguration dbItem, InventoryTagDBLayer tagDbLayer, List<Long> workflowInvIds) {
        if (JocInventory.isWorkflow(dbItem.getType())) {
            if (tagDbLayer.hasTaggings(dbItem.getName(), dbItem.getType())) {
                if (tagDbLayer.update(dbItem.getName(), dbItem.getType(), dbItem.getId()) > 0) {
                    workflowInvIds.add(dbItem.getId());
                }
            }
        }
    }
    
    private static void restoreTaggings(DBItemInventoryConfiguration dbItem, String trashName, InventoryTagDBLayer tagDbLayer,
            List<Long> workflowInvIds) {
        if (JocInventory.isWorkflow(dbItem.getType())) {
            if (tagDbLayer.hasTaggings(trashName, dbItem.getType())) {
                if (tagDbLayer.update(trashName, dbItem.getName(), dbItem.getType(), dbItem.getId()) > 0) {
                    workflowInvIds.add(dbItem.getId());
                }
            }
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
    
    private static DBItemInventoryConfiguration createItem(DBItemInventoryConfigurationTrash oldItem, java.nio.file.Path newItem, Long auditLogId,
            InventoryDBLayer dbLayer) {
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
        item.setTitle(oldItem.getTitle());
        item.setType(oldItem.getType());
        item.setContent(oldItem.getContent());
        item.setValid(validate(item, dbLayer));
        return item;
    }

}
