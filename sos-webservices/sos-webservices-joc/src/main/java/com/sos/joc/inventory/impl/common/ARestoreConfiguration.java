package com.sos.joc.inventory.impl.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
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
            Set<DBItemInventoryConfiguration> updated = new HashSet<>();
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
            
            Set<DBItemInventoryConfiguration> referendByItems = new HashSet<>();
            for (DBItemInventoryConfiguration item : updated) {
                referendByItems.addAll(getReferendByItems(item, dbLayer));
            }
            updated.addAll(referendByItems);
            
            DependencyResolver.updateDependencies(updated);
            response.setDeliveryDate(Date.from(Instant.now()));
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(response));
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
    
    private static Set<DBItemInventoryConfiguration> getReferendByItems(DBItemInventoryConfiguration config,
            InventoryDBLayer dbLayer) throws JsonParseException, JsonMappingException, SOSHibernateException, JsonProcessingException,
                IOException {
        Set<DBItemInventoryConfiguration> referendByItems = new HashSet<>();
        switch (config.getTypeAsEnum()) {
        case LOCK: // determine Workflows with Lock instructions
            Set<DBItemInventoryConfiguration> workflows = dbLayer.getUsedWorkflowsByLockId(config.getName());
            if (workflows != null && !workflows.isEmpty()) {
                referendByItems.addAll(workflows);
            }
            break;
        case JOBRESOURCE: // determine Workflows and JobTemplates with Jobs containing JobResource
            Set<DBItemInventoryConfiguration> workflows2 = dbLayer.getUsedWorkflowsByJobResource(config.getName());
            if (workflows2 != null && !workflows2.isEmpty()) {
                referendByItems.addAll(workflows2);
            }
            List<DBItemInventoryConfiguration> jobTemplates = dbLayer.getUsedJobTemplatesByJobResource(config.getName());
            if (jobTemplates != null && !jobTemplates.isEmpty()) {
                referendByItems.addAll(jobTemplates);
            }
            break;

        case NOTICEBOARD: // determine Workflows with PostNotice or ExpectNotice reference
            Set<DBItemInventoryConfiguration> workflow3 = dbLayer.getUsedWorkflowsByBoardName(config.getName());
            if (workflow3 != null && !workflow3.isEmpty()) {
                referendByItems.addAll(workflow3);
            }
            break;

        case WORKFLOW: // determine Schedules and FileOrderSources with Workflow reference
            List<DBItemInventoryConfiguration> schedules = dbLayer.getUsedSchedulesByWorkflowName(config.getName());
            if (schedules != null && !schedules.isEmpty()) {
                referendByItems.addAll(schedules);
            }
            List<DBItemInventoryConfiguration> fileOrderSources = dbLayer.getUsedFileOrderSourcesByWorkflowName(config.getName());
            if (fileOrderSources != null && !fileOrderSources.isEmpty()) {
                referendByItems.addAll(fileOrderSources);
            }
            Set<DBItemInventoryConfiguration> addOrderWorkflows = dbLayer.getUsedWorkflowsByAddOrdersWorkflowName(config.getName());
            if (addOrderWorkflows != null && !addOrderWorkflows.isEmpty()) {
                referendByItems.addAll(addOrderWorkflows);
            }
            break;
            
        case WORKINGDAYSCALENDAR: // determine Schedules and Calendars with Calendar reference
        case NONWORKINGDAYSCALENDAR:
            List<DBItemInventoryConfiguration> schedules1 = dbLayer.getUsedSchedulesByCalendarName(config.getName());
            if (schedules1 != null && !schedules1.isEmpty()) {
                referendByItems.addAll(schedules1);
            }
            
            List<DBItemInventoryConfiguration> calendars = dbLayer.getUsedCalendarsByCalendarName(config.getName());
            if (calendars != null && !calendars.isEmpty()) {
                referendByItems.addAll(calendars);
            }
            
            break;
            
        case INCLUDESCRIPT: // determine Workflows with script reference in INCLUDE line of a job script
            List<DBItemInventoryConfiguration> workflowsOrJobTemplates = dbLayer.getWorkflowsAndJobTemplatesWithIncludedScripts();
            if (workflowsOrJobTemplates != null && !workflowsOrJobTemplates.isEmpty()) {
                referendByItems.addAll(workflowsOrJobTemplates);
            }
            break;
            
//        case JOBTEMPLATE:
//            Set<DBItemInventoryConfiguration> workflows5 = dbLayer.getUsedWorkflowsByJobTemplateName(config.getName());
//            if (workflows5 != null && !workflows5.isEmpty()) {
//                for (DBItemInventoryConfiguration workflow : workflows5) {
//                    boolean changed = false;
//                    Workflow w = Globals.objectMapper.readValue(workflow.getContent(), Workflow.class);
//                    if (w.getJobs() != null) {
//                        for (Map.Entry<String, Job> entry : w.getJobs().getAdditionalProperties().entrySet()) {
//                            Job j = entry.getValue();
//                            if (j.getJobTemplate() != null && j.getJobTemplate().getName() != null && j.getJobTemplate().getName().equals(config
//                                    .getName())) {
//                                j.getJobTemplate().setName(newName);
//                                changed = true;
//                            }
//                        }
//                    }
//                    if (changed) { // TODO is it really ok that setDeployed(false)? I think, NO
//                        workflow.setContent(Globals.objectMapper.writeValueAsString(w));
//                        //workflow.setDeployed(false);
//                        int i = items.indexOf(workflow);
//                        if (i != -1) {
//                            items.get(i).setContent(workflow.getContent());
//                            //items.get(i).setDeployed(false);
//                        } else {
//                            JocInventory.updateConfiguration(dbLayer, workflow);
//                            events.add(workflow.getFolder());
//                        }
//                    }
//                }
//            }
//            break;
//            
        default:
            break;
        }
        return referendByItems;
    }

}
