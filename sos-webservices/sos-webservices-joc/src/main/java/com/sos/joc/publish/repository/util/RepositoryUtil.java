package com.sos.joc.publish.repository.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.commons.codec.Charsets;
import org.apache.commons.io.IOUtils;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.model.publish.repository.CopyToFilter;
import com.sos.joc.model.publish.repository.ResponseFolder;
import com.sos.joc.model.publish.repository.ResponseFolderItem;
import com.sos.joc.model.tree.Tree;
import com.sos.joc.publish.common.ConfigurationObjectFileExtension;
import com.sos.joc.publish.common.ControllerObjectFileExtension;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.util.PublishUtils;

public abstract class RepositoryUtil {

    // private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryUtil.class);
//    private static final CopyOption[] COPYOPTIONS = new StandardCopyOption[] { StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING };
    private static final OpenOption OPENOPTIONS = StandardOpenOption.TRUNCATE_EXISTING;

    public static Path getPathWithExtension(Configuration cfg) {
        switch (cfg.getObjectType()) {
        case FILEORDERSOURCE:
            return Paths.get(cfg.getPath() + getExtension(ConfigurationType.FILEORDERSOURCE));
        case INCLUDESCRIPT:
            return Paths.get(cfg.getPath() + getExtension(ConfigurationType.INCLUDESCRIPT));
        case JOBCLASS:
            return Paths.get(cfg.getPath() + getExtension(ConfigurationType.JOBCLASS));
        case JOBRESOURCE:
            return Paths.get(cfg.getPath() + getExtension(ConfigurationType.JOBRESOURCE));
        case LOCK:
            return Paths.get(cfg.getPath() + getExtension(ConfigurationType.LOCK));
        case NOTICEBOARD:
            return Paths.get(cfg.getPath() + getExtension(ConfigurationType.NOTICEBOARD));
        case SCHEDULE:
            return Paths.get(cfg.getPath() + getExtension(ConfigurationType.SCHEDULE));
        case WORKFLOW:
            return Paths.get(cfg.getPath() + getExtension(ConfigurationType.WORKFLOW));
        case NONWORKINGDAYSCALENDAR:
            return Paths.get(cfg.getPath() + getExtension(ConfigurationType.NONWORKINGDAYSCALENDAR));
        case WORKINGDAYSCALENDAR:
            return Paths.get(cfg.getPath() + getExtension(ConfigurationType.WORKINGDAYSCALENDAR));
        default:
            return Paths.get(cfg.getPath());
        }
    }

    public static String getExtension(ConfigurationType type) {
        switch (type) {
        case FILEORDERSOURCE:
            return ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION.toString();
        case INCLUDESCRIPT:
            return ConfigurationObjectFileExtension.SCRIPT_FILE_EXTENSION.toString();
        case JOBCLASS:
            return ControllerObjectFileExtension.JOBCLASS_FILE_EXTENSION.toString();
        case JOBRESOURCE:
            return ControllerObjectFileExtension.JOBRESOURCE_FILE_EXTENSION.toString();
        case LOCK:
            return ControllerObjectFileExtension.LOCK_FILE_EXTENSION.toString();
        case NOTICEBOARD:
            return ControllerObjectFileExtension.NOTICEBOARD_FILE_EXTENSION.toString();
        case SCHEDULE:
            return ConfigurationObjectFileExtension.SCHEDULE_FILE_EXTENSION.toString();
        case WORKFLOW:
            return ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.toString();
        case NONWORKINGDAYSCALENDAR:
        case WORKINGDAYSCALENDAR:
            return ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.toString();
        default:
            return "";
        }
    }

    public static String stripFileExtension(Path filename) {
        String path = "";
        if (filename.toString().endsWith(ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.toString())) {
            path = filename.toString().replace(ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.value(), "");
        } else if (filename.getFileName().toString().endsWith(ControllerObjectFileExtension.JOBRESOURCE_FILE_EXTENSION.toString())) {
            path = filename.toString().replace(ControllerObjectFileExtension.JOBRESOURCE_FILE_EXTENSION.value(), "");
        } else if (filename.getFileName().toString().endsWith(ControllerObjectFileExtension.LOCK_FILE_EXTENSION.toString())) {
            path = filename.toString().replace(ControllerObjectFileExtension.LOCK_FILE_EXTENSION.value(), "");
        } else if (filename.getFileName().toString().endsWith(ControllerObjectFileExtension.NOTICEBOARD_FILE_EXTENSION.toString())) {
            path = filename.toString().replace(ControllerObjectFileExtension.NOTICEBOARD_FILE_EXTENSION.value(), "");
        } else if (filename.getFileName().toString().endsWith(ControllerObjectFileExtension.JOBCLASS_FILE_EXTENSION.toString())) {
            path = filename.toString().replace(ControllerObjectFileExtension.JOBCLASS_FILE_EXTENSION.value(), "");
        } else if (filename.getFileName().toString().endsWith(ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION.toString())) {
            path = filename.toString().replace(ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION.value(), "");
        } else if (filename.getFileName().toString().endsWith(ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.toString())) {
            path = filename.toString().replace(ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.value(), "");
        } else if (filename.getFileName().toString().endsWith(ConfigurationObjectFileExtension.SCHEDULE_FILE_EXTENSION.toString())) {
            path = filename.toString().replace(ConfigurationObjectFileExtension.SCHEDULE_FILE_EXTENSION.value(), "");
        } else if (filename.getFileName().toString().endsWith(ConfigurationObjectFileExtension.SCRIPT_FILE_EXTENSION.toString())) {
            path = filename.toString().replace(ConfigurationObjectFileExtension.SCRIPT_FILE_EXTENSION.value(), "");
        } else {
            path = filename.toString();
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }

    public static String stripFileExtensionNormalized(Path path) {
        String normalizedPath = "";
        if (path.toString().endsWith(ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.toString())) {
            normalizedPath = Globals.normalizePath(path.toString().replace(ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.value(), ""));
        } else if (path.getFileName().toString().endsWith(ControllerObjectFileExtension.JOBRESOURCE_FILE_EXTENSION.toString())) {
            normalizedPath = Globals.normalizePath(path.toString().replace(ControllerObjectFileExtension.JOBRESOURCE_FILE_EXTENSION.value(), ""));
        } else if (path.getFileName().toString().endsWith(ControllerObjectFileExtension.LOCK_FILE_EXTENSION.toString())) {
            normalizedPath = Globals.normalizePath(path.toString().replace(ControllerObjectFileExtension.LOCK_FILE_EXTENSION.value(), ""));
        } else if (path.getFileName().toString().endsWith(ControllerObjectFileExtension.NOTICEBOARD_FILE_EXTENSION.toString())) {
            normalizedPath = Globals.normalizePath(path.toString().replace(ControllerObjectFileExtension.NOTICEBOARD_FILE_EXTENSION.value(), ""));
        } else if (path.getFileName().toString().endsWith(ControllerObjectFileExtension.JOBCLASS_FILE_EXTENSION.toString())) {
            normalizedPath = Globals.normalizePath(path.toString().replace(ControllerObjectFileExtension.JOBCLASS_FILE_EXTENSION.value(), ""));
        } else if (path.getFileName().toString().endsWith(ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION.toString())) {
            normalizedPath = Globals.normalizePath(path.toString().replace(ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION.value(), ""));
        } else if (path.getFileName().toString().endsWith(ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.toString())) {
            normalizedPath = Globals.normalizePath(path.toString().replace(ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.value(), ""));
        } else if (path.getFileName().toString().endsWith(ConfigurationObjectFileExtension.SCHEDULE_FILE_EXTENSION.toString())) {
            normalizedPath = Globals.normalizePath(path.toString().replace(ConfigurationObjectFileExtension.SCHEDULE_FILE_EXTENSION.value(), ""));
        } else if (path.getFileName().toString().endsWith(ConfigurationObjectFileExtension.SCRIPT_FILE_EXTENSION.toString())) {
            normalizedPath = Globals.normalizePath(path.toString().replace(ConfigurationObjectFileExtension.SCRIPT_FILE_EXTENSION.value(), ""));
        } else {
            normalizedPath = Globals.normalizePath(path.toString());
        }
        if (normalizedPath.startsWith("//")) {
            normalizedPath = normalizedPath.substring(1);
        }
        return normalizedPath;
    }

    public static ConfigurationType getConfigurationTypeFromFileExtension(Path path) {
        if (path.toString().endsWith(ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.toString())) {
            return ConfigurationType.WORKFLOW;
        } else if (path.toString().endsWith(ControllerObjectFileExtension.JOBRESOURCE_FILE_EXTENSION.toString())) {
            return ConfigurationType.JOBRESOURCE;
        } else if (path.toString().endsWith(ControllerObjectFileExtension.LOCK_FILE_EXTENSION.toString())) {
            return ConfigurationType.LOCK;
        } else if (path.toString().endsWith(ControllerObjectFileExtension.NOTICEBOARD_FILE_EXTENSION.toString())) {
            return ConfigurationType.NOTICEBOARD;
        } else if (path.toString().endsWith(ControllerObjectFileExtension.JOBCLASS_FILE_EXTENSION.toString())) {
            return ConfigurationType.JOBCLASS;
        } else if (path.toString().endsWith(ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION.toString())) {
            return ConfigurationType.FILEORDERSOURCE;
        } else if (path.toString().endsWith(ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.toString())) {
            File calendarFile = path.toFile();
            try {
                FileInputStream stream = new FileInputStream(calendarFile);
                String content = IOUtils.toString(stream, Charsets.UTF_8);
                if (content.contains(ConfigurationType.WORKINGDAYSCALENDAR.toString())) {
                    return ConfigurationType.WORKINGDAYSCALENDAR;
                } else {
                    return ConfigurationType.NONWORKINGDAYSCALENDAR;
                }
            } catch (Exception e) {
                throw new JocException(e);
            }
        } else if (path.toString().endsWith(ConfigurationObjectFileExtension.SCHEDULE_FILE_EXTENSION.toString())) {
            return ConfigurationType.SCHEDULE;
        } else if (path.toString().endsWith(ConfigurationObjectFileExtension.SCRIPT_FILE_EXTENSION.toString())) {
            return ConfigurationType.INCLUDESCRIPT;
        } else {
            return ConfigurationType.FOLDER;
        }
    }

    public static SortedSet<Tree> initTreeByFolder(Path path, Boolean recursive) throws Exception {
        Comparator<Tree> comparator = Comparator.comparing(Tree::getPath).reversed();
        SortedSet<Tree> folders = new TreeSet<>(comparator);
        Set<Tree> results = getTree(path);
        final int parentDepth = path.getNameCount();
        if (results != null && !results.isEmpty()) {
            if (recursive != null && recursive) {
                folders.addAll(results);
            } else {
                folders.addAll(results.stream()
                        .filter(item -> Paths.get(item.getPath()).getNameCount() <= parentDepth + 1).collect(Collectors.toSet()));
            }
        }
        return folders;
    }

    public static ResponseFolder getTree(SortedSet<ResponseFolder> folders, Path startFolder, Path repositoryBase, Boolean recursive) {
        Map<Path, ResponseFolder> treeMap = new HashMap<>();
        for (ResponseFolder folder : folders) {
            Path pFolder = Paths.get(folder.getPath());
            ResponseFolder tree = null;
            if (treeMap.containsKey(pFolder)) {
                tree = treeMap.get(pFolder);
                tree.setItems(folder.getItems());
                if (recursive != null && recursive) {
                    tree.getFolders().removeIf(child -> (child.getFolders() == null || child.getFolders().isEmpty()) 
                            && (child.getItems() == null || child.getItems().isEmpty()));
                }
            } else {
                tree = folder;
                tree.setFolders(Collections.emptyList());
                tree.setName(pFolder.getFileName() == null ? "" : pFolder.getFileName().toString());
                tree.setLastModified(getLastModified(pFolder));
                treeMap.put(pFolder, tree);
            }
            fillTreeMap(treeMap, pFolder, tree);
        }
        if (treeMap.isEmpty()) {
            return new ResponseFolder();
        }
        Path subPath = subPath(repositoryBase, startFolder);
        if (!subPath.startsWith(Paths.get("/"))) {
            subPath = Paths.get("/").resolve(subPath);
        }
        return treeMap.get(subPath);
    }

    public static ResponseFolderItem getResponseFolderItem(Path rootPath, Path path) {
        ResponseFolderItem item = new ResponseFolderItem();
        item.setFolder(Globals.normalizePath(subPath(rootPath, path.getParent()).toString()));
        item.setObjectName(stripFileExtension(path.getFileName()));
        item.setObjectType(RepositoryUtil.getConfigurationTypeFromFileExtension(path));
        Date childLastModified;
        try {
            childLastModified = Date.from(Files.getLastModifiedTime(path).toInstant());
            item.setLastModified(childLastModified);
        } catch (IOException e) {
            item.setLastModified(Date.from(Instant.now()));
        }
        return item;
    }

    public static Path subPath(Path initialFolder, Path subFolder) {
        if (initialFolder.equals(subFolder)) {
            return Paths.get("/");
        } else {
            return subFolder.subpath(initialFolder.getNameCount(), subFolder.getNameCount());
        }
    }

    public static TreeSet<Path> readRepositoryAsTreeSet(Path repository) throws IOException {
        TreeSet<Path> paths = new TreeSet<>();
        Files.walkFileTree(repository, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                paths.add(dir);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
                if (filePath.getFileName().toString().endsWith(".json")) {
                    paths.add(filePath);
                }
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
        return paths;
    }

    public static Date getLastModified(Path path) {
        try {
            return Date.from(Files.getLastModifiedTime(path).toInstant());
        } catch (IOException e) {
            return Date.from(Instant.now());
        }
    }
    
    public static Set<ConfigurationObject> getDeployableEnvIndependentConfigurationsFromDB(CopyToFilter filter, DBLayerDeploy dbLayer, String commitId)
            throws DBConnectionRefusedException, DBInvalidDataException, JocMissingRequiredParameterException, DBMissingDataException, IOException,
            SOSHibernateException {
        Set<Integer> types = new HashSet<Integer>();
        types.add(ConfigurationType.WORKFLOW.intValue());
        types.add(ConfigurationType.JOBCLASS.intValue());
        types.add(ConfigurationType.LOCK.intValue());
        types.add(ConfigurationType.FILEORDERSOURCE.intValue());
        types.add(ConfigurationType.NOTICEBOARD.intValue());
        return getDeployableConfigurationsFromDB(filter, dbLayer, commitId, types);
    }

    public static Set<ConfigurationObject> getDeployableEnvRelatedConfigurationsFromDB(CopyToFilter filter, DBLayerDeploy dbLayer, String commitId)
            throws DBConnectionRefusedException, DBInvalidDataException, JocMissingRequiredParameterException, DBMissingDataException, IOException,
            SOSHibernateException {
        Set<Integer> types = new HashSet<Integer>();
        types.add(ConfigurationType.JOBRESOURCE.intValue());
        return getDeployableConfigurationsFromDB(filter, dbLayer, commitId, types);
    }

    public static Set<ConfigurationObject> getReleasableConfigurationsFromDB(CopyToFilter filter, DBLayerDeploy dbLayer) throws DBConnectionRefusedException,
            DBInvalidDataException, JocMissingRequiredParameterException, DBMissingDataException, IOException, SOSHibernateException {
        Map<String, ConfigurationObject> allObjectsMap = new HashMap<String, ConfigurationObject>();
        // TODO: add missing JobResources
        if (filter != null) {
            if (filter.getEnvRelated().getReleasedConfigurations() != null && !filter.getEnvRelated().getReleasedConfigurations().isEmpty()) {
                List<Configuration> releasedFolders = filter.getEnvRelated().getReleasedConfigurations().stream()
                        .filter(item -> item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                        .map(item -> item.getConfiguration()).collect(Collectors.toList());
                Set<DBItemInventoryReleasedConfiguration> allItems = new HashSet<DBItemInventoryReleasedConfiguration>();
                if (releasedFolders != null && !releasedFolders.isEmpty()) {
                    allItems.addAll(PublishUtils.getReleasedInventoryConfigurationsfromFoldersWithoutDrafts(releasedFolders, dbLayer));
                }
                List<DBItemInventoryReleasedConfiguration> configurationDbItems = dbLayer.getFilteredReleasedConfigurations(filter);
                if (configurationDbItems != null && !configurationDbItems.isEmpty()) {
                    allItems.addAll(configurationDbItems);
                }
                if (!allItems.isEmpty()) {
                    allItems.stream().filter(Objects::nonNull).filter(item -> !item.getTypeAsEnum().equals(ConfigurationType.FOLDER))
                        .forEach(item -> allObjectsMap.put(item.getName(), PublishUtils.getConfigurationObjectFromDBItem(item)));
                }
            }
            if (filter.getEnvRelated().getDraftConfigurations() != null && !filter.getEnvRelated().getDraftConfigurations().isEmpty()) {
                List<Configuration> draftFolders = filter.getEnvRelated().getDraftConfigurations().stream()
                        .filter(item -> item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                        .map(item -> item.getConfiguration()).collect(Collectors.toList());
                Set<DBItemInventoryConfiguration> allItems = new HashSet<DBItemInventoryConfiguration>();
                if (draftFolders != null && !draftFolders.isEmpty()) {
                    allItems.addAll(PublishUtils.getReleasableInventoryConfigurationsWithoutReleasedfromFolders(draftFolders, dbLayer));
                }
                List<DBItemInventoryConfiguration> configurationDbItems = dbLayer.getFilteredReleasableConfigurations(filter);
                if (configurationDbItems != null && !configurationDbItems.isEmpty()) {
                    allItems.addAll(configurationDbItems);
                }
                if (!allItems.isEmpty()) {
                    allItems.stream().filter(Objects::nonNull).filter(item -> !item.getTypeAsEnum().equals(ConfigurationType.FOLDER))
                        .forEach(item -> {
                            if (!allObjectsMap.containsKey(item.getName())) {
                                allObjectsMap.put(item.getName(), PublishUtils.getConfigurationObjectFromDBItem(item));
                            }
                        });
                }
            }
        }
        Set<ConfigurationObject> withoutDuplicates = new HashSet<ConfigurationObject>(allObjectsMap.values());
        return withoutDuplicates;
    }

    public static void writeToRepository(Set<ConfigurationObject> deployables, Set<ConfigurationObject> releasables, DBLayerDeploy dbLayer)
            throws IOException {
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
                default:
                    break;
                }
                if (extension != null) {
                    content = Globals.prettyPrintObjectMapper.writeValueAsString(deployable.getConfiguration());
                    String filename = deployable.getPath().substring(1).concat(extension);
                    byte[] contentBytes = content.getBytes();
                    Files.write(Paths.get(filename), contentBytes, OPENOPTIONS);
                }
            }
        }
        if (releasables != null && !releasables.isEmpty()) {
            for (ConfigurationObject releasable : releasables) {
                // process releasable objects
                String extension = null;
                switch (releasable.getObjectType()) {
                case SCHEDULE:
                    extension = ConfigurationObjectFileExtension.SCHEDULE_FILE_EXTENSION.toString();
                    break;
                case INCLUDESCRIPT:
                    extension = ConfigurationObjectFileExtension.SCRIPT_FILE_EXTENSION.toString();
                    break;
                case WORKINGDAYSCALENDAR:
                case NONWORKINGDAYSCALENDAR:
                    extension = ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.toString();
                    break;
                default:
                    break;
                }
                if (extension != null) {
                    content = Globals.prettyPrintObjectMapper.writeValueAsString(releasable.getConfiguration());
                    String filename = releasable.getPath().substring(1).concat(extension);
                    byte[] contentBytes = content.getBytes();
                    Files.write(Paths.get(filename), contentBytes, OPENOPTIONS);
                }
            }
        }
    }

    private static Set<ConfigurationObject> getDeployableConfigurationsFromDB(CopyToFilter filter, DBLayerDeploy dbLayer, String commitId, 
            Set<Integer> types) throws DBConnectionRefusedException, DBInvalidDataException, JocMissingRequiredParameterException, 
            DBMissingDataException, IOException, SOSHibernateException {
        Set<ConfigurationObject> configurations = new HashSet<ConfigurationObject>();
        if (filter != null) {
            if (filter.getEnvIndependent() != null && !filter.getEnvIndependent().getDeployConfigurations().isEmpty()) {
                Set<Configuration> depFolders = filter.getEnvIndependent().getDeployConfigurations().stream()
                        .filter(item -> item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                        .map(item -> item.getConfiguration()).collect(Collectors.toSet());
                if (filter.getEnvRelated() != null && !filter.getEnvRelated().getDeployConfigurations().isEmpty()) {
                    depFolders.addAll(filter.getEnvRelated().getDeployConfigurations().stream()
                            .filter(item -> item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                            .map(item -> item.getConfiguration()).collect(Collectors.toSet()));
                }
                Set<DBItemDeploymentHistory> allItems = new HashSet<DBItemDeploymentHistory>();
                if (depFolders != null && !depFolders.isEmpty()) {
                    allItems.addAll(
                            getLatestActiveDepHistoryEntriesFromFoldersByType(depFolders, types, dbLayer).stream()
                                .filter(Objects::nonNull).collect(Collectors.toSet())
                            );
                }
                List<DBItemDeploymentHistory> deploymentDbItems = dbLayer.getFilteredDeployments(filter);
                if (deploymentDbItems != null && !deploymentDbItems.isEmpty()) {
                    allItems.addAll(deploymentDbItems);
                }
                if (!allItems.isEmpty()) {
                    allItems.stream().filter(Objects::nonNull).filter(item -> !item.getType().equals(ConfigurationType.FOLDER.intValue()))
                        .forEach(item -> {
                            if (commitId != null) {
                                dbLayer.storeCommitIdForLaterUsage(item, commitId);
                            }
                            configurations.add(PublishUtils.getConfigurationObjectFromDBItem(item, commitId));
                        });
                }
            }
            if (filter.getEnvIndependent() != null && !filter.getEnvIndependent().getDraftConfigurations().isEmpty()) {
                List<Configuration> draftFolders = filter.getEnvIndependent().getDraftConfigurations().stream()
                        .filter(item -> item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                        .map(item -> item.getConfiguration()).collect(Collectors.toList());
                if (filter.getEnvRelated() != null && !filter.getEnvRelated().getDraftConfigurations().isEmpty()) {
                    draftFolders.addAll(filter.getEnvRelated().getDraftConfigurations().stream()
                            .filter(item -> item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                            .map(item -> item.getConfiguration()).collect(Collectors.toList()));
                }
                Set<DBItemInventoryConfiguration> allItems = new HashSet<DBItemInventoryConfiguration>();
                if (draftFolders != null && !draftFolders.isEmpty()) {
                    allItems.addAll(PublishUtils.getDeployableInventoryConfigurationsfromFolders(draftFolders, dbLayer));
                }
                List<DBItemInventoryConfiguration> configurationDbItems = dbLayer.getFilteredDeployableConfigurations(filter);
                if (configurationDbItems != null && !configurationDbItems.isEmpty()) {
                    allItems.addAll(configurationDbItems);
                }
                if (!allItems.isEmpty()) {
                    allItems.stream().filter(Objects::nonNull).filter(item -> !item.getTypeAsEnum().equals(ConfigurationType.FOLDER))
                        .forEach(item -> {
                            boolean alreadyExists = false;
                            for (ConfigurationObject config : configurations) {
                                if (item.getName().equals(config.getName()) && item.getTypeAsEnum().equals(config.getObjectType())) {
                                    alreadyExists = true;
                                    break;
                                }
                            }
                            if (!alreadyExists) {
                                configurations.add(PublishUtils.getConfigurationObjectFromDBItem(item));
                            }
                        });
                }
            }
        }
        return configurations;
    }

    private static Set<DBItemDeploymentHistory> getLatestActiveDepHistoryEntriesFromFoldersByType(Set<Configuration> folders, Set<Integer> types,
            DBLayerDeploy dbLayer) {
        ConcurrentMap<String, Optional<DBItemDeploymentHistory>> groupedEntries = 
                folders.parallelStream().map(item -> dbLayer.getDepHistoryItemsFromFolderByType(item.getPath(), types, item.getRecursive()))
                    .flatMap(List::stream).collect(
                            Collectors.groupingByConcurrent(DBItemDeploymentHistory::getPath, 
                            Collectors.maxBy(Comparator.comparing(DBItemDeploymentHistory::getId))));
        return groupedEntries.values().stream().filter(Optional::isPresent).map(Optional::get)
                .filter(item -> OperationType.DELETE.value() != item.getOperation()).collect(Collectors.toSet());
    }

    private static Set<Path> readFolders(Path repository) throws IOException {
        Set<Path> paths = new HashSet<Path>();
        Files.walkFileTree(repository, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                paths.add(dir);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
        return paths;
    }

    private static void fillTreeMap(Map<Path, ResponseFolder> treeMap, Path folder, ResponseFolder tree) {
        Path parent = folder.getParent();
        if (parent != null) {
            ResponseFolder parentTree = null;
            if (treeMap.containsKey(parent)) {
                parentTree = treeMap.get(parent);
                List<ResponseFolder> treeList = parentTree.getFolders();
                if (treeList == null) {
                    treeList = new ArrayList<>();
                    treeList.add(tree);
                    parentTree.setFolders(treeList);
                } else {
                    if (treeList.contains(tree)) {
                        treeList.remove(tree);
                    }
                    treeList.add(0, tree);
                }
            } else {
                parentTree = new ResponseFolder();
                parentTree.setPath(parent.toString().replace('\\', '/'));
                List<ResponseFolder> treeList = new ArrayList<>();
                treeList.add(tree);
                parentTree.setFolders(treeList);
                treeMap.put(parent, parentTree);
            }
            fillTreeMap(treeMap, parent, parentTree);
        }
    }

    private static Set<Tree> getTree(Path folder) throws IOException {
        return readFolders(folder).stream().map(path -> {
            Tree tree = new Tree();
            tree.setPath(path.toString());
            tree.setDeleted(false);
            return tree;
        }).collect(Collectors.toSet());
    }

}
