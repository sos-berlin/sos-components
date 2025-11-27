package com.sos.joc.inventory.impl.common;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.jobtemplate.JobTemplate;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.dependencies.DependencyResolver;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.JsonConverter;
import com.sos.joc.classes.inventory.WorkflowConverter;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.exceptions.JocObjectAlreadyExistException;
import com.sos.joc.model.SuffixPrefix;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.ResponseNewPath;
import com.sos.joc.model.inventory.copy.RequestFilter;

public abstract class ACopyConfiguration extends JOCResourceImpl {

    public JOCDefaultResponse copy(RequestFilter in, String request) throws Exception {
        return copy(in, false, request);
    }
    
    public JOCDefaultResponse copy(RequestFilter in, boolean forDescriptors, String request) throws Exception {
        SOSHibernateSession session = null;
        List<DBItemInventoryConfiguration> updated = new ArrayList<DBItemInventoryConfiguration>();
        try {
            session = Globals.createSosHibernateStatelessConnection(request);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            
            session.beginTransaction();
            DBItemInventoryConfiguration config = JocInventory.getConfiguration(dbLayer, in, folderPermissions);
            ConfigurationType type = config.getTypeAsEnum();
            
            final java.nio.file.Path oldPath = Paths.get(config.getPath());
            final String oldFolder = config.getFolder();
            // without any prefix/suffix
            java.nio.file.Path pWithoutFix = Paths.get(oldFolder).resolve(in.getNewPath()).normalize();
            boolean newFolderIsRootFolder = JocInventory.ROOT_FOLDER.equals(pWithoutFix.toString().replace('\\', '/'));
            String newFolder = newFolderIsRootFolder ? JocInventory.ROOT_FOLDER : pWithoutFix.getParent().toString().replace('\\', '/');
            String newPathWithoutFix = pWithoutFix.toString().replace('\\', '/');
            String newFilename = newFolderIsRootFolder ? "" : pWithoutFix.getFileName().toString();
            
            // folder copy or (object copy where target and source name are the same)
            boolean fixMustUsed = JocInventory.isFolder(type) || (!JocInventory.isFolder(type) && oldPath.getFileName().toString().equals(newFilename));
            
            ConfigurationGlobalsJoc clusterSettings = Globals.getConfigurationGlobalsJoc();
            SuffixPrefix suffixPrefix = new SuffixPrefix(); 
            if (fixMustUsed) {
                // JOC-1232: newFilename contains sub folder in case of type folder
                //              suffix contains highest suffix of folder /
                //              DB: ... "likefolder" like '%'
                // Determine items of folder then determine SuffixPrefix per item
                // this has to be done for each item of the folder separately
                suffixPrefix = JocInventory.getSuffixPrefix(in.getSuffix(), in.getPrefix(), ClusterSettings.getCopyPasteSuffixPrefix(clusterSettings),
                        clusterSettings.getCopyPasteSuffix().getDefault(), newFilename, type, dbLayer);
            } else {
                suffixPrefix.setPrefix("");
                suffixPrefix.setSuffix("");
            }
            
            final List<String> replace = JocInventory.getSearchReplace(suffixPrefix);
            Set<String> events = Collections.emptySet();
            Set<String> folderEvents = Collections.emptySet();
            ResponseNewPath response = new ResponseNewPath();
            response.setObjectType(type);
            
            // Check folder permissions
            if (JocInventory.isFolder(type)) {
                if (!folderPermissions.isPermittedForFolder(newPathWithoutFix)) {
                    throw new JocFolderPermissionsException("Access denied for folder: " + newPathWithoutFix);
                }
                // Check Java variable name rules
                for (int i = 0; i < pWithoutFix.getNameCount(); i++) {
                    if (i == pWithoutFix.getNameCount() - 1) {
                        SOSCheckJavaVariableName.test("name", pWithoutFix.getName(i).toString());
                    } else {
                        SOSCheckJavaVariableName.test("folder", pWithoutFix.getName(i).toString());
                    }
                }
                
                List<DBItemInventoryConfiguration> oldDBFolderContent = null;
                if (forDescriptors) {
                    oldDBFolderContent = dbLayer.getFolderContent(
                            config.getPath(), true, Collections.singletonList(ConfigurationType.DEPLOYMENTDESCRIPTOR.intValue()), true);
                } else if (in.getShallowCopy()) {
                    oldDBFolderContent = dbLayer.getFolderContent(config.getPath(), true, JocInventory.getTypesFromObjectsWithReferencesAndFolders(), false);
                } else {
                    oldDBFolderContent = dbLayer.getFolderContent(config.getPath(), true, null, false);
                }
                if (oldDBFolderContent == null) {
                    oldDBFolderContent = Collections.emptyList();
                }

                List<Integer> typesForReferences = Arrays.asList(ConfigurationType.WORKFLOW.intValue(), ConfigurationType.JOBTEMPLATE.intValue(),
                        ConfigurationType.WORKINGDAYSCALENDAR.intValue(), ConfigurationType.NONWORKINGDAYSCALENDAR.intValue(), ConfigurationType.LOCK
                                .intValue(), ConfigurationType.NOTICEBOARD.intValue(), ConfigurationType.JOBRESOURCE.intValue(),
                        ConfigurationType.INCLUDESCRIPT.intValue());

                // JOC-1232: FIX
                List<AuditLogDetail> auditLogDetails = new ArrayList<>();
                Map<ConfigurationType, Map<String, String>> oldToNewName = new HashMap<ConfigurationType, Map<String,String>>();
                List<DBItemInventoryConfiguration> newDBFolderItems = new ArrayList<DBItemInventoryConfiguration>(); 
                for (DBItemInventoryConfiguration oldDBFolderItem : oldDBFolderContent) {
                    java.nio.file.Path oldItemPath = Paths.get(oldDBFolderItem.getPath());
                    String oldName = oldDBFolderItem.getName();
                    DBItemInventoryConfiguration newDbItem = null; 
                    if (!JocInventory.isFolder(oldDBFolderItem.getTypeAsEnum())) {
                        SuffixPrefix folderItemSuffixPrefix = new SuffixPrefix(); 
                        if (fixMustUsed) {
                            folderItemSuffixPrefix = JocInventory.getSuffixPrefix(in.getSuffix(), in.getPrefix(), 
                                    ClusterSettings.getCopyPasteSuffixPrefix(clusterSettings), clusterSettings.getCopyPasteSuffix().getDefault(),
                                    oldDBFolderItem.getName(), oldDBFolderItem.getTypeAsEnum(), dbLayer);
                        } else {
                            folderItemSuffixPrefix.setPrefix("");
                            folderItemSuffixPrefix.setSuffix("");
                        }
                        final List<String> folderItemReplace = JocInventory.getSearchReplace(folderItemSuffixPrefix);
                        
                        java.nio.file.Path itemPath = oldItemPath;
                        if (!folderItemSuffixPrefix.getSuffix().isEmpty() || !folderItemSuffixPrefix.getPrefix().isEmpty()) {
                            itemPath = pWithoutFix.resolve(oldPath.relativize(oldItemPath));
                            String newName = oldItemPath.getFileName().toString().replaceFirst(folderItemReplace.get(0), folderItemReplace.get(1));
                            itemPath = itemPath.getParent().resolve(newName);
                        }
                        newDbItem = createItem(oldDBFolderItem, itemPath);
                        String newName = newDbItem.getName();
                        auditLogDetails.add(new AuditLogDetail(itemPath, newDbItem.getType()));
                        
                        if(typesForReferences.contains(newDbItem.getType()) && !in.getShallowCopy()) {
                            if (oldToNewName.containsKey(newDbItem.getTypeAsEnum())) {
                                oldToNewName.get(newDbItem.getTypeAsEnum()).put(oldName, newName);
                            } else {
                                Map<String,String> oldNewName = new HashMap<String, String>();
                                oldNewName.put(oldName, newName);
                                oldToNewName.put(newDbItem.getTypeAsEnum(), oldNewName);
                            }
                        }
                        newDBFolderItems.add(newDbItem);
                        JocInventory.insertConfiguration(dbLayer, newDbItem);
                    } else {
                        // folder
                        java.nio.file.Path newItemPath = pWithoutFix.resolve(oldPath.relativize(oldItemPath));
                        if(!newItemPath.toString().replace('\\', '/').equals(JocInventory.ROOT_FOLDER)) {
                            newDbItem = createItem(oldDBFolderItem, newItemPath);
                            newDBFolderItems.add(newDbItem);
                            DBItemInventoryConfiguration alreadyExists = dbLayer.getConfiguration(newDbItem.getPath(), newDbItem.getType());
                            if (alreadyExists != null) {
                                newDBFolderItems.add(alreadyExists);
                            } else {
                                JocInventory.insertConfiguration(dbLayer, newDbItem);
                            }
                        }
                    }
                }
//                updated = newDBFolderItems;
                oldDBFolderContent = newDBFolderItems;
//                Map<ConfigurationType, Map<String, String>> oldToNewName = (!in.getShallowCopy()) ? oldDBFolderContent.stream().filter(
//                        item -> typesForReferences.contains(item.getType())).collect(Collectors.groupingBy(
//                                DBItemInventoryConfiguration::getTypeAsEnum, Collectors.toMap(DBItemInventoryConfiguration::getName, item -> item
//                                        .getName().replaceFirst(replace.get(0), replace.get(1))))) : Collections.emptyMap();
                
//                List<AuditLogDetail> auditLogDetails = new ArrayList<>();
//                oldDBFolderContent = oldDBFolderContent.stream().map(oldItem -> {
//                    java.nio.file.Path oldItemPath = Paths.get(oldItem.getPath());
//                    if (ConfigurationType.FOLDER.intValue() == oldItem.getType()) {
//                        return createItem(oldItem, pWithoutFix.resolve(oldPath.relativize(oldItemPath)));
//                    }
//                    auditLogDetails.add(new AuditLogDetail(oldItemPath, oldItem.getType()));
//                    return createItem(oldItem, pWithoutFix.resolve(oldPath.relativize(oldItemPath.getParent().resolve(oldItem.getName()
//                            .replaceFirst(replace.get(0), replace.get(1))))));
//                }).collect(Collectors.toList());

//                List<DBItemInventoryConfiguration> newDBFolderContent = null;
//                if (in.getShallowCopy()) {
//                    newDBFolderContent = dbLayer.getFolderContent(newPathWithoutFix, true, JocInventory.getTypesFromObjectsWithReferencesAndFolders());
//                } else {
//                    newDBFolderContent = dbLayer.getFolderContent(newPathWithoutFix, true, null);
//                }
//
//                if (newDBFolderContent != null && !newDBFolderContent.isEmpty()) {
//                    newDBFolderContent.retainAll(oldDBFolderContent);
//                    if (!newDBFolderContent.isEmpty()) {
//                        Map<Boolean, List<DBItemInventoryConfiguration>> map = newDBFolderContent.stream().collect(Collectors.groupingBy(
//                                item -> ConfigurationType.FOLDER.intValue() == item.getType()));
//                        if (!map.getOrDefault(false, Collections.emptyList()).isEmpty()) { // all not folder items
//                            throw new JocObjectAlreadyExistException("Cannot move to " + newPathWithoutFix + ": common objects are " + map.get(false).stream()
//                                    .map(DBItemInventoryConfiguration::getPath).collect(Collectors.joining("', '", "'", "'")));
//                        }
//                        
//                        oldDBFolderContent.removeAll(map.getOrDefault(true, Collections.emptyList()));
//                    }
//                }
                
                // JOC-1232: FIX END

                DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog(), auditLogDetails);
                
                if (!JocInventory.ROOT_FOLDER.equals(config.getPath())) {
                    DBItemInventoryConfiguration newItem = null;
                    if(forDescriptors) {
                        newItem = dbLayer.getConfiguration(newPathWithoutFix, ConfigurationType.DESCRIPTORFOLDER.intValue());
                    } else {
                        newItem = dbLayer.getConfiguration(newPathWithoutFix, ConfigurationType.FOLDER.intValue());
                    }
                    if (newItem == null) {
                        if (!newFolderIsRootFolder) {
                            DBItemInventoryConfiguration newDbItem = createItem(config, pWithoutFix);
                            newDbItem.setAuditLogId(dbAuditLog.getId());
                            JocInventory.insertConfiguration(dbLayer, newDbItem);
                            if(forDescriptors) {
                                JocInventory.makeParentDirs(dbLayer, pWithoutFix.getParent(), newDbItem.getAuditLogId(), ConfigurationType.DESCRIPTORFOLDER);
                            } else {
                                JocInventory.makeParentDirs(dbLayer, pWithoutFix.getParent(), newDbItem.getAuditLogId(), ConfigurationType.FOLDER);
                            }
                            response.setId(newDbItem.getId());
                            response.setPath(newDbItem.getPath());
                        } else {
                            response.setId(0L);
                            response.setPath("/");
                        }
                    } else if (!oldDBFolderContent.isEmpty()) {
                        response.setId(newItem.getId());
                        response.setPath(newItem.getPath());
                    }
                } else {
                    response.setId(0L);
                    response.setPath("/");
                }
                if (in.getShallowCopy()) {
                    for (DBItemInventoryConfiguration item : oldDBFolderContent) {
                        item.setAuditLogId(dbAuditLog.getId());
                        // JOC-1232
                        JocInventory.updateConfiguration(dbLayer, item);
                    }
                    updated = oldDBFolderContent;
                } else {
                    for (DBItemInventoryConfiguration item : oldDBFolderContent) {
                        item.setAuditLogId(dbAuditLog.getId());
                        String json = item.getContent();
                        boolean jsonIsChanged = true;
                        switch (item.getTypeAsEnum()) {
                        case WORKFLOW:
                            for (Map.Entry<String, String> oldNewName : oldToNewName.getOrDefault(ConfigurationType.LOCK, Collections.emptyMap())
                                    .entrySet()) {
                                json = json.replaceAll("(\"lockName\"\\s*:\\s*\")" + oldNewName.getKey() + "\"", "$1" + oldNewName.getValue() + "\"");
                            }
                            // addOrder Instructions
                            for (Map.Entry<String, String> oldNewName : oldToNewName.getOrDefault(ConfigurationType.WORKFLOW, Collections.emptyMap())
                                    .entrySet()) {
                                json = json.replaceAll("(\"workflowName\"\\s*:\\s*\")" + oldNewName.getKey() + "\"", "$1" + oldNewName.getValue() + "\"");
                            }
                            // include scripts
                            for (Map.Entry<String, String> oldNewName : oldToNewName.getOrDefault(ConfigurationType.INCLUDESCRIPT, Collections.emptyMap())
                                    .entrySet()) {
                                json = JsonConverter.replaceNameOfIncludeScript(json, oldNewName.getKey(), oldNewName.getValue());
                            }
                            // notice, notices Instructions, jobResources, jobTemplate
                            Map<String, String> oldNewBoardNames = oldToNewName.getOrDefault(ConfigurationType.NOTICEBOARD, Collections.emptyMap());
                            Map<String, String> oldNewJobResourceNames = oldToNewName.getOrDefault(ConfigurationType.JOBRESOURCE, Collections.emptyMap());
                            Map<String, String> oldNewJobTemplateNames = oldToNewName.getOrDefault(ConfigurationType.JOBTEMPLATE, Collections.emptyMap());
                            if (oldNewBoardNames.size() > 0 || oldNewJobResourceNames.size() > 0 || oldNewJobTemplateNames.size() > 0) {
                                Workflow w = WorkflowConverter.convertInventoryWorkflow(json);

                                if (oldNewBoardNames.size() > 0) {
                                    WorkflowsHelper.updateWorkflowBoardname(oldNewBoardNames, w.getInstructions());
                                }
                                if (oldNewJobResourceNames.size() > 0 || oldNewJobTemplateNames.size() > 0) {
                                    // JobResources on Workflow level
                                    if (oldNewJobResourceNames.size() > 0 && w.getJobResourceNames() != null) {
                                        w.setJobResourceNames(w.getJobResourceNames().stream().map(s -> oldNewJobResourceNames.getOrDefault(s, s))
                                                .collect(Collectors.toList()));
                                    }
                                    // JobResources on Job level and JobTemplate.name
                                    if (w.getJobs() != null) {
                                        w.getJobs().getAdditionalProperties().forEach((k, v) -> {
                                            if (oldNewJobResourceNames.size() > 0 && v.getJobResourceNames() != null) {
                                                v.setJobResourceNames(v.getJobResourceNames().stream().map(s -> oldNewJobResourceNames.getOrDefault(s,
                                                        s)).collect(Collectors.toList()));
                                            }
                                            if (oldNewJobTemplateNames.size() > 0 && v.getJobTemplate() != null) {
                                                String oldName = v.getJobTemplate().getName();
                                                if (oldName != null) {
                                                    v.getJobTemplate().setName(oldNewJobTemplateNames.getOrDefault(oldName, oldName));
                                                }
                                            }
                                        });
                                    }
                                }
                                
                                json = Globals.objectMapper.writeValueAsString(w);
                            }
                            break;
                        case FILEORDERSOURCE:
                            for (Map.Entry<String, String> oldNewName : oldToNewName.getOrDefault(ConfigurationType.WORKFLOW, Collections.emptyMap())
                                    .entrySet()) {
                                json = json.replaceAll("(\"workflowName\"\\s*:\\s*\")" + oldNewName.getKey() + "\"", "$1" + oldNewName.getValue()
                                        + "\"");
                            }
                            break;
                        case SCHEDULE:
                            Map<String, String> oldNewWorkflowNames = oldToNewName.getOrDefault(ConfigurationType.WORKFLOW, Collections.emptyMap());
                            Map<String, String> oldNewNonWorkingDaysCalendarNames = oldToNewName.getOrDefault(ConfigurationType.NONWORKINGDAYSCALENDAR,
                                    Collections.emptyMap());
                            if (!oldNewWorkflowNames.isEmpty() || !oldNewNonWorkingDaysCalendarNames.isEmpty()) {
                                Schedule sc = JocInventory.convertSchedule(json, Schedule.class);
                                if (!oldNewWorkflowNames.isEmpty()) {
                                    if (sc.getWorkflowName() != null) {
                                        sc.setWorkflowName(oldNewWorkflowNames.getOrDefault(sc.getWorkflowName(), sc.getWorkflowName())); 
                                    }
                                    if (sc.getWorkflowNames() != null) {
                                        sc.setWorkflowNames(sc.getWorkflowNames().stream().map(s -> oldNewWorkflowNames.getOrDefault(s, s))
                                                .collect(Collectors.toList()));
                                    } 
                                }
                                if (!oldNewNonWorkingDaysCalendarNames.isEmpty()) {
                                    if (sc.getCalendars() != null) {
                                        sc.getCalendars().forEach(ac -> {
                                            if (ac.getCalendarName() != null) {
                                                ac.setCalendarName(oldNewNonWorkingDaysCalendarNames.getOrDefault(ac.getCalendarName(), ac
                                                        .getCalendarName()));
                                            }
                                            if (ac.getExcludes() != null && ac.getExcludes().getNonWorkingDayCalendars() != null) {
                                                ac.getExcludes().setNonWorkingDayCalendars(ac.getExcludes().getNonWorkingDayCalendars().stream().map(
                                                        s -> oldNewNonWorkingDaysCalendarNames.getOrDefault(s, s)).collect(Collectors.toSet()));
                                            }
                                        });
                                    }
                                }
                                json = Globals.objectMapper.writeValueAsString(sc);
//                                for (Map.Entry<String, String> oldNewName : oldNewWorkflowNames.entrySet()) {
//                                    json = json.replaceAll("(\"workflowName\"\\s*:\\s*\")" + oldNewName.getKey() + "\"", "$1" + oldNewName.getValue()
//                                            + "\"");
//                                }
//                                if (sc.getWorkflowNames() != null) {
//                                    sc.setWorkflowNames(sc.getWorkflowNames().stream().map(s -> oldNewWorkflowNames.getOrDefault(s, s))
//                                            .collect(Collectors.toList()));
//                                    json = Globals.objectMapper.writeValueAsString(sc);
//                                }
                            }
                            for (Map.Entry<String, String> oldNewName : oldToNewName.getOrDefault(ConfigurationType.WORKINGDAYSCALENDAR, Collections
                                    .emptyMap()).entrySet()) {
                                json = json.replaceAll("(\"calendarName\"\\s*:\\s*\")" + oldNewName.getKey() + "\"", "$1" + oldNewName.getValue()
                                        + "\"");
                            }
//                            for (Map.Entry<String, String> oldNewName : oldToNewName.getOrDefault(ConfigurationType.NONWORKINGDAYSCALENDAR,
//                                    Collections.emptyMap()).entrySet()) {
//                                json = json.replaceAll("(\"calendarName\"\\s*:\\s*\")" + oldNewName.getKey() + "\"", "$1" + oldNewName.getValue()
//                                        + "\"");
//                            }
                            break;
                        case WORKINGDAYSCALENDAR:
                        case NONWORKINGDAYSCALENDAR:
                            Map<String, String> oldNewNonWorkingDaysCalendarNames1 = oldToNewName.getOrDefault(ConfigurationType.NONWORKINGDAYSCALENDAR,
                                    Collections.emptyMap());
                            if (!oldNewNonWorkingDaysCalendarNames1.isEmpty()) {
                                Calendar calendar = (Calendar) JocInventory.content2IJSObject(json, ConfigurationType.WORKINGDAYSCALENDAR);
                                if (calendar.getExcludes() != null && calendar.getExcludes().getNonWorkingDayCalendars() != null) {
                                    calendar.getExcludes().setNonWorkingDayCalendars(calendar.getExcludes().getNonWorkingDayCalendars().stream().map(
                                            s -> oldNewNonWorkingDaysCalendarNames1.getOrDefault(s, s)).collect(Collectors.toSet()));
                                }
                                json = Globals.objectMapper.writeValueAsString(calendar);
                            }
                            break;
                        case JOBTEMPLATE:
                            // include scripts
                            for (Map.Entry<String, String> oldNewName : oldToNewName.getOrDefault(ConfigurationType.INCLUDESCRIPT, Collections.emptyMap())
                                    .entrySet()) {
                                json = JsonConverter.replaceNameOfIncludeScript(json, oldNewName.getKey(), oldNewName.getValue());
                            }
                            // JobResources
                            Map<String, String> oldNewJobResourceNames2 = oldToNewName.getOrDefault(ConfigurationType.JOBRESOURCE, Collections.emptyMap());
                            if (oldNewJobResourceNames2.size() > 0) {
                                JobTemplate jt = JocInventory.convertJobTemplate(json, JobTemplate.class);
                                if (jt.getJobResourceNames() != null) {
                                    jt.setJobResourceNames(jt.getJobResourceNames().stream().map(s -> oldNewJobResourceNames2.getOrDefault(s, s))
                                            .collect(Collectors.toList()));
                                    json = Globals.objectMapper.writeValueAsString(jt);
                                }
                            }
                        default:
                            jsonIsChanged = false;
                            break;
                        }
                        if (jsonIsChanged) {
                            item.setContent(json);
                            JocInventory.updateConfiguration(dbLayer, item);
                        }
                    }
                    updated = oldDBFolderContent;
                }
                
                events = Collections.singleton(newPathWithoutFix);
                folderEvents = Collections.singleton(newFolder);
                
            } else {
                if (!folderPermissions.isPermittedForFolder(newFolder)) {
                    throw new JocFolderPermissionsException("Access denied for folder: " + newFolder);
                }
                
                java.nio.file.Path p = pWithoutFix;
                if (!suffixPrefix.getSuffix().isEmpty() || !suffixPrefix.getPrefix().isEmpty()) {
                    p = pWithoutFix.getParent().resolve(pWithoutFix.getFileName().toString().replaceFirst(replace.get(0), replace.get(1)));
                }
                // Check Java variable name rules
                for (int i = 0; i < p.getNameCount(); i++) {
                    if (i == p.getNameCount() - 1) {
                        SOSCheckJavaVariableName.test("name", p.getName(i).toString());
                    } else {
                        SOSCheckJavaVariableName.test("folder", p.getName(i).toString());
                    }
                }
                String newPath = p.toString().replace('\\', '/');
                DBItemInventoryConfiguration targetItem = dbLayer.getConfiguration(newPath, config.getType());

                if (targetItem != null) { // this can occur if prefix/suffix is badly chosen
                    throw new JocObjectAlreadyExistException(String.format("%s %s already exists", ConfigurationType.fromValue(config.getType())
                            .value().toLowerCase(), targetItem.getPath()));
                } else {
                    // check unique name - this can occur if prefix/suffix is badly chosen
                    List<DBItemInventoryConfiguration> namedItems = dbLayer.getConfigurationByName(p.getFileName().toString(), config.getType());
                    if (namedItems != null) {
                        namedItems.remove(config);
                        if (!namedItems.isEmpty()) {
                            throw new JocObjectAlreadyExistException(String.format("The name has to be unique: '%s' is already used in '%s'", p
                                    .getFileName().toString(), namedItems.get(0).getPath()));
                        }
                    }
                }
                
                DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog(), Collections.singleton(
                        new AuditLogDetail(oldPath, config.getType())));
                DBItemInventoryConfiguration newDbItem = createItem(config, p);
                //createAuditLog(newDbItem, in.getAuditLog());
                newDbItem.setAuditLogId(dbAuditLog.getId());
                JocInventory.insertConfiguration(dbLayer, newDbItem);
                updated.add(newDbItem);
                if(forDescriptors) {
                    JocInventory.makeParentDirs(dbLayer, p.getParent(), newDbItem.getAuditLogId(), ConfigurationType.DESCRIPTORFOLDER);
                } else {
                    JocInventory.makeParentDirs(dbLayer, p.getParent(), newDbItem.getAuditLogId(), ConfigurationType.FOLDER);
                }
                response.setId(newDbItem.getId());
                response.setPath(newDbItem.getPath());
                events = Collections.singleton(newDbItem.getFolder());
            }
            session.commit();
            if(!updated.isEmpty()) {
                DependencyResolver.updateDependencies(updated);
            }
            for (String event : events) {
                JocInventory.postEvent(event);
            }
            for (String event : folderEvents) {
                JocInventory.postFolderEvent(event);
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

    private static DBItemInventoryConfiguration createItem(DBItemInventoryConfiguration oldItem, java.nio.file.Path newItem) {
        DBItemInventoryConfiguration item = new DBItemInventoryConfiguration();
        item.setId(null);
        item.setPath(newItem.toString().replace('\\', '/'));
        item.setFolder(newItem.getParent().toString().replace('\\', '/'));
        item.setName(newItem.getFileName().toString());
        item.setDeployed(false);
        item.setReleased(false);
        item.setModified(Date.from(Instant.now()));
        item.setCreated(Date.from(Instant.now()));
        item.setDeleted(false);
        item.setAuditLogId(0L);
        item.setTitle(oldItem.getTitle());
        item.setType(oldItem.getType());
        item.setValid(oldItem.getValid());
        item.setContent(oldItem.getContent());
        return item;
    }

}
