package com.sos.joc.publish.util;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.JsonConverter;
import com.sos.joc.classes.inventory.JsonSerializer;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
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
import com.sos.joc.model.joc.JocMetaInfo;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.ControllerObject;
import com.sos.joc.model.publish.DeployablesValidFilter;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.model.publish.folder.ExportFolderFilter;
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

public class ExportUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportUtils.class);
    private static final String AGENT_FILE_EXTENSION = ".agent.json";
    
    public static Set<ControllerObject> getFolderControllerObjectsForSigning(ExportFolderFilter filter, String account, DBLayerDeploy dbLayer,
            String commitId) throws SOSHibernateException {
        Map<String, ControllerObject> allObjects = new HashMap<String, ControllerObject>();
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
                    .forEach(item -> allObjects.put(item.getName(), getContollerObjectFromDBItem(item, commitId, account, releasedScripts)));
            }
            if(!filter.getForSigning().getWithoutDrafts()) {
                allDraftItems.addAll(getDeployableInventoryConfigurationsfromFolders(folderPaths, recursive, dbLayer));
                allDraftItems.stream().filter(Objects::nonNull).filter(dbItem -> filterTypes.contains(dbItem.getTypeAsEnum())).forEach(
                        item -> {
                            if(!allObjects.containsKey(item.getName())) {
                                allObjects.put(item.getName(), mapInvConfigToJSObject(item, account, commitId, releasedScripts));
                            }
                        });
            }
        }
        return new HashSet<ControllerObject>(allObjects.values());
    }
    
    public static Set<ConfigurationObject> getFolderConfigurationObjectsForShallowCopy(ExportFolderFilter filter, String account,
            DBLayerDeploy dbLayer) throws SOSHibernateException {
        Map<String, ConfigurationObject> allObjectsMap = new HashMap<String, ConfigurationObject>();
        if (filter != null && filter.getShallowCopy() != null) {
            Set<DBItemInventoryReleasedConfiguration> allReleasedItems = new HashSet<DBItemInventoryReleasedConfiguration>();
            Set<DBItemDeploymentHistory> allDeployedItems = new HashSet<DBItemDeploymentHistory>();
            Set<DBItemInventoryConfiguration> allDraftItems = new HashSet<DBItemInventoryConfiguration>();
            final List<ConfigurationType> filterTypes = filter.getShallowCopy().getObjectTypes();
            final List<String> folderPaths = filter.getShallowCopy().getFolders();
            final boolean recursive = filter.getShallowCopy().getRecursive();
            if (!filter.getShallowCopy().getWithoutDeployed()) {
                allDeployedItems.addAll(getLatestActiveDepHistoryEntriesWithoutDraftsFromFolders(folderPaths, recursive, null, dbLayer));
                allDeployedItems.stream().filter(Objects::nonNull).filter(item -> filterTypes.contains(ConfigurationType.fromValue(item.getType())))
                .forEach(item -> allObjectsMap.put(item.getName(), getConfigurationObjectFromDBItem(item)));
            }
            if(!filter.getShallowCopy().getWithoutReleased()) {
                allReleasedItems.addAll(getReleasedInventoryConfigurationsfromFoldersWithoutDrafts(folderPaths, recursive, dbLayer));
                allReleasedItems.stream().filter(Objects::nonNull).filter(item -> filterTypes.contains(ConfigurationType.fromValue(item.getType())))
                    .forEach(item -> allObjectsMap.put(item.getName(), PublishUtils.getConfigurationObjectFromDBItem(item)));
            }
            if (!filter.getShallowCopy().getWithoutDrafts()) {
                allDraftItems.addAll(getDeployableInventoryConfigurationsfromFolders(folderPaths, recursive, dbLayer));
                if (filter.getShallowCopy().getOnlyValidObjects()) {
                    allDraftItems = allDraftItems.stream().filter(item -> item.getValid()).filter(Objects::nonNull).collect(Collectors.toSet());
                }
                allDraftItems.stream().filter(Objects::nonNull).filter(dbItem -> filterTypes.contains(dbItem.getTypeAsEnum())).forEach(
                        item -> {
                            if (!allObjectsMap.containsKey(item.getName())) {
                                allObjectsMap.put(item.getName(), PublishUtils.getConfigurationObjectFromDBItem(item));
                            }
                        });
            }
        }
        return new HashSet<ConfigurationObject>(allObjectsMap.values());
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
            jsObject.setContent(JsonConverter.readAsConvertedDeployObject(item.getPath(), item.getContent(), StoreDeployments.CLASS_MAPPING.get(
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
                jsObject.setContent(JsonConverter.readAsConvertedDeployObject(item.getPath(), item.getInvContent(),
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
                    .filter(cfg -> cfg.getName().equals(item.getName()) && cfg.getType().equals(item.getType())).findFirst().orElse(null);
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
            configurationObject.setPath(item.getPath());
            configurationObject.setName(item.getName());
            configurationObject.setObjectType(ConfigurationType.fromValue(item.getType()));
            configurationObject.setConfiguration(JocInventory.content2IJSObject(item.getInvContent(), configurationObject.getObjectType().intValue()));
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
                                    workflow.setPath(deployable.getPath());
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
                    JocMetaInfo jocMetaInfo = createJocMetaInfo(jocVersion, apiVersion, inventoryVersion);
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

    public static StreamingOutput writeZipFileShallow(Set<ConfigurationObject> deployables, DBLayerDeploy dbLayer, Version jocVersion,
            Version apiVersion, Version inventoryVersion) {
        StreamingOutput streamingOutput = new StreamingOutput() {

            @Override
            public void write(OutputStream output) throws IOException {
                ZipOutputStream zipOut = null;
                try {
                    zipOut = new ZipOutputStream(new BufferedOutputStream(output), StandardCharsets.UTF_8);
                    String content = null;
                    if (deployables != null && !deployables.isEmpty()) {
                        for (ConfigurationObject deployable : deployables) {
                            String extension = null;
                            switch (deployable.getObjectType()) {
                            case WORKFLOW:
                                extension = ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.toString();
                                break;
                            case JOBRESOURCE:
                                extension = ControllerObjectFileExtension.JOBRESOURCE_FILE_EXTENSION.toString();
                                break;
                            case LOCK:
                                extension = ControllerObjectFileExtension.LOCK_FILE_EXTENSION.toString();
                                break;
                            case NOTICEBOARD:
                                extension = ControllerObjectFileExtension.NOTICEBOARD_FILE_EXTENSION.toString();
                                break;
                            case JOBCLASS:
                                extension = ControllerObjectFileExtension.JOBCLASS_FILE_EXTENSION.toString();
                                break;
                            case FILEORDERSOURCE:
                                extension = ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION.toString();
                                break;
                            case SCHEDULE:
                                extension = ConfigurationObjectFileExtension.SCHEDULE_FILE_EXTENSION.toString();
                                break;
                            case INCLUDESCRIPT:
                                extension = ConfigurationObjectFileExtension.SCRIPT_FILE_EXTENSION.toString();
                                break;
                            case JOB:
                                extension = ConfigurationObjectFileExtension.JOB_FILE_EXTENSION.toString();
                                break;
                            case WORKINGDAYSCALENDAR:
                            case NONWORKINGDAYSCALENDAR:
                                extension = ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.toString();
                                break;
                            default:
                                break;
                            }
                            if (extension != null) {
                                content = Globals.prettyPrintObjectMapper.writeValueAsString(deployable.getConfiguration());
                                String zipEntryName = deployable.getPath().substring(1).concat(extension);
                                ZipEntry entry = new ZipEntry(zipEntryName);
                                zipOut.putNextEntry(entry);
                                zipOut.write(content.getBytes());
                                zipOut.closeEntry();
                            }
                        }
                    }
                    JocMetaInfo jocMetaInfo = createJocMetaInfo(jocVersion, apiVersion, inventoryVersion);
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
                    byte[] contentBytes = null;
                    if (deployables != null && !deployables.isEmpty()) {
                        for (ControllerObject deployable : deployables) {
                            String extension = null;
                            switch (deployable.getObjectType()) {
                            case WORKFLOW:
                                extension = ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.toString();
                                Workflow workflow = (Workflow) deployable.getContent();
                                workflow.setVersionId(commitId);
                                if (controllerId != null && updateableAgentNames != null) {
                                    PublishUtils.replaceAgentNameWithAgentId(workflow, updateableAgentNames, controllerId);
                                }
                                workflow.setPath(Paths.get(deployable.getPath()).getFileName().toString());
                                // workflow.setPath(deployable.getPath());
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
                    JocMetaInfo jocMetaInfo = createJocMetaInfo(jocVersion, apiVersion, inventoryVersion);
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

    public static StreamingOutput writeTarGzipFileShallow(Set<ConfigurationObject> configurations, DBLayerDeploy dbLayer, Version jocVersion,
            Version apiVersion, Version inventoryVersion) {
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
                    if (configurations != null && !configurations.isEmpty()) {
                        for (ConfigurationObject deployable : configurations) {
                            String extension = null;
                            switch (deployable.getObjectType()) {
                            case WORKFLOW:
                                extension = ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.toString();
                                break;
                            case JOBRESOURCE:
                                extension = ControllerObjectFileExtension.JOBRESOURCE_FILE_EXTENSION.toString();
                                break;
                            case LOCK:
                                extension = ControllerObjectFileExtension.LOCK_FILE_EXTENSION.toString();
                                break;
                            case NOTICEBOARD:
                                extension = ControllerObjectFileExtension.NOTICEBOARD_FILE_EXTENSION.toString();
                                break;
                            case JOBCLASS:
                                extension = ControllerObjectFileExtension.JOBCLASS_FILE_EXTENSION.toString();
                                break;
                            case FILEORDERSOURCE:
                                extension = ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION.toString();
                                break;
                            case SCHEDULE:
                                extension = ConfigurationObjectFileExtension.SCHEDULE_FILE_EXTENSION.toString();
                                break;
                            case INCLUDESCRIPT:
                                extension = ConfigurationObjectFileExtension.SCRIPT_FILE_EXTENSION.toString();
                                break;
                            case JOB:
                                extension = ConfigurationObjectFileExtension.JOB_FILE_EXTENSION.toString();
                                break;
                            case WORKINGDAYSCALENDAR:
                            case NONWORKINGDAYSCALENDAR:
                                extension = ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.toString();
                                break;
                            default:
                                break;
                            }
                            if (extension != null) {
                                content = Globals.prettyPrintObjectMapper.writeValueAsString(deployable.getConfiguration());
                                String zipEntryName = deployable.getPath().substring(1).concat(extension);
                                TarArchiveEntry entry = new TarArchiveEntry(zipEntryName);
                                byte[] contentBytes = content.getBytes();
                                entry.setSize(contentBytes.length);
                                tarOut.putArchiveEntry(entry);
                                tarOut.write(contentBytes);
                                tarOut.closeArchiveEntry();
                            }
                        }
                    }
                    JocMetaInfo jocMetaInfo = createJocMetaInfo(jocVersion, apiVersion, inventoryVersion);
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

    private static JocMetaInfo createJocMetaInfo(Version jocVersion, Version apiVersion, Version inventoryVersion) {
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
                        if (agent.getAgentCluster() != null) {
                            zipEntryName = agent.getAgentCluster().getAgentId() + AGENT_FILE_EXTENSION;
                            content = Globals.prettyPrintObjectMapper.writeValueAsString(agent.getAgentCluster());
                        } else if (agent.getStandaloneAgent() != null) {
                            zipEntryName = agent.getStandaloneAgent().getAgentId() + AGENT_FILE_EXTENSION;
                            content = Globals.prettyPrintObjectMapper.writeValueAsString(agent.getStandaloneAgent());
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
                        if (agent.getAgentCluster() != null) {
                            zipEntryName = agent.getAgentCluster().getAgentId() + AGENT_FILE_EXTENSION;
                            content = Globals.prettyPrintObjectMapper.writeValueAsString(agent.getAgentCluster());
                        } else if (agent.getStandaloneAgent() != null) {
                            zipEntryName = agent.getStandaloneAgent().getAgentId() + AGENT_FILE_EXTENSION;
                            content = Globals.prettyPrintObjectMapper.writeValueAsString(agent.getStandaloneAgent());
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
    
}
