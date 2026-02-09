package com.sos.joc.publish.util;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.JsonConverter;
import com.sos.joc.classes.inventory.JsonSerializer;
import com.sos.joc.classes.order.OrderTags;
import com.sos.joc.classes.tag.GroupedTag;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.InventoryJobTagDBLayer;
import com.sos.joc.db.inventory.InventoryOrderTagDBLayer;
import com.sos.joc.db.inventory.InventoryTagDBLayer;
import com.sos.joc.db.inventory.items.InventoryJobTagItem;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.Version;
import com.sos.joc.model.agent.transfer.Agent;
import com.sos.joc.model.common.IDeployObject;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.search.ResponseBaseSearchItem;
import com.sos.joc.model.joc.JocMetaInfo;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.ControllerObject;
import com.sos.joc.model.publish.DeployablesValidFilter;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.model.publish.folder.ExportFolderFilter;
import com.sos.joc.model.tag.AddOrdersOrderTags;
import com.sos.joc.model.tag.ExportedJobTagItem;
import com.sos.joc.model.tag.ExportedJobTagItems;
import com.sos.joc.model.tag.ExportedOrderTags;
import com.sos.joc.model.tag.ExportedTagItem;
import com.sos.joc.model.tag.ExportedTaggedObject;
import com.sos.joc.model.tag.ExportedTags;
import com.sos.joc.model.tag.FileOrderSourceOrderTags;
import com.sos.joc.model.tag.ScheduleOrderTags;
import com.sos.joc.publish.common.ConfigurationObjectFileExtension;
import com.sos.joc.publish.common.ControllerObjectFileExtension;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.UpdateableFileOrderSourceAgentName;
import com.sos.joc.publish.mapper.UpdateableWorkflowJobAgentName;
import com.sos.sign.model.board.Board;
import com.sos.sign.model.fileordersource.FileOrderSource;
import com.sos.sign.model.jobclass.JobClass;
import com.sos.sign.model.jobresource.JobResource;
import com.sos.sign.model.lock.Lock;
import com.sos.sign.model.workflow.Workflow;

import jakarta.ws.rs.core.StreamingOutput;

public class ExportUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportUtils.class);
    private static final String AGENT_FILE_EXTENSION = ".agent.json";
    public static final String TAGS_ENTRY_OLD_NAME = "workflow.tags.json";
    public static final String TAGS_ENTRY_NAME = "tags.json";
    
    public static final Map<ConfigurationType, String> extensionMap = Collections.unmodifiableMap(new HashMap<ConfigurationType, String>() {

        private static final long serialVersionUID = 1L;

        {
            put(ConfigurationType.WORKINGDAYSCALENDAR, ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.toString());
            put(ConfigurationType.NONWORKINGDAYSCALENDAR, ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.toString());
            put(ConfigurationType.JOBTEMPLATE, ConfigurationObjectFileExtension.JOBTEMPLATE_FILE_EXTENSION.toString());
            put(ConfigurationType.JOBCLASS, ControllerObjectFileExtension.JOBCLASS_FILE_EXTENSION.toString());
            put(ConfigurationType.JOBRESOURCE, ControllerObjectFileExtension.JOBRESOURCE_FILE_EXTENSION.toString());
            put(ConfigurationType.LOCK, ControllerObjectFileExtension.LOCK_FILE_EXTENSION.toString());
            put(ConfigurationType.FILEORDERSOURCE, ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION.toString());
            put(ConfigurationType.SCHEDULE, ConfigurationObjectFileExtension.SCHEDULE_FILE_EXTENSION.toString());
            put(ConfigurationType.INCLUDESCRIPT, ConfigurationObjectFileExtension.SCRIPT_FILE_EXTENSION.toString());
            put(ConfigurationType.WORKFLOW, ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.toString());
            put(ConfigurationType.NOTICEBOARD, ControllerObjectFileExtension.NOTICEBOARD_FILE_EXTENSION.toString());
            put(ConfigurationType.REPORT, ConfigurationObjectFileExtension.REPORT_FILE_EXTENSION.toString());
        }
    });

    
    public static Set<ControllerObject> getFolderControllerObjectsForSigning(ExportFolderFilter filter, String account, DBLayerDeploy dbLayer,
            String commitId) throws SOSHibernateException {
        Map<String, List<ControllerObject>> allObjects = new HashMap<String, List<ControllerObject>>();
        if (filter != null && filter.getForSigning() != null) {
            Set<DBItemDeploymentHistory> allDeployedItems = new HashSet<DBItemDeploymentHistory>();
            Set<DBItemInventoryConfiguration> allDraftItems = new HashSet<DBItemInventoryConfiguration>();
            final List<ConfigurationType> filterTypes = filter.getForSigning().getObjectTypes();
            final List<String> folderPaths = filter.getForSigning().getFolders();
            final String controllerId = filter.getForSigning().getControllerId();
            final boolean recursive = filter.getForSigning().getRecursive();
            final Map<String, String> releasedScripts = dbLayer.getReleasedScripts();
            if(!filter.getForSigning().getWithoutDeployed()) {
                allDeployedItems.addAll(getLatestActiveDepHistoryEntriesWithoutDraftsFromFolders(folderPaths, recursive, controllerId, dbLayer));
                allDeployedItems.stream().filter(Objects::nonNull).filter(item -> filterTypes.contains(ConfigurationType.fromValue(item.getType())))
                    .forEach(item -> {
                        if(allObjects.containsKey(item.getName())) {
                            allObjects.get(item.getName()).add(getContollerObjectFromDBItem(item, commitId, account, releasedScripts));
                        } else {
                            allObjects.put(item.getName(), new ArrayList<ControllerObject>());
                            allObjects.get(item.getName()).add(getContollerObjectFromDBItem(item, commitId, account, releasedScripts));
                        }
                    });
            }
            if(!filter.getForSigning().getWithoutDrafts()) {
                allDraftItems.addAll(getDeployableInventoryConfigurationsfromFolders(folderPaths, recursive, dbLayer));
                allDraftItems.stream().filter(Objects::nonNull).filter(dbItem -> filterTypes.contains(dbItem.getTypeAsEnum())).forEach(
                        item -> {
                            if(allObjects.containsKey(item.getName())) {
                                allObjects.get(item.getName()).add(mapInvConfigToJSObject(item, account, commitId, releasedScripts));
                            } else {
                                allObjects.put(item.getName(), new ArrayList<ControllerObject>());
                                allObjects.get(item.getName()).add(mapInvConfigToJSObject(item, account, commitId, releasedScripts));
                            }
                        });
            }
        }
        return allObjects.values().stream().flatMap(List::stream).collect(Collectors.toSet());
//        return new HashSet<ControllerObject>(allObjects.values());
    }
    
    public static Set<ConfigurationObject> getFolderConfigurationObjectsForShallowCopy(ExportFolderFilter filter, String account,
            DBLayerDeploy dbLayer) throws SOSHibernateException {
        Set<ConfigurationObject> allObjects = new HashSet<ConfigurationObject>();
        if (filter != null && filter.getShallowCopy() != null) {
            Set<DBItemInventoryReleasedConfiguration> allReleasedItems = new HashSet<DBItemInventoryReleasedConfiguration>();
            Set<DBItemDeploymentHistory> allDeployedItems = new HashSet<DBItemDeploymentHistory>();
            Set<DBItemInventoryConfiguration> allDraftItems = new HashSet<DBItemInventoryConfiguration>();
            final Set<ConfigurationType> filterTypes = filter.getShallowCopy().getObjectTypes();
            final List<String> folderPaths = filter.getShallowCopy().getFolders();
            final boolean recursive = filter.getShallowCopy().getRecursive();
            if (!filter.getShallowCopy().getWithoutDeployed()) {
                allDeployedItems.addAll(getLatestActiveDepHistoryEntriesWithoutDraftsFromFolders(folderPaths, recursive, null, dbLayer));
                allDeployedItems.stream().filter(Objects::nonNull).filter(item -> filterTypes.contains(ConfigurationType.fromValue(item.getType())))
                        .map(ExportUtils::getConfigurationObjectFromDBItem).forEach(item -> allObjects.add(item));
            }
            if (!filter.getShallowCopy().getWithoutReleased()) {
                allReleasedItems.addAll(getReleasedInventoryConfigurationsfromFoldersWithoutDrafts(folderPaths, recursive, dbLayer));
                allReleasedItems.stream().filter(Objects::nonNull).filter(item -> filterTypes.contains(ConfigurationType.fromValue(item.getType())))
                        .map(PublishUtils::getConfigurationObjectFromDBItem).forEach(item -> allObjects.add(item));
            }
            if (!filter.getShallowCopy().getWithoutDrafts()) {
                allDraftItems.addAll(getDeployableInventoryConfigurationsfromFolders(folderPaths, recursive, dbLayer));
                allDraftItems.addAll(getReleasableInventoryConfigurationsWithoutReleasedfromFolders(folderPaths, recursive, dbLayer));
                if (filter.getShallowCopy().getOnlyValidObjects()) {
                    allDraftItems = allDraftItems.stream().filter(Objects::nonNull)
                            .filter(dbItem -> filterTypes.contains(dbItem.getTypeAsEnum()))
                            .filter(DBItemInventoryConfiguration::getValid).collect(Collectors.toSet());
                } else {
                    allDraftItems = allDraftItems.stream().filter(Objects::nonNull)
                            .filter(dbItem -> filterTypes.contains(dbItem.getTypeAsEnum())).collect(Collectors.toSet());
                }
                allDraftItems.stream().map(PublishUtils::getConfigurationObjectFromDBItem).forEach(cfg -> allObjects.add(cfg));
            }
        }
        return allObjects;
    }
    

    public static Set<ControllerObject> getDeployableControllerObjectsFromDB(DeployablesValidFilter filter, DBLayerDeploy dbLayer, String commitId,
            String account)
            throws DBConnectionRefusedException, DBInvalidDataException, JocMissingRequiredParameterException, DBMissingDataException, IOException,
            SOSHibernateException {
        Set<ControllerObject> allObjects = new HashSet<ControllerObject>();
        if (filter != null) {
            if (filter.getDeployConfigurations() != null && !filter.getDeployConfigurations().isEmpty()) {
                List<Configuration> depFolders = filter.getDeployConfigurations().stream().filter(item -> item.getConfiguration().getObjectType()
                        .equals(ConfigurationType.FOLDER)).map(item -> item.getConfiguration()).collect(Collectors.toList());
                Set<DBItemDeploymentHistory> allItems = new HashSet<DBItemDeploymentHistory>();
                if (depFolders != null && !depFolders.isEmpty()) {
                    allItems.addAll(PublishUtils.getLatestActiveDepHistoryEntriesWithoutDraftsFromFolders(depFolders, dbLayer));
                }
                List<DBItemDeploymentHistory> deploymentDbItems = dbLayer.getFilteredDeployments(filter);
                if (deploymentDbItems != null && !deploymentDbItems.isEmpty()) {
                    allItems.addAll(deploymentDbItems);
                }
                if (!allItems.isEmpty()) {
                    final Map<String, String> releasedScripts = dbLayer.getReleasedScripts();
                    allItems.stream().filter(Objects::nonNull).filter(item -> !item.getType().equals(ConfigurationType.FOLDER.intValue())).forEach(
                            item -> {
                                if (commitId != null) {
                                    dbLayer.storeCommitIdForLaterUsage(item, commitId);
                                }
                                allObjects.add(getContollerObjectFromDBItem(item, commitId, account, releasedScripts));
                            });
                }
            }
            if (filter.getDraftConfigurations() != null && !filter.getDraftConfigurations().isEmpty()) {
                List<Configuration> draftFolders = filter.getDraftConfigurations().stream().filter(item -> item.getConfiguration().getObjectType()
                        .equals(ConfigurationType.FOLDER)).map(item -> item.getConfiguration()).collect(Collectors.toList());
                Set<DBItemInventoryConfiguration> allItems = new HashSet<DBItemInventoryConfiguration>();
                if (draftFolders != null && !draftFolders.isEmpty()) {
                    allItems.addAll(PublishUtils.getDeployableInventoryConfigurationsfromFolders(draftFolders, dbLayer));
                }
                List<DBItemInventoryConfiguration> configurationDbItems = dbLayer.getFilteredDeployableConfigurations(filter);
                if (configurationDbItems != null && !configurationDbItems.isEmpty()) {
                    allItems.addAll(configurationDbItems);
                }
                if (!allItems.isEmpty()) {
                    final Map<String, String> releasedScripts = dbLayer.getReleasedScripts();
                    allItems.stream().filter(Objects::nonNull).filter(item -> !item.getTypeAsEnum().equals(ConfigurationType.FOLDER)).forEach(
                            item -> {
                                if (commitId != null) {
                                    dbLayer.storeCommitIdForLaterUsage(item, commitId);
                                }
                                allObjects.add(mapInvConfigToJSObject(item, account, commitId, releasedScripts));
                            });
                }
            }
        }
        return allObjects;
    }

    private static ControllerObject mapInvConfigToJSObject(DBItemInventoryConfiguration item, String account, String commitId,
            Map<String, String> releasedScripts) {
        try {
            ControllerObject jsObject = new ControllerObject();
            jsObject.setPath(item.getPath());
            jsObject.setObjectType(DeployType.fromValue(item.getType()));
            jsObject.setContent(JsonConverter.readAsConvertedDeployObject(null, item.getPath(), item.getContent(), StoreDeployments.CLASS_MAPPING.get(
                    item.getType()), commitId, releasedScripts));
            jsObject.setAccount(account);
            jsObject.setModified(item.getModified());
            return jsObject;
        } catch (IOException e) {
            throw new JocException(e);
        }
    }

    private static ControllerObject getContollerObjectFromDBItem(DBItemDeploymentHistory item, String commitId, String account,
            Map<String, String> releasedScripts) {
        try {
            ControllerObject jsObject = new ControllerObject();
            jsObject.setPath(item.getPath());
            jsObject.setObjectType(item.getTypeAsEnum());
            if (releasedScripts != null) {
                jsObject.setContent(JsonConverter.readAsConvertedDeployObject(item.getControllerId(), item.getPath(), item.getInvContent(),
                        StoreDeployments.CLASS_MAPPING.get(item.getType()), commitId, releasedScripts));
            } else {
                jsObject.setContent(Globals.objectMapper.readValue(item.getInvContent(), IDeployObject.class));
            }
            jsObject.setVersion(item.getVersion());
            jsObject.setAccount(account);
            return jsObject;
        } catch (IOException e) {
            throw new JocException(e);
        }
    }

    private static Set<DBItemDeploymentHistory> getLatestActiveDepHistoryEntriesFromFolders(List<String> folders, boolean recursive,
            String controllerId, DBLayerDeploy dbLayer) {
        Map<String, Optional<DBItemDeploymentHistory>> groupedEntries = 
                folders.stream().map(item -> dbLayer.getDepHistoryItemsFromFolder(item, controllerId, recursive)).flatMap(List::stream)
                .collect(Collectors.groupingBy(item -> item.getType() + ":" + item.getName(), 
                        Collectors.maxBy(Comparator.comparing(DBItemDeploymentHistory::getId))));
        return groupedEntries.values().stream().filter(Optional::isPresent).map(Optional::get)
                .filter(item -> OperationType.DELETE.value() != item.getOperation()).collect(Collectors.toSet());
    }

    private static Set<DBItemDeploymentHistory> getLatestActiveDepHistoryEntriesWithoutDraftsFromFolders(List<String> folders, boolean recursive,
            String controllerId, DBLayerDeploy dbLayer) {
        Set<DBItemDeploymentHistory> allLatest = getLatestActiveDepHistoryEntriesFromFolders(folders, recursive, controllerId, dbLayer);
        List<DBItemInventoryConfiguration> allCfgs = new ArrayList<DBItemInventoryConfiguration>(); 
        folders.stream().forEach(item -> allCfgs.addAll(dbLayer.getDeployableInventoryConfigurationsByFolder(item, recursive)));
        allLatest = allLatest.stream().filter(item -> {
            DBItemInventoryConfiguration dbItem = allCfgs.stream()
//                    .filter(cfg -> cfg.getName().equals(item.getName()) && cfg.getType().equals(item.getType())).findFirst().orElse(null);
                    .filter(cfg -> cfg.getId().equals(item.getInventoryConfigurationId())).findFirst().orElse(null);
            if (dbItem != null && item.getPath().equals(dbItem.getPath())) {
                return true;
             } else {
                return false;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
        allLatest = allLatest.stream().filter(item -> {
            if (item.getName() == null || item.getName().isEmpty()) {
                LOGGER.debug(String.format("No name found for item with path: %1$s ", item.getPath()));
                String name = Paths.get(item.getPath()).getFileName().toString();
                item.setName(name);
                LOGGER.debug(String.format("Item name set to: %1$s ", item.getName()));
            }
            DBItemInventoryConfiguration dbItem = allCfgs.stream()
                    .filter(cfg -> cfg.getName().equals(item.getName()) && cfg.getType().equals(item.getType())).findFirst().orElse(null);
            if(dbItem != null) {
                return dbItem.getDeployed();
            } else {
                // history items source does not exist in current configuration
                // decision: ignore item as only objects from history with existing current configuration are relevant
                return false;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
        return allLatest;
    }

    private static Set<DBItemInventoryConfiguration> getDeployableInventoryConfigurationsfromFolders(List<String> folders, boolean recursive,
            DBLayerDeploy dbLayer) {
        List<DBItemInventoryConfiguration> entries = new ArrayList<DBItemInventoryConfiguration>();
        folders.stream().forEach(item -> entries.addAll(dbLayer.getDeployableInventoryConfigurationsByFolderWithoutDeployed(item, recursive)));
        return entries.stream().collect(Collectors.toSet());
    }

    private static Set<DBItemInventoryConfiguration> getReleasableInventoryConfigurationsWithoutReleasedfromFolders(List<String> folders,
            boolean recursive, DBLayerDeploy dbLayer) {
        List<DBItemInventoryConfiguration> entries = new ArrayList<DBItemInventoryConfiguration>();
        folders.stream().forEach(item -> entries.addAll(dbLayer.getReleasableInventoryConfigurationsByFolderWithoutReleased(item, recursive)));
        return entries.stream().collect(Collectors.toSet());
    }

    private static Set<DBItemInventoryReleasedConfiguration> getReleasedInventoryConfigurationsfromFoldersWithoutDrafts(List<String> folders,
            boolean recursive, DBLayerDeploy dbLayer) {
        List<DBItemInventoryReleasedConfiguration> entries = new ArrayList<DBItemInventoryReleasedConfiguration>();
        folders.stream().forEach(item -> entries.addAll(dbLayer.getReleasedInventoryConfigurationsByFolder(item, recursive)));
        Set<DBItemInventoryReleasedConfiguration> allReleased = entries.stream().filter(item -> {
            DBItemInventoryConfiguration dbItem = dbLayer.getConfigurationByName(item.getName(), item.getType());
            if (dbItem != null && item.getPath().equals(dbItem.getPath())) {
                return true;
            } else {
                return false;
            }
        }).filter(item -> {
            if (item.getName() == null || item.getName().isEmpty()) {
                LOGGER.debug(String.format("No name found for item with path: %1$s ", item.getPath()));
                String name = Paths.get(item.getPath()).getFileName().toString();
                item.setName(name);
                LOGGER.debug(String.format("Item name set to: %1$s ", item.getName()));
            }
            Boolean released = dbLayer.getInventoryConfigurationReleasedByNameAndType(item.getName(), item.getType());
            if (released == null) {
                // released item does not exist in current configuration
                // decision: ignore item as only objects from released configurations with existing current configuration are relevant
                return false;
            } else {
                return released;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
        return allReleased;
    }
    
    private static ConfigurationObject getConfigurationObjectFromDBItem(DBItemDeploymentHistory item) {
        try {
            ConfigurationObject configurationObject = new ConfigurationObject();
            configurationObject.setId(item.getInventoryConfigurationId());
            configurationObject.setPath(item.getPath());
            configurationObject.setName(item.getName());
            configurationObject.setObjectType(ConfigurationType.fromValue(item.getType()));
            configurationObject.setConfiguration(JocInventory.content2IJSObject(item.getInvContent(), configurationObject.getObjectType()));
            return configurationObject;
        } catch (IOException e) {
            throw new JocException(e);
        }
    }
    
    public static StreamingOutput writeZipFileForSigning(Set<ControllerObject> deployables,
            Set<UpdateableWorkflowJobAgentName> updateableAgentNames, Set<UpdateableFileOrderSourceAgentName> updateableFOSAgentNames,
            String commitId, String controllerId, DBLayerDeploy dbLayer, Version jocVersion, Version apiVersion, Version inventoryVersion) {
        StreamingOutput streamingOutput = new StreamingOutput() {

            @Override
            public void write(OutputStream output) throws IOException {
                ZipOutputStream zipOut = null;
                try {
                    zipOut = new ZipOutputStream(new BufferedOutputStream(output), StandardCharsets.UTF_8);
                    byte[] contentBytes = null;
                    if (deployables != null && !deployables.isEmpty()) {
                        for (ControllerObject deployable : deployables) {
                            String extension = null;
                            switch (deployable.getObjectType()) {
                            case WORKFLOW:
                                extension = ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.toString();
                                Workflow workflow = (Workflow) deployable.getContent();
                                workflow.setVersionId(commitId);
                                // determine agent names to be replaced
                                if (controllerId != null && updateableAgentNames != null) {
                                    workflow.setPath(Paths.get(deployable.getPath()).getFileName().toString());
                                    PublishUtils.replaceAgentNameWithAgentId(workflow, updateableAgentNames, controllerId);
                                }
                                workflow.setPath(Paths.get(deployable.getPath()).getFileName().toString());
                                contentBytes = JsonSerializer.serializeAsBytes(workflow);
                                break;
                            case JOBRESOURCE:
                                extension = ControllerObjectFileExtension.JOBRESOURCE_FILE_EXTENSION.toString();
                                JobResource jobResource = (JobResource) deployable.getContent();
                                jobResource.setPath(Paths.get(deployable.getPath()).getFileName().toString());
                                contentBytes = JsonSerializer.serializeAsBytes(jobResource);
                                break;
                            case LOCK:
                                extension = ControllerObjectFileExtension.LOCK_FILE_EXTENSION.toString();
                                Lock lock = (Lock) deployable.getContent();
                                lock.setPath(Paths.get(deployable.getPath()).getFileName().toString());
                                contentBytes = JsonSerializer.serializeAsBytes(lock);
                                break;
                            case PLANNABLEBOARD:
                            case NOTICEBOARD:
                                extension = ControllerObjectFileExtension.NOTICEBOARD_FILE_EXTENSION.toString();
                                Board board = (Board) deployable.getContent();
                                board.setPath(Paths.get(deployable.getPath()).getFileName().toString());
                                contentBytes = JsonSerializer.serializeAsBytes(board);
                                break;
                            case JOBCLASS:
                                extension = ControllerObjectFileExtension.JOBCLASS_FILE_EXTENSION.toString();
                                JobClass jobClass = (JobClass) deployable.getContent();
                                contentBytes = JsonSerializer.serializeAsBytes(jobClass);
                                break;
                            case FILEORDERSOURCE:
                                extension = ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION.toString();
                                FileOrderSource fileOrderSource = (FileOrderSource) deployable.getContent();
                                // determine agent names to be replaced
                                if (controllerId != null && updateableAgentNames != null) {
                                    fileOrderSource.setPath(deployable.getPath());
                                    PublishUtils.replaceAgentNameWithAgentId(fileOrderSource, updateableFOSAgentNames, controllerId);
                                }
                                fileOrderSource.setPath(Paths.get(deployable.getPath()).getFileName().toString());
                                contentBytes = JsonSerializer.serializeAsBytes(fileOrderSource);
                                break;
                            }
                            String zipEntryName = deployable.getPath().substring(1).concat(extension);
                            ZipEntry entry = new ZipEntry(zipEntryName);
                            zipOut.putNextEntry(entry);
                            zipOut.write(contentBytes);
                            zipOut.closeEntry();
                        }
                    }
                    JocMetaInfo jocMetaInfo = createJocMetaInfo(jocVersion, apiVersion, inventoryVersion, commitId);
                    if (!ImportUtils.isJocMetaInfoNullOrEmpty(jocMetaInfo)) {
                        String zipEntryName = ImportUtils.JOC_META_INFO_FILENAME;
                        ZipEntry entry = new ZipEntry(zipEntryName);
                        zipOut.putNextEntry(entry);
                        zipOut.write(Globals.prettyPrintObjectMapper.writeValueAsBytes(jocMetaInfo));
                        zipOut.closeEntry();
                    }
                    zipOut.flush();
                } finally {
                    if (zipOut != null) {
                        try {
                            zipOut.close();
                        } catch (Exception e) {
                        }
                    }
                }
            }
        };
        return streamingOutput;
    }

//    public static StreamingOutput writeZipFileShallow(Set<ConfigurationObject> deployables, DBLayerDeploy dbLayer, Version jocVersion,
//            Version apiVersion, Version inventoryVersion, boolean relativePath, List<String> startPaths) {
//        return writeZipFileShallow(deployables, dbLayer, jocVersion, apiVersion, inventoryVersion, relativePath, startPaths, false);
//    }

    public static StreamingOutput writeZipFileShallow(Set<ConfigurationObject> deployables, DBLayerDeploy dbLayer, Version jocVersion,
            Version apiVersion, Version inventoryVersion, boolean relativePath, List<String> startPaths, boolean withAllTags) {
        ExportedTags tags = getTagsToExportFromConfigurationObjects(deployables, withAllTags, dbLayer.getSession());
        
        Map<String, String> groupedOrderTags = getGroupedOrderTags(deployables, dbLayer.getSession());

        StreamingOutput streamingOutput = new StreamingOutput() {

            @Override
            public void write(OutputStream output) throws IOException {
                ZipOutputStream zipOut = null;
                try {
                    zipOut = new ZipOutputStream(new BufferedOutputStream(output), StandardCharsets.UTF_8);
                    String content = null;
                    if (deployables != null && !deployables.isEmpty()) {
                        for (ConfigurationObject deployable : deployables) {
                            switch (deployable.getObjectType()) {
                            case WORKFLOW:
                                deployable.setConfiguration(OrderTags.addGroupsToInstructions((com.sos.inventory.model.workflow.Workflow) deployable
                                        .getConfiguration(), groupedOrderTags));
                                break;
                            case FILEORDERSOURCE:
                                deployable.setConfiguration(OrderTags.addGroupsToFileOrderSource(
                                        (com.sos.inventory.model.fileordersource.FileOrderSource) deployable.getConfiguration(), groupedOrderTags));
                                break;
                            case SCHEDULE:
                                deployable.setConfiguration(OrderTags.addGroupsToOrderPreparation(
                                        (com.sos.inventory.model.schedule.Schedule) deployable.getConfiguration(), groupedOrderTags));
                                break;
                            default:
                                break;
                            }
                            String extension = extensionMap.get(deployable.getObjectType());
                            if (extension != null) {
                                content = Globals.prettyPrintObjectMapper.writeValueAsString(deployable.getConfiguration());
                                String zipEntryName = null;
                                if (relativePath && startPaths != null && !startPaths.isEmpty()) {
                                    Optional<String> startPathOptional = startPaths.stream().filter(path -> deployable.getPath().startsWith(path))
                                            .findFirst();
                                    if (startPathOptional.isPresent()) {
                                        String startPath = startPathOptional.get();
                                        String startFolder = Paths.get(startPath).getFileName().toString();
                                        Path path = Paths.get(deployable.getPath());
                                        if (startPath != null && path.startsWith(startPath)) {
                                            zipEntryName = startFolder.concat("/").concat(Paths.get(startPath).relativize(path).toString().replace(
                                                    '\\', '/').concat(extension));
                                        }
                                    } else {
                                        zipEntryName = deployable.getPath().substring(1).concat(extension);
                                    }
                                } else {
                                    zipEntryName = deployable.getPath().substring(1).concat(extension);
                                }
                                ZipEntry entry = new ZipEntry(zipEntryName);
                                zipOut.putNextEntry(entry);
                                zipOut.write(content.getBytes());
                                zipOut.closeEntry();
                            }
                        }
                    }
                    if (tags != null) {
                        if ((tags.getTags() != null && !tags.getTags().isEmpty()) || (tags.getJobTags() != null && !tags.getJobTags().isEmpty())) {
                            ZipEntry tagEntry = new ZipEntry(TAGS_ENTRY_NAME);
                            zipOut.putNextEntry(tagEntry);
                            zipOut.write(Globals.prettyPrintObjectMapper.writeValueAsBytes(tags));
                            zipOut.closeEntry();
                        }
                    }
                    JocMetaInfo jocMetaInfo = createJocMetaInfo(jocVersion, apiVersion, inventoryVersion, null);
                    if (!ImportUtils.isJocMetaInfoNullOrEmpty(jocMetaInfo)) {
                        String zipEntryName = ImportUtils.JOC_META_INFO_FILENAME;
                        ZipEntry entry = new ZipEntry(zipEntryName);
                        zipOut.putNextEntry(entry);
                        zipOut.write(Globals.prettyPrintObjectMapper.writeValueAsBytes(jocMetaInfo));
                        zipOut.closeEntry();
                    }
                    zipOut.flush();
                } finally {
                    if (zipOut != null) {
                        try {
                            zipOut.close();
                        } catch (Exception e) {
                        }
                    }
                }
            }
        };
        return streamingOutput;
    }

    public static StreamingOutput writeTarGzipFileForSigning(Set<ControllerObject> deployables,
            Set<UpdateableWorkflowJobAgentName> updateableAgentNames, Set<UpdateableFileOrderSourceAgentName> updateableFOSAgentNames,
            String commitId, String controllerId, DBLayerDeploy dbLayer, Version jocVersion, Version apiVersion, Version inventoryVersion) {
        
        StreamingOutput streamingOutput = new StreamingOutput() {

            @Override
            public void write(OutputStream output) throws IOException {
                GZIPOutputStream gzipOut = null;
                TarArchiveOutputStream tarOut = null;
                BufferedOutputStream bOut = null;
                try {
                    bOut = new BufferedOutputStream(output);
                    gzipOut = new GZIPOutputStream(bOut);
                    tarOut = new TarArchiveOutputStream(gzipOut);
                    tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
                    byte[] contentBytes = null;
                    if (deployables != null && !deployables.isEmpty()) {
                        for (ControllerObject deployable : deployables) {
                            String extension = null;
                            switch (deployable.getObjectType()) {
                            case WORKFLOW:
                                extension = ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.toString();
                                Workflow workflow = (Workflow) deployable.getContent();
                                workflow.setVersionId(commitId);
                                workflow.setPath(Paths.get(deployable.getPath()).getFileName().toString());
                                if (controllerId != null && updateableAgentNames != null) {
                                    PublishUtils.replaceAgentNameWithAgentId(workflow, updateableAgentNames, controllerId);
                                }
                                contentBytes = JsonSerializer.serializeAsBytes(workflow);
                                break;
                            case JOBRESOURCE:
                                extension = ControllerObjectFileExtension.JOBRESOURCE_FILE_EXTENSION.toString();
                                JobResource jobResource = (JobResource) deployable.getContent();
                                jobResource.setPath(Paths.get(deployable.getPath()).getFileName().toString());
                                contentBytes = JsonSerializer.serializeAsBytes(jobResource);
                                break;
                            case LOCK:
                                extension = ControllerObjectFileExtension.LOCK_FILE_EXTENSION.toString();
                                Lock lock = (Lock) deployable.getContent();
                                lock.setPath(Paths.get(deployable.getPath()).getFileName().toString());
                                contentBytes = JsonSerializer.serializeAsBytes(lock);
                                break;
                            case PLANNABLEBOARD:
                            case NOTICEBOARD:
                                extension = ControllerObjectFileExtension.NOTICEBOARD_FILE_EXTENSION.toString();
                                Board board = (Board) deployable.getContent();
                                board.setPath(Paths.get(deployable.getPath()).getFileName().toString());
                                contentBytes = JsonSerializer.serializeAsBytes(board);
                                break;
                            case JOBCLASS:
                                extension = ControllerObjectFileExtension.JOBCLASS_FILE_EXTENSION.toString();
                                JobClass jobClass = (JobClass) deployable.getContent();
                                contentBytes = JsonSerializer.serializeAsBytes(jobClass);
                                break;
                            case FILEORDERSOURCE:
                                extension = ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION.toString();
                                FileOrderSource fileOrderSource = (FileOrderSource) deployable.getContent();
                                // determine agent names to be replaced
                                if (controllerId != null && updateableAgentNames != null) {
                                    PublishUtils.replaceAgentNameWithAgentId(fileOrderSource, updateableFOSAgentNames, controllerId);
                                }
                                contentBytes = JsonSerializer.serializeAsBytes(fileOrderSource);
                                break;
                            }
                            String zipEntryName = deployable.getPath().substring(1).concat(extension);
                            TarArchiveEntry entry = new TarArchiveEntry(zipEntryName);
                            entry.setSize(contentBytes.length);
                            tarOut.putArchiveEntry(entry);
                            tarOut.write(contentBytes);
                            tarOut.closeArchiveEntry();
                        }
                    }
                    JocMetaInfo jocMetaInfo = createJocMetaInfo(jocVersion, apiVersion, inventoryVersion, commitId);
                    if (!ImportUtils.isJocMetaInfoNullOrEmpty(jocMetaInfo)) {
                        String zipEntryName = ImportUtils.JOC_META_INFO_FILENAME;
                        TarArchiveEntry entry = new TarArchiveEntry(zipEntryName);
                        byte[] jocMetaInfoBytes = Globals.prettyPrintObjectMapper.writeValueAsBytes(jocMetaInfo);
                        entry.setSize(jocMetaInfoBytes.length);
                        tarOut.putArchiveEntry(entry);
                        tarOut.write(jocMetaInfoBytes);
                        tarOut.closeArchiveEntry();
                    }
                    tarOut.flush();
                } finally {
                    if (tarOut != null) {
                        try {
                            tarOut.finish();
                            tarOut.close();
                        } catch (Exception e) {
                        }
                    }
                    if (gzipOut != null) {
                        try {
                            gzipOut.flush();
                            gzipOut.close();
                        } catch (Exception e) {
                        }
                    }
                    if (bOut != null) {
                        try {
                            bOut.flush();
                            bOut.close();
                        } catch (Exception e) {
                        }
                    }

                }

            }
        };
        return streamingOutput;
    }

//    public static StreamingOutput writeTarGzipFileShallow(Set<ConfigurationObject> configurations, DBLayerDeploy dbLayer, Version jocVersion,
//            Version apiVersion, Version inventoryVersion, boolean relativePath, List<String> startPaths) throws Exception {
//        return writeTarGzipFileShallow(configurations, dbLayer, jocVersion, apiVersion, inventoryVersion, relativePath, startPaths, false);
//    }

    public static StreamingOutput writeTarGzipFileShallow(Set<ConfigurationObject> configurations, DBLayerDeploy dbLayer, Version jocVersion,
            Version apiVersion, Version inventoryVersion, boolean relativePath, List<String> startPaths, boolean withAllTags) throws Exception {
        ExportedTags tags = getTagsToExportFromConfigurationObjects(configurations, withAllTags, dbLayer.getSession());
        
        Map<String, String> groupedOrderTags = getGroupedOrderTags(configurations, dbLayer.getSession());

        StreamingOutput streamingOutput = new StreamingOutput() {

            @Override
            public void write(OutputStream output) throws IOException {
                GZIPOutputStream gzipOut = null;
                TarArchiveOutputStream tarOut = null;
                BufferedOutputStream bOut = null;
                try {
                    bOut = new BufferedOutputStream(output);
                    gzipOut = new GZIPOutputStream(bOut);
                    tarOut = new TarArchiveOutputStream(gzipOut);
                    tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
                    String content = null;
                    if (configurations != null && !configurations.isEmpty()) {
                        for (ConfigurationObject deployable : configurations) {
                            switch (deployable.getObjectType()) {
                            case WORKFLOW:
                                deployable.setConfiguration(OrderTags.addGroupsToInstructions((com.sos.inventory.model.workflow.Workflow) deployable
                                        .getConfiguration(), groupedOrderTags));
                                break;
                            case FILEORDERSOURCE:
                                deployable.setConfiguration(OrderTags.addGroupsToFileOrderSource(
                                        (com.sos.inventory.model.fileordersource.FileOrderSource) deployable.getConfiguration(), groupedOrderTags));
                                break;
                            case SCHEDULE:
                                deployable.setConfiguration(OrderTags.addGroupsToOrderPreparation(
                                        (com.sos.inventory.model.schedule.Schedule) deployable.getConfiguration(), groupedOrderTags));
                                break;
                            default:
                                break;
                            }
                            
                            String extension = extensionMap.get(deployable.getObjectType());
                            if (extension != null) {
                                content = Globals.prettyPrintObjectMapper.writeValueAsString(deployable.getConfiguration());
                                String zipEntryName = null;
                                if(relativePath && startPaths != null && !startPaths.isEmpty()) {
                                    Optional<String> startPathOptional = startPaths.stream().filter(path -> deployable.getPath().startsWith(path)).findFirst();
                                    if(startPathOptional.isPresent()) {
                                        String startPath = startPathOptional.get();
                                        String startFolder = Paths.get(startPath).getFileName().toString();
                                        Path path = Paths.get(deployable.getPath());
                                        if(startPath != null && path.startsWith(startPath)) {
                                            zipEntryName = startFolder.concat("/").concat(Paths.get(startPath).relativize(path).toString().replace('\\', '/').concat(extension));
                                        }
                                    } else {
                                        zipEntryName = deployable.getPath().substring(1).concat(extension);
                                    }
                                } else {
                                    zipEntryName = deployable.getPath().substring(1).concat(extension);
                                }
                                TarArchiveEntry entry = new TarArchiveEntry(zipEntryName);
                                byte[] contentBytes = content.getBytes();
                                entry.setSize(contentBytes.length);
                                tarOut.putArchiveEntry(entry);
                                tarOut.write(contentBytes);
                                tarOut.closeArchiveEntry();
                            }
                        }
                    }
                    TarArchiveEntry tagEntry = new TarArchiveEntry(TAGS_ENTRY_NAME);
                    byte[] contentBytes = Globals.prettyPrintObjectMapper.writeValueAsBytes(tags);
                    tagEntry.setSize(contentBytes.length);
                    tarOut.putArchiveEntry(tagEntry);
                    tarOut.write(contentBytes);
                    tarOut.closeArchiveEntry();
                    JocMetaInfo jocMetaInfo = createJocMetaInfo(jocVersion, apiVersion, inventoryVersion, null);
                    if (!ImportUtils.isJocMetaInfoNullOrEmpty(jocMetaInfo)) {
                        String zipEntryName = ImportUtils.JOC_META_INFO_FILENAME;
                        TarArchiveEntry entry = new TarArchiveEntry(zipEntryName);
                        byte[] jocMetaInfoBytes = Globals.prettyPrintObjectMapper.writeValueAsBytes(jocMetaInfo);
                        entry.setSize(jocMetaInfoBytes.length);
                        tarOut.putArchiveEntry(entry);
                        tarOut.write(jocMetaInfoBytes);
                        tarOut.closeArchiveEntry();
                    }
                    tarOut.flush();
                } finally {
                    if (tarOut != null) {
                        try {
                            tarOut.finish();
                            tarOut.close();
                        } catch (Exception e) {
                        }
                    }
                    if (gzipOut != null) {
                        try {
                            gzipOut.flush();
                            gzipOut.close();
                        } catch (Exception e) {
                        }
                    }
                    if (bOut != null) {
                        try {
                            bOut.flush();
                            bOut.close();
                        } catch (Exception e) {
                        }
                    }

                }

            }
        };
        return streamingOutput;
    }

    private static JocMetaInfo createJocMetaInfo(Version jocVersion, Version apiVersion, Version inventoryVersion, String commitId) {
        JocMetaInfo jocMetaInfo = new JocMetaInfo();
        if (jocVersion != null) {
            jocMetaInfo.setJocVersion(jocVersion.getVersion());
        }
        if (inventoryVersion != null) {
            jocMetaInfo.setInventorySchemaVersion(inventoryVersion.getVersion());
        }
        if (apiVersion != null) {
            jocMetaInfo.setApiVersion(apiVersion.getVersion());
        }
        if(commitId != null) {
            jocMetaInfo.setVersionId(commitId);
        }
        return jocMetaInfo;
    }
    
    public static StreamingOutput writeAgentExportZipFile(Set<Agent> agents) {
        StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException {
                ZipOutputStream zipOut = null;
                try {
                    zipOut = new ZipOutputStream(new BufferedOutputStream(output), StandardCharsets.UTF_8);
                    String content = null;
                    String zipEntryName = null;
                    for (Agent agent : agents) {
                        content = Globals.prettyPrintObjectMapper.writeValueAsString(agent);
                        if (agent.getAgentCluster() != null) {
                            zipEntryName = agent.getAgentCluster().getAgentId() + AGENT_FILE_EXTENSION;
                        } else if (agent.getStandaloneAgent() != null) {
                            zipEntryName = agent.getStandaloneAgent().getAgentId() + AGENT_FILE_EXTENSION;
                        }
                        ZipEntry entry = new ZipEntry(zipEntryName);
                        zipOut.putNextEntry(entry);
                        zipOut.write(content.getBytes());
                        zipOut.closeEntry();
                    }
                    zipOut.flush();
                } finally {
                    if (zipOut != null) {
                        try {
                            zipOut.close();
                        } catch (Exception e) {
                        }
                    }
                }
            }
        };
        return streamingOutput;
    }
        
    public static StreamingOutput writeAgentExportTarGzipFile(Set<Agent> agents) {
        StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException {
                GZIPOutputStream gzipOut = null;
                TarArchiveOutputStream tarOut = null;
                BufferedOutputStream bOut = null;
                try {
                    bOut = new BufferedOutputStream(output);
                    gzipOut = new GZIPOutputStream(bOut);
                    tarOut = new TarArchiveOutputStream(gzipOut);
                    String content = null;
                    String zipEntryName = null;
                    for (Agent agent : agents) {
                        content = Globals.prettyPrintObjectMapper.writeValueAsString(agent);
                        if (agent.getAgentCluster() != null) {
                            zipEntryName = agent.getAgentCluster().getAgentId() + AGENT_FILE_EXTENSION;
                        } else if (agent.getStandaloneAgent() != null) {
                            zipEntryName = agent.getStandaloneAgent().getAgentId() + AGENT_FILE_EXTENSION;
                        }
                        TarArchiveEntry entry = new TarArchiveEntry(zipEntryName);
                        byte[] contentBytes = content.getBytes();
                        entry.setSize(contentBytes.length);
                        tarOut.putArchiveEntry(entry);
                        tarOut.write(contentBytes);
                        tarOut.closeArchiveEntry();
                    }
                
                    tarOut.flush();
                } finally {
                    if (tarOut != null) {
                        try {
                            tarOut.finish();
                            tarOut.close();
                        } catch (Exception e) {
                        }
                    }
                    if (gzipOut != null) {
                        try {
                            gzipOut.flush();
                            gzipOut.close();
                        } catch (Exception e) {
                        }
                    }
                    if (bOut != null) {
                        try {
                            bOut.flush();
                            bOut.close();
                        } catch (Exception e) {
                        }
                    }
    
                }
    
            }
        };
        return streamingOutput;
    }
    
    private static Map<String, String> getGroupedOrderTags(Set<ConfigurationObject> configurations, SOSHibernateSession session) {
        InventoryOrderTagDBLayer dbOrderTagLayer = new InventoryOrderTagDBLayer(session);
        Set<ConfigurationType> objectsWithOrderTags = EnumSet.of(ConfigurationType.WORKFLOW, ConfigurationType.SCHEDULE,
                ConfigurationType.FILEORDERSOURCE);
        boolean hasObjectsWithOrderTags = configurations != null && configurations.stream().anyMatch(d -> objectsWithOrderTags.contains(d
                .getObjectType()));
        return hasObjectsWithOrderTags ? dbOrderTagLayer.getGroupedTags(null, true).stream().distinct().collect(
                Collectors.toMap(GroupedTag::getTag, GroupedTag::toString)) : Collections.emptyMap();
    }
    
    private static ExportedTags getTagsToExportFromConfigurationObjects(Set<ConfigurationObject> deployables, boolean withAllTags,
            SOSHibernateSession session) {
        if (withAllTags) {
            return getAllTagsToExport(session);
        } else {
            return getTagsToExportFromConfigurationObjects(deployables, session);
        }
    }

    private static ExportedTags getAllTagsToExport(SOSHibernateSession session) {
        ExportedTags tagsToExport = new ExportedTags();
        InventoryDBLayer invDbLayer = new InventoryDBLayer(session);
        InventoryTagDBLayer tagDbLayer = new InventoryTagDBLayer(session);
        InventoryJobTagDBLayer dbJobTagLayer = new InventoryJobTagDBLayer(session);
        InventoryOrderTagDBLayer dbOrderTagLayer = new InventoryOrderTagDBLayer(session);
        
        // workflow tags
        BiFunction<ExportedTagItem, ResponseBaseSearchItem, ExportedTagItem> toExportedTagItem = (eti, r) -> {
            eti.setName(new GroupedTag(r.getGroup(), r.getName()).toString());
            eti.setOrdering(r.getOrdering());
            ExportedTaggedObject eto = new ExportedTaggedObject();
            eto.setName(r.getPath());
            eto.setType(ConfigurationType.WORKFLOW.value());
            eti.getUsedBy().add(eto);
            return eti;
        };
        BinaryOperator<ExportedTagItem> combineExportedTagItem = (eti, newEti) -> {
            eti.getUsedBy().addAll(newEti.getUsedBy());
            return eti;
        };
        tagsToExport.setTags(tagDbLayer.getWorkflowTags().stream().collect(Collectors.groupingBy(ResponseBaseSearchItem::getName, Collectors
                .collectingAndThen(Collectors.toList(), b -> b.stream().reduce(new ExportedTagItem(), toExportedTagItem, combineExportedTagItem))))
                .values().stream().collect(Collectors.toList()));

        // job tags
        BiFunction<ExportedJobTagItem, InventoryJobTagItem, ExportedJobTagItem> toExportedJobTagItem = (ejti, jti) -> {
            ejti.setName(jti.getWorkflowName());
            if (ejti.getJobs() == null) {
                ejti.setJobs(new ExportedJobTagItems()); 
            }
            ejti.getJobs().addAdditionalProperties(jti.getJobName(), new GroupedTag(jti.getGroup(), jti.getTagName()).toString());
            return ejti;
        };
        BinaryOperator<ExportedJobTagItem> combineExportedJobTagItem = (ejti, newEjti) -> {
            newEjti.getJobs().getAdditionalProperties().forEach((k, v) -> ejti.getJobs().addAdditionalProperties(k, v));
            return ejti;
        };
        tagsToExport.setJobTags(dbJobTagLayer.getAllJobTags().stream().collect(Collectors.groupingBy(InventoryJobTagItem::getWorkflowName, Collectors
                .collectingAndThen(Collectors.toList(), b -> b.stream().sorted(Comparator.comparingInt(InventoryJobTagItem::getOrdering)).reduce(
                        new ExportedJobTagItem(), toExportedJobTagItem, combineExportedJobTagItem)))).values().stream().collect(Collectors.toList()));

        // order tags
        Map<String, String> groupedOrderTags = dbOrderTagLayer.getGroupedTags(null, true).stream().filter(GroupedTag::hasGroup).collect(Collectors
                .toMap(GroupedTag::getTag, GroupedTag::toString, (k1, k2) -> k1));

        List<Integer> types = Arrays.asList(ConfigurationType.FILEORDERSOURCE.intValue(), ConfigurationType.SCHEDULE.intValue());
        Map<ConfigurationType, List<DBItemInventoryConfiguration>> confWithTags = invDbLayer.getConfigurationsWithOrderTags(types).stream().collect(
                Collectors.groupingBy(DBItemInventoryConfiguration::getTypeAsEnum));
        
        Map<String, String> workflowsWithTags = invDbLayer.getWorkflowWithOrderTags();
        
        ExportedOrderTags ot = new ExportedOrderTags();
        ot.setFileOrderSources(getFileOrderSourceOrderTags(confWithTags, groupedOrderTags));
        ot.setSchedules(getSchedulesOrderTags(confWithTags, groupedOrderTags));
        ot.setWorkflows(getAddOrderOrderTags(workflowsWithTags, groupedOrderTags));
        
        tagsToExport.setOrderTags(ot);

        return tagsToExport;
    }
    
    private static List<FileOrderSourceOrderTags> getFileOrderSourceOrderTags(Map<ConfigurationType, List<DBItemInventoryConfiguration>> confWithTags,
            Map<String, String> groupedOrderTags) {
        List<FileOrderSourceOrderTags> ot = new ArrayList<>();
        if (confWithTags.containsKey(ConfigurationType.FILEORDERSOURCE)) {
            ot = confWithTags.get(ConfigurationType.FILEORDERSOURCE).stream().map(dbItem -> {
                try {
                    FileOrderSourceOrderTags fos = Globals.objectMapper.readValue(dbItem.getContent(), FileOrderSourceOrderTags.class);
                    if (fos.getTags() == null || fos.getTags().isEmpty()) {
                        return null;
                    }
                    fos.setTags(fos.getTags().stream().map(t -> groupedOrderTags.getOrDefault(t, t)).collect(Collectors.toSet()));
                    fos.setName(dbItem.getName());
                    return fos;
                } catch (Exception e) {
                    LOGGER.error("", e);
                }
                return null;
            }).filter(Objects::nonNull).toList();
        }
        if (ot.isEmpty()) {
            ot = null;
        }
        return ot;
    }
    
    private static List<ScheduleOrderTags> getSchedulesOrderTags(Map<ConfigurationType, List<DBItemInventoryConfiguration>> confWithTags,
            Map<String, String> groupedOrderTags) {
        List<ScheduleOrderTags> ot = new ArrayList<>();
        if (confWithTags.containsKey(ConfigurationType.SCHEDULE)) {
            ot = confWithTags.get(ConfigurationType.SCHEDULE).stream().map(dbItem -> {
                try {
                    ScheduleOrderTags sch = Globals.objectMapper.readValue(dbItem.getContent(), ScheduleOrderTags.class);
                    if (sch.getOrderParameterisations() == null) {
                        return null;
                    }
                    sch.setOrderParameterisations(sch.getOrderParameterisations().stream().filter(op -> op.getTags() != null && !op.getTags()
                            .isEmpty()).peek(op -> {
                                op.setForceJobAdmission(null);
                                op.setPositions(null);
                                op.setPriority(null);
                                op.setVariables(null);
                                op.setTags(op.getTags().stream().map(t -> groupedOrderTags.getOrDefault(t, t)).collect(Collectors.toSet()));
                            }).toList());
                    if (sch.getOrderParameterisations().isEmpty()) {
                        return null;
                    }
                    sch.setName(dbItem.getName());
                    return sch;
                } catch (Exception e) {
                    LOGGER.error("", e);
                }
                return null;
            }).filter(Objects::nonNull).toList();
        }
        if (ot.isEmpty()) {
            ot = null;
        }
        return ot;
    }
    
    private static List<AddOrdersOrderTags> getAddOrderOrderTags(Map<String, String> workflowsWithTags, Map<String, String> groupedOrderTags) {
        List<AddOrdersOrderTags> ot = workflowsWithTags.entrySet().stream().map(dbItem -> {
            try {
                AddOrdersOrderTags aoot = Globals.objectMapper.readValue(dbItem.getValue(), AddOrdersOrderTags.class);
                aoot.setName(dbItem.getKey());
                aoot.getAddOrderTags().getAdditionalProperties().replaceAll((k, v) -> v.stream().map(t -> groupedOrderTags.getOrDefault(t, t))
                        .collect(Collectors.toCollection(LinkedHashSet::new)));
                return aoot;
            } catch (Exception e) {
                LOGGER.error("", e);
            }
            return null;
        }).filter(Objects::nonNull).toList();
        if (ot.isEmpty()) {
            ot = null;
        }
        return ot;
    }

    private static ExportedTags getTagsToExportFromConfigurationObjects(Set<ConfigurationObject> deployables, SOSHibernateSession session) {
        ExportedTags tagsToExport = new ExportedTags();
        InventoryDBLayer invDbLayer = new InventoryDBLayer(session);
        InventoryTagDBLayer tagDbLayer = new InventoryTagDBLayer(session);
        InventoryJobTagDBLayer dbJobTagLayer = new InventoryJobTagDBLayer(session);
        InventoryOrderTagDBLayer dbOrderTagLayer = new InventoryOrderTagDBLayer(session);
        Map<String, ExportedTagItem> tags = new HashMap<>();
        Set<ExportedJobTagItem> jobTags = new HashSet<>();
        
        for (ConfigurationObject deployable : deployables) {
            
            ExportedTaggedObject reference = new ExportedTaggedObject();
            reference.setName(JocInventory.pathToName(deployable.getPath()));
            reference.setType(deployable.getObjectType().value());
            ExportedJobTagItems jti = getJobTags(deployable, dbJobTagLayer);
            if (jti != null) {
                ExportedJobTagItem job = new ExportedJobTagItem();
                job.setJobs(jti);
                job.setName(reference.getName());
                jobTags.add(job);
            }
            
            tagDbLayer.getTagsWithGroupsAndOrdering(deployable.getId()).forEach(tag -> {
                ExportedTagItem tagItem = tags.getOrDefault(tag.toString(), new ExportedTagItem());
                tagItem.setName(tag.toString());
                tagItem.setOrdering(tag.getOrdering());
                tagItem.getUsedBy().add(reference);
                tags.put(tag.toString(), tagItem);
            });
        }
        
        // order tags
        List<Integer> types = Arrays.asList(ConfigurationType.FILEORDERSOURCE.intValue(), ConfigurationType.SCHEDULE.intValue());
        Map<ConfigurationType, List<DBItemInventoryConfiguration>> confWithTags = invDbLayer.getConfigurationsWithOrderTags(types).stream().collect(
                Collectors.groupingBy(DBItemInventoryConfiguration::getTypeAsEnum));

        Map<String, String> workflowsWithTags = invDbLayer.getWorkflowWithOrderTags();

        Map<ConfigurationType, Set<String>> deployableNamesPerType = deployables.stream().collect(Collectors.groupingBy(
                ConfigurationObject::getObjectType, Collectors.mapping(ConfigurationObject::getName, Collectors.toSet())));
        deployableNamesPerType.putIfAbsent(ConfigurationType.FILEORDERSOURCE, Collections.emptySet());
        deployableNamesPerType.putIfAbsent(ConfigurationType.SCHEDULE, Collections.emptySet());
        deployableNamesPerType.putIfAbsent(ConfigurationType.WORKFLOW, Collections.emptySet());

        workflowsWithTags.keySet().retainAll(deployableNamesPerType.get(ConfigurationType.WORKFLOW));

        confWithTags.getOrDefault(ConfigurationType.FILEORDERSOURCE, Collections.emptyList()).removeIf(i -> !deployableNamesPerType.get(
                ConfigurationType.FILEORDERSOURCE).contains(i.getName()));
        confWithTags.getOrDefault(ConfigurationType.SCHEDULE, Collections.emptyList()).removeIf(i -> !deployableNamesPerType.get(
                ConfigurationType.SCHEDULE).contains(i.getName()));

        ExportedOrderTags ot = new ExportedOrderTags();
        Map<String, String> groupedOrderTags = dbOrderTagLayer.getGroupedTags(null, true).stream().filter(GroupedTag::hasGroup).collect(Collectors
                .toMap(GroupedTag::getTag, GroupedTag::toString, (k1, k2) -> k1));
        ot.setFileOrderSources(getFileOrderSourceOrderTags(confWithTags, groupedOrderTags));
        ot.setSchedules(getSchedulesOrderTags(confWithTags, groupedOrderTags));
        ot.setWorkflows(getAddOrderOrderTags(workflowsWithTags, groupedOrderTags));
        
        tagsToExport.setTags(tags.values().stream().collect(Collectors.toList()));
        tagsToExport.setJobTags(jobTags.stream().collect(Collectors.toList()));
        tagsToExport.setOrderTags(ot);
        return tagsToExport;
    }
    
    private static ExportedJobTagItems getJobTags(ConfigurationObject deployable, InventoryJobTagDBLayer dbJobTagLayer) {
        if (ConfigurationType.WORKFLOW.equals(deployable.getObjectType())) {
            com.sos.inventory.model.workflow.Workflow w = (com.sos.inventory.model.workflow.Workflow) deployable.getConfiguration();
            if (w.getJobs() != null && w.getJobs().getAdditionalProperties() != null) {
                ExportedJobTagItems jobTags = new ExportedJobTagItems();
                jobTags.setAdditionalProperties(dbJobTagLayer.getTagsWithGroups(deployable.getId(), w.getJobs().getAdditionalProperties().keySet()));
                if (jobTags.getAdditionalProperties() == null || jobTags.getAdditionalProperties().isEmpty()) {
                    return null;
                } else {
                    return jobTags;
                }
            }
        }
        return null;
    }

}
