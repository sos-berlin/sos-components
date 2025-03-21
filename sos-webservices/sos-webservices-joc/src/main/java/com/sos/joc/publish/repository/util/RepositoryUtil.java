package com.sos.joc.publish.repository.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
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

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.Config;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.model.publish.repository.Category;
import com.sos.joc.model.publish.repository.CopyToFilter;
import com.sos.joc.model.publish.repository.DeleteFromFilter;
import com.sos.joc.model.publish.repository.ResponseFolder;
import com.sos.joc.model.publish.repository.ResponseFolderItem;
import com.sos.joc.model.publish.repository.UpdateFromFilter;
import com.sos.joc.model.tree.Tree;
import com.sos.joc.publish.common.ConfigurationObjectFileExtension;
import com.sos.joc.publish.common.ControllerObjectFileExtension;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.util.PublishUtils;

public abstract class RepositoryUtil {

     private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryUtil.class);
//    private static final CopyOption[] COPYOPTIONS = new StandardCopyOption[] { StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING };
//    private static final OpenOption[] OPENOPTIONS = new StandardOpenOption[] { StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE};

    public static String getExtension(ConfigurationType type) {
        switch (type) {
        case FILEORDERSOURCE:
            return ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION.toString();
        case INCLUDESCRIPT:
            return ConfigurationObjectFileExtension.SCRIPT_FILE_EXTENSION.toString();
        case JOBTEMPLATE:
            return ConfigurationObjectFileExtension.JOBTEMPLATE_FILE_EXTENSION.toString();
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
        case REPORT:
            return ConfigurationObjectFileExtension.REPORT_FILE_EXTENSION.toString();
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
        } else if (filename.getFileName().toString().endsWith(ConfigurationObjectFileExtension.JOBTEMPLATE_FILE_EXTENSION.toString())) {
            path = filename.toString().replace(ConfigurationObjectFileExtension.JOBTEMPLATE_FILE_EXTENSION.value(), "");
        } else {
            path = filename.toString();
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }

    public static Path stripFileExtensionFromPath(Path path) {
        Path newPath = null;
        if (path.toString().endsWith(ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.toString())) {
            newPath = Paths.get(path.toString().replace(ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.value(), ""));
        } else if (path.getFileName().toString().endsWith(ControllerObjectFileExtension.JOBRESOURCE_FILE_EXTENSION.toString())) {
            newPath = Paths.get(path.toString().replace(ControllerObjectFileExtension.JOBRESOURCE_FILE_EXTENSION.value(), ""));
        } else if (path.getFileName().toString().endsWith(ControllerObjectFileExtension.LOCK_FILE_EXTENSION.toString())) {
            newPath = Paths.get(path.toString().replace(ControllerObjectFileExtension.LOCK_FILE_EXTENSION.value(), ""));
        } else if (path.getFileName().toString().endsWith(ControllerObjectFileExtension.NOTICEBOARD_FILE_EXTENSION.toString())) {
            newPath = Paths.get(path.toString().replace(ControllerObjectFileExtension.NOTICEBOARD_FILE_EXTENSION.value(), ""));
        } else if (path.getFileName().toString().endsWith(ControllerObjectFileExtension.JOBCLASS_FILE_EXTENSION.toString())) {
            newPath = Paths.get(path.toString().replace(ControllerObjectFileExtension.JOBCLASS_FILE_EXTENSION.value(), ""));
        } else if (path.getFileName().toString().endsWith(ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION.toString())) {
            newPath = Paths.get(path.toString().replace(ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION.value(), ""));
        } else if (path.getFileName().toString().endsWith(ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.toString())) {
            newPath = Paths.get(path.toString().replace(ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.value(), ""));
        } else if (path.getFileName().toString().endsWith(ConfigurationObjectFileExtension.SCHEDULE_FILE_EXTENSION.toString())) {
            newPath = Paths.get(path.toString().replace(ConfigurationObjectFileExtension.SCHEDULE_FILE_EXTENSION.value(), ""));
        } else if (path.getFileName().toString().endsWith(ConfigurationObjectFileExtension.SCRIPT_FILE_EXTENSION.toString())) {
            newPath = Paths.get(path.toString().replace(ConfigurationObjectFileExtension.SCRIPT_FILE_EXTENSION.value(), ""));
        } else if (path.getFileName().toString().endsWith(ConfigurationObjectFileExtension.JOBTEMPLATE_FILE_EXTENSION.toString())) {
            newPath = Paths.get(path.toString().replace(ConfigurationObjectFileExtension.JOBTEMPLATE_FILE_EXTENSION.value(), ""));
        } else {
            newPath = path;
        }
        return newPath;
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
        } else if (path.getFileName().toString().endsWith(ConfigurationObjectFileExtension.JOBTEMPLATE_FILE_EXTENSION.toString())) {
            normalizedPath = Globals.normalizePath(path.toString().replace(ConfigurationObjectFileExtension.JOBTEMPLATE_FILE_EXTENSION.value(), ""));
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
                String content = IOUtils.toString(stream, StandardCharsets.UTF_8);
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
        } else if (path.toString().endsWith(ConfigurationObjectFileExtension.JOBTEMPLATE_FILE_EXTENSION.toString())) {
            return ConfigurationType.JOBTEMPLATE;
        } else if (path.toString().endsWith(ConfigurationObjectFileExtension.REPORT_FILE_EXTENSION.toString())) {
            return ConfigurationType.REPORT;
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
                if(!".git".equals(dir.getFileName().toString()) && !dir.toString().contains(".git")) {
                    paths.add(dir);
                }
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

    public static TreeSet<Path> readRepositoryAsTreeSet(Path repository, boolean recursive) throws IOException {
        TreeSet<Path> paths = new TreeSet<>();
        if (recursive) {
            Files.walkFileTree(repository, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if(!".git".equals(dir.getFileName().toString()) && !dir.toString().contains(".git")) {
                        paths.add(dir);
                    }
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
        } else {
            Files.walkFileTree(repository, EnumSet.noneOf(FileVisitOption.class), 2, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if(!".git".equals(dir.getFileName().toString()) && !dir.toString().contains(".git")) {
                        paths.add(dir);
                    }
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
        }
        return paths;
    }

    public static TreeSet<Path> readItemsFromRepositoryFolderAsTreeSet(Path repository) throws IOException {
        TreeSet<Path> paths = new TreeSet<>();
        Files.walkFileTree(repository, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
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
    
    public static Set<ConfigurationObject> getDeployableRolloutConfigurationsFromDB(CopyToFilter filter, DBLayerDeploy dbLayer,
            String commitId) throws DBConnectionRefusedException, DBInvalidDataException, JocMissingRequiredParameterException, 
        DBMissingDataException, IOException, SOSHibernateException {
        List<ConfigurationType> rolloutTypes = getRolloutConfigurationTypes();
        return getDeployableConfigurationsFromDB(filter, dbLayer, commitId, rolloutTypes.stream()
                .map(type -> type.intValue()).collect(Collectors.toSet()));
    }

    public static Set<ConfigurationObject> getDeployableLocalConfigurationsFromDB(CopyToFilter filter, DBLayerDeploy dbLayer, 
            String commitId) throws DBConnectionRefusedException, DBInvalidDataException, JocMissingRequiredParameterException, 
        DBMissingDataException, IOException, SOSHibernateException {
        List<ConfigurationType> localTypes = getLocalConfigurationTypes();
        return getDeployableConfigurationsFromDB(filter, dbLayer, commitId, localTypes.stream()
                .map(type -> type.intValue()).collect(Collectors.toSet()));
    }
    
    public static Set<ConfigurationObject> getReleasableRolloutConfigurationsFromDB(CopyToFilter filter, DBLayerDeploy dbLayer) 
            throws DBConnectionRefusedException, DBInvalidDataException, JocMissingRequiredParameterException, DBMissingDataException, 
            IOException, SOSHibernateException {
        List<ConfigurationType> rolloutTypes = getRolloutConfigurationTypes();
        return getReleasableConfigurationsFromDB(filter, dbLayer, rolloutTypes.stream()
                .map(type -> type.intValue()).collect(Collectors.toSet()));
    }

    public static Set<ConfigurationObject> getReleasableLocalConfigurationsFromDB(CopyToFilter filter, DBLayerDeploy dbLayer) 
            throws DBConnectionRefusedException, DBInvalidDataException, JocMissingRequiredParameterException, DBMissingDataException, 
            IOException, SOSHibernateException {
        List<ConfigurationType> localTypes = getLocalConfigurationTypes();
        return getReleasableConfigurationsFromDB(filter, dbLayer, localTypes.stream()
                .map(type -> type.intValue()).collect(Collectors.toSet()));
    }

    public static Set<Path> getPathsToDeleteFromFS(DeleteFromFilter filter, Path repositoryBase) {
        Set<Path> entries = new TreeSet<Path>();
        if (filter != null && !filter.getConfigurations().isEmpty()) {
            filter.getConfigurations().stream()
                .filter(cfg -> ConfigurationType.FOLDER.equals(cfg.getConfiguration().getObjectType()))
                .map(config -> config.getConfiguration())
                .forEach(folder -> {
                    try {
                        entries.addAll(RepositoryUtil.readRepositoryAsTreeSet(
                                repositoryBase.resolve(Paths.get("/").relativize(Paths.get(folder.getPath())))));
                    } catch (IOException e) {
                        LOGGER.error("Could not read from repository: " + 
                                repositoryBase.resolve(Paths.get("/").relativize(Paths.get(folder.getPath()))).toString());
                    }
                });
            filter.getConfigurations().stream()
                .filter(cfg -> !ConfigurationType.FOLDER.equals(cfg.getConfiguration().getObjectType()))
                .map(config -> config.getConfiguration())
                .forEach(cfg -> entries.add(repositoryBase.resolve(
                        Paths.get("/").relativize(Paths.get(cfg.getPath() + RepositoryUtil.getExtension(cfg.getObjectType())))))
                );
            
        }
        return entries;
    }
    
    public static void writeToRepository(Set<ConfigurationObject> deployables, Set<ConfigurationObject> releasables, Path repositoryBase,
            StoreItemsCategory category) throws IOException {
        String content = null;
        List<ConfigurationType> localTypes = getLocalConfigurationTypes();
        List<ConfigurationType> rolloutTypes = getRolloutConfigurationTypes();
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
                case JOBTEMPLATE:
                    extension = ConfigurationObjectFileExtension.JOBTEMPLATE_FILE_EXTENSION.toString();
                    break;
                case WORKINGDAYSCALENDAR:
                case NONWORKINGDAYSCALENDAR:
                    extension = ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.toString();
                    break;
                case REPORT:
                    extension = ConfigurationObjectFileExtension.REPORT_FILE_EXTENSION.toString();
                    break;
                default:
                    break;
                }
                Path repository = repositoryBase.resolve("rollout");
                if (rolloutTypes.contains(deployable.getObjectType())) {
                    repository = repositoryBase.resolve("rollout");
                } else if (localTypes.contains(deployable.getObjectType())) {
                    repository = repositoryBase.resolve("local");
                }
                if (extension != null) {
                    content = Globals.prettyPrintObjectMapper.writeValueAsString(deployable.getConfiguration());
                    String filename = deployable.getPath().substring(1).concat(extension);
                    byte[] contentBytes = content.getBytes();
                    switch (category) {
                    case BOTH:
                        Files.createDirectories(repository.resolve(Paths.get(filename)).getParent());
                        Files.write(repository.resolve(Paths.get(filename)), contentBytes);
                        break;
                    case LOCAL:
                        if (localTypes.contains(deployable.getObjectType())) {
                            Files.createDirectories(repository.resolve(Paths.get(filename)).getParent());
                            Files.write(repository.resolve(Paths.get(filename)), contentBytes);
                        }
                        break;
                    case ROLLOUT: 
                        if (rolloutTypes.contains(deployable.getObjectType())) {
                            Files.createDirectories(repository.resolve(Paths.get(filename)).getParent());
                            Files.write(repository.resolve(Paths.get(filename)), contentBytes);
                        }
                        break;
                    }
                }
            }
        }
        if (releasables != null && !releasables.isEmpty()) {
            for (ConfigurationObject releasable : releasables) {
                // process releasable objects
                String extension = null;
                switch (releasable.getObjectType()) {
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
                case JOBTEMPLATE:
                    extension = ConfigurationObjectFileExtension.JOBTEMPLATE_FILE_EXTENSION.toString();
                    break;
                case WORKINGDAYSCALENDAR:
                case NONWORKINGDAYSCALENDAR:
                    extension = ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.toString();
                    break;
                case REPORT:
                    extension = ConfigurationObjectFileExtension.REPORT_FILE_EXTENSION.toString();
                    break;
                default:
                    break;
                }
                Path repository = repositoryBase.resolve("local");
                if (rolloutTypes.contains(releasable.getObjectType())) {
                    repository = repositoryBase.resolve("rollout");
                } else if (localTypes.contains(releasable.getObjectType())) {
                    repository = repositoryBase.resolve("local");
                }
                if (extension != null) {
                    content = Globals.prettyPrintObjectMapper.writeValueAsString(releasable.getConfiguration());
                    String filename = releasable.getPath().substring(1).concat(extension);
                    byte[] contentBytes = content.getBytes();
                    switch (category) {
                    case BOTH:
                        Files.createDirectories(repository.resolve(Paths.get(filename)).getParent());
                        Files.write(repository.resolve(Paths.get(filename)), contentBytes);
                        break;
                    case LOCAL:
                        if (localTypes.contains(releasable.getObjectType())) {
                            Files.createDirectories(repository.resolve(Paths.get(filename)).getParent());
                            Files.write(repository.resolve(Paths.get(filename)), contentBytes);
                        }
                        break;
                    case ROLLOUT: 
                        if (rolloutTypes.contains(releasable.getObjectType())) {
                            Files.createDirectories(repository.resolve(Paths.get(filename)).getParent());
                            Files.write(repository.resolve(Paths.get(filename)), contentBytes);
                        }
                        break;
                    }
                }
            }
        }
    }

    public static void deleteFromRepository(Set<ConfigurationObject> toDelete, Path repositories)
            throws IOException {
        Set<String> foldersToDelete = new HashSet<String>();
        if (toDelete != null && !toDelete.isEmpty()) {
            for (ConfigurationObject cfg : toDelete) {
                String extension = null;
                switch (cfg.getObjectType()) {
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
                case JOBTEMPLATE:
                    extension = ConfigurationObjectFileExtension.JOBTEMPLATE_FILE_EXTENSION.toString();
                    break;
                case WORKINGDAYSCALENDAR:
                case NONWORKINGDAYSCALENDAR:
                    extension = ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.toString();
                    break;
                case FOLDER:
                    break;
                case REPORT:
                    extension = ConfigurationObjectFileExtension.REPORT_FILE_EXTENSION.toString();
                    break;
                default:
                    break;
                }
                if (extension != null) {
                    String filename = cfg.getPath().substring(1).concat(extension);
                    Files.deleteIfExists(repositories.resolve(Paths.get(filename)));
                } else if (ConfigurationType.FOLDER.equals(cfg.getObjectType())){
                    String filename = cfg.getPath().substring(1);
                    foldersToDelete.add(filename);
                }
            }
            if (!foldersToDelete.isEmpty()) {
                foldersToDelete.stream().forEach(folder -> {
                    try {
                        Files.deleteIfExists(repositories.resolve(Paths.get(folder)));
                    } catch (IOException e) {
                        LOGGER.debug("Could not delete folder : " + folder);
                    }
                });
                
            }
        }
    }

    public static Set<DBItemInventoryConfiguration> getUpdatedDbItems(UpdateFromFilter filter, Path repositoryBase, DBLayerDeploy dbLayer) {
        List <ConfigurationType> localTypes = getLocalConfigurationTypes();
        List <ConfigurationType> rolloutTypes = getRolloutConfigurationTypes();
        
        Set<DBItemInventoryConfiguration> dbItemsToUpdate = new HashSet<DBItemInventoryConfiguration>();
        Set<Config> folders = filter.getConfigurations().stream()
                .filter(item -> ConfigurationType.FOLDER.equals(item.getConfiguration().getObjectType())).collect(Collectors.toSet());
        Set<Config> items = filter.getConfigurations().stream()
                .filter(item -> !ConfigurationType.FOLDER.equals(item.getConfiguration().getObjectType())).collect(Collectors.toSet());
        
        dbItemsToUpdate.addAll(folders.stream().map(folder -> dbLayer.getAllInventoryConfigurationsByFolder(
                    folder.getConfiguration().getPath(), folder.getConfiguration().getRecursive()))
                .flatMap(Collection::stream).filter(item -> {
                    if(filter.getCategory().equals(Category.LOCAL)) {
                        if(localTypes.contains(item.getTypeAsEnum())) {
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        if(rolloutTypes.contains(item.getTypeAsEnum())) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                }).collect(Collectors.toSet()));
        dbItemsToUpdate.addAll(items.stream().map(item -> dbLayer.getInventoryConfigurationByNameAndType(
                Paths.get(stripFileExtension(Paths.get(item.getConfiguration().getPath()))).getFileName().toString(),
                item.getConfiguration().getObjectType().intValue())).filter(Objects::nonNull).filter(item -> {
                    if(filter.getCategory().equals(Category.LOCAL)) {
                        if(localTypes.contains(item.getTypeAsEnum())) {
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        if(rolloutTypes.contains(item.getTypeAsEnum())) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                }).collect(Collectors.toSet()));

        if (!dbItemsToUpdate.isEmpty()) {
            Map<ConfigurationType, Set<String>> notUpdated = new HashMap<ConfigurationType, Set<String>>();
            InventoryDBLayer invDbLayer = new InventoryDBLayer(dbLayer.getSession());
            dbItemsToUpdate = dbItemsToUpdate.stream()
                .filter(item -> (Category.LOCAL.equals(filter.getCategory()) && localTypes.contains(item.getTypeAsEnum())) ||
                    (Category.ROLLOUT.equals(filter.getCategory()) && rolloutTypes.contains(item.getTypeAsEnum())))
                .map(dbItem -> {
                    boolean updated = false;
                    ConfigurationType objType = dbItem.getTypeAsEnum();
                    byte[] content = null;
                    try {
                        Path repository = repositoryBase;
                        if (Category.LOCAL.equals(filter.getCategory())) {
                            repository = repositoryBase.resolve("local");
                        } else if (Category.ROLLOUT.equals(filter.getCategory())) {
                            repository = repositoryBase.resolve("rollout");
                        }
                        if (dbItem.getPath().startsWith("/")) {
                            content = Files.readAllBytes(repository.resolve(Paths.get(dbItem.getPath().substring(1) + getExtension(objType))));
                        } else {
                            content = Files.readAllBytes(repository.resolve(Paths.get(dbItem.getPath() + getExtension(objType))));
    
                        }
                        String updatedContent = null;
                        if (!ConfigurationType.FOLDER.equals(objType)) {
                            updatedContent = getConfiguration(content, objType);
                        }
                        if (updatedContent != null) {
                            if (!JocInventory.isJsonHashEqual(dbItem.getContent(), updatedContent, dbItem.getTypeAsEnum())) {
                                updated = true;
                            } else {
                                if(!notUpdated.containsKey(dbItem.getTypeAsEnum())) {
                                    notUpdated.put(dbItem.getTypeAsEnum(), new HashSet<String>());
                                }
                                notUpdated.get(dbItem.getTypeAsEnum()).add(dbItem.getName());
                            }
                            boolean valid = true;
                            try {
                                Validator.validate(objType, content, invDbLayer, null);
                            } catch (Exception e) {
                                valid = false;
                            }
                            dbItem.setContent(updatedContent);
                            dbItem.setDeployed(false);
                            dbItem.setReleased(false);
                            dbItem.setValid(valid);
                            dbItem.setModified(Date.from(Instant.now()));
                        }
                    } catch (IOException e) {
                        LOGGER.error("", e);
                    }
                    if(updated) {
                        return dbItem;
                    } else {
                        return null;
                    }
            }).filter(Objects::nonNull).collect(Collectors.toSet()); 
            if(!notUpdated.isEmpty() && LOGGER.isDebugEnabled()) {
                LOGGER.debug(notUpdated.entrySet().stream().map(entry -> {
                    return entry.getValue().size()  + " " + entry.getKey().value().toLowerCase();
                }).collect(Collectors.joining(", ", "[HASH COMPARE]:", " not updated, because json content is equal.")));
            }
        }
        return dbItemsToUpdate;
    }
    
    public static Set<DBItemInventoryConfiguration> getNewItemsToUpdate(UpdateFromFilter filter, Path repositoryBase, DBLayerDeploy dbLayer) {
        List <ConfigurationType> localTypes = getLocalConfigurationTypes();
        List <ConfigurationType> rolloutTypes = getRolloutConfigurationTypes();
        Set<Config> newItems = new HashSet<Config>();
        Set<DBItemInventoryConfiguration> dbItems = new HashSet<DBItemInventoryConfiguration>();
        Path repository = repositoryBase.resolve("rollout");
        if (Category.ROLLOUT.equals(filter.getCategory())) {
            repository = repositoryBase.resolve("rollout");
        } else if (Category.LOCAL.equals(filter.getCategory())) {
            repository = repositoryBase.resolve("local");
        }
        for (Config cfg : filter.getConfigurations()) {
            if (!ConfigurationType.FOLDER.equals(cfg.getConfiguration().getObjectType())) {
                DBItemInventoryConfiguration cfgDbItem = dbLayer.getConfigurationByName(
                        Paths.get(cfg.getConfiguration().getPath()).getFileName().toString(), 
                        cfg.getConfiguration().getObjectType());
                if (cfgDbItem == null) {
                    if((Category.LOCAL.equals(filter.getCategory()) && localTypes.contains(cfg.getConfiguration().getObjectType())) ||
                            (Category.ROLLOUT.equals(filter.getCategory()) && rolloutTypes.contains(cfg.getConfiguration().getObjectType()))) {
                        newItems.add(cfg);
                    }
                }
            } else {
                try {
                    TreeSet<Path> paths = readRepositoryAsTreeSet(repository.resolve(cfg.getConfiguration().getPath().substring(1)), cfg.getConfiguration().getRecursive());
                    for (Path path : paths) {
                        if (!ConfigurationType.FOLDER.equals(getConfigurationTypeFromFileExtension(path))) {
                            String newPath = path.toString().replace('\\', '/').replace(repository.toString().replace('\\', '/'), "");
                            DBItemInventoryConfiguration cfgDbItem = dbLayer.getConfigurationByName(
                                    stripFileExtension(path.getFileName()), 
                                    getConfigurationTypeFromFileExtension(path));
                            if (cfgDbItem == null) {
                                Config newCfg = new Config();
                                Configuration newConf = new Configuration();
                                newConf.setPath(stripFileExtension(Paths.get(newPath)).replace('\\', '/'));
                                newConf.setObjectType(getConfigurationTypeFromFileExtension(path));
                                newCfg.setConfiguration(newConf);
                                newItems.add(newCfg);
                            }
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error("could not read from path " + 
                            repository.resolve(cfg.getConfiguration().getPath().substring(1)).toString().replace('\\', '/'), e.getCause());
                }
            }
        }
        final Path repo = repository;
        InventoryDBLayer invDbLayer = new InventoryDBLayer(dbLayer.getSession()); 
        newItems.stream().forEach(newItem -> {
            ConfigurationType objType = newItem.getConfiguration().getObjectType();
            byte[] content = null;
            try {
                if (newItem.getConfiguration().getPath().startsWith("/")) {
                    content = Files.readAllBytes(repo.resolve(Paths.get(newItem.getConfiguration().getPath().substring(1) + getExtension(objType))));
                } else {
                    content = Files.readAllBytes(repo.resolve(Paths.get(newItem.getConfiguration().getPath() + getExtension(objType))));
                }
                String updatedContent = null;
                if (!ConfigurationType.FOLDER.equals(objType)) {
                    updatedContent = getConfiguration(content, objType);
                }
                if (updatedContent != null) {
                    boolean valid = true;
                    try {
                        Validator.validate(objType, content, invDbLayer, null);
                    } catch (Exception e) {
                        valid = false;
                    }
                    dbItems.add(createItem(newItem, updatedContent, valid));
                }
            } catch (IOException e) {
                LOGGER.error("", e);
            }
        });
        return dbItems;
    }

    public static Path getPathWithExtension(Configuration cfg) {
        switch (cfg.getObjectType()) {
        case FILEORDERSOURCE:
            return Paths.get(cfg.getPath() + getExtension(ConfigurationType.FILEORDERSOURCE));
        case INCLUDESCRIPT:
            return Paths.get(cfg.getPath() + getExtension(ConfigurationType.INCLUDESCRIPT));
        case JOBTEMPLATE:
            return Paths.get(cfg.getPath() + getExtension(ConfigurationType.JOBTEMPLATE));
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
        case REPORT:
            return Paths.get(cfg.getPath() + getExtension(ConfigurationType.REPORT));
        default:
            return Paths.get(cfg.getPath());
        }
    }

    private static String getConfiguration(byte[] content, ConfigurationType type) throws StreamReadException, DatabindException,
            JsonProcessingException, IOException {
        if (content == null || !JocInventory.CLASS_MAPPING.containsKey(type)) {
            return null;
        }
        return Globals.objectMapper.writeValueAsString(Globals.prettyPrintObjectMapper.readValue(content, JocInventory.CLASS_MAPPING.get(type)));
    }
    
    private static Set<ConfigurationObject> getDeployableConfigurationsFromDB(CopyToFilter filter, DBLayerDeploy dbLayer, String commitId, 
            Set<Integer> types) throws DBConnectionRefusedException, DBInvalidDataException, JocMissingRequiredParameterException, 
            DBMissingDataException, IOException, SOSHibernateException {
        Set<ConfigurationObject> configurations = new HashSet<ConfigurationObject>();
        if (filter != null) {
            if (filter.getRollout() != null) {
                if (!filter.getRollout().getDeployConfigurations().isEmpty()) {
                    Set<Configuration> depFolders = filter.getRollout().getDeployConfigurations().stream()
                            .filter(item -> item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                            .map(item -> item.getConfiguration()).collect(Collectors.toSet());
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
                                configurations.add(PublishUtils.getConfigurationObjectFromDBItem(item, commitId));
                            });
                    }
                }
                if (!filter.getRollout().getDraftConfigurations().isEmpty()) {
                    List<Configuration> draftFolders = filter.getRollout().getDraftConfigurations().stream()
                            .filter(item -> item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                            .map(item -> item.getConfiguration()).collect(Collectors.toList());
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
            if (filter.getLocal() != null) {
                if (!filter.getLocal().getDeployConfigurations().isEmpty()) {
                    Set<Configuration> depFolders = filter.getLocal().getDeployConfigurations().stream()
                            .filter(item -> item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                            .map(item -> item.getConfiguration()).collect(Collectors.toSet());
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
                                configurations.add(PublishUtils.getConfigurationObjectFromDBItem(item, commitId));
                            });
                    }
                }
                if (!filter.getLocal().getDraftConfigurations().isEmpty()) {
                    List<Configuration> draftFolders = filter.getLocal().getDraftConfigurations().stream()
                            .filter(item -> item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                            .map(item -> item.getConfiguration()).collect(Collectors.toList());
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
        }
        return configurations;
    }

    private static Set<ConfigurationObject> getReleasableConfigurationsFromDB(CopyToFilter filter, DBLayerDeploy dbLayer, Set<Integer> types) 
            throws DBConnectionRefusedException, DBInvalidDataException, JocMissingRequiredParameterException, DBMissingDataException, 
            IOException, SOSHibernateException {
        Map<String, ConfigurationObject> allObjectsMap = new HashMap<String, ConfigurationObject>();
        if (filter != null) {
            if(filter.getLocal() != null) {
                if (filter.getLocal().getReleasedConfigurations() != null && !filter.getLocal().getReleasedConfigurations().isEmpty()) {
                    List<Configuration> releasedFolders = filter.getLocal().getReleasedConfigurations().stream()
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
                if (filter.getLocal().getDraftConfigurations() != null && !filter.getLocal().getDraftConfigurations().isEmpty()) {
                    List<Configuration> draftFolders = filter.getLocal().getDraftConfigurations().stream()
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
            if (filter.getRollout() != null) {
                if (filter.getRollout().getReleasedConfigurations() != null && !filter.getRollout().getReleasedConfigurations().isEmpty()) {
                    List<Configuration> releasedFolders = filter.getRollout().getReleasedConfigurations().stream()
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
                if (filter.getRollout().getDraftConfigurations() != null && !filter.getRollout().getDraftConfigurations().isEmpty()) {
                    List<Configuration> draftFolders = filter.getRollout().getDraftConfigurations().stream()
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
        }
        if (filter != null && filter.getLocal() != null) {
        }
        Set<ConfigurationObject> withoutDuplicates = new HashSet<ConfigurationObject>(allObjectsMap.values());
        return withoutDuplicates;
    }

    private static Set<DBItemDeploymentHistory> getLatestActiveDepHistoryEntriesFromFoldersByType(Set<Configuration> folders,
            Set<Integer> types, DBLayerDeploy dbLayer) {
        ConcurrentMap<String, Optional<DBItemDeploymentHistory>> groupedEntries = 
                folders.parallelStream().map(item -> dbLayer.getDepHistoryItemsFromFolderByType(item.getPath(), types, item.getRecursive()))
                    .flatMap(List::stream).collect(
                            Collectors.groupingByConcurrent(item -> item.getType() + ":" + item.getName(), 
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

    private static List<ConfigurationType> getLocalConfigurationTypes() {
        List<ConfigurationType> types = new ArrayList<ConfigurationType>();
        if (Globals.getConfigurationGlobalsGit().getHoldWorkflows().getValue() != null 
                && !Globals.getConfigurationGlobalsGit().getHoldWorkflows().getValue().isEmpty()) {
            if (Category.LOCAL.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldWorkflows().getValue())) {
                types.add(ConfigurationType.WORKFLOW);
            }
        } else {
            if (Category.LOCAL.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldWorkflows().getDefault())) {
                types.add(ConfigurationType.WORKFLOW);
            }
        }
        if (Globals.getConfigurationGlobalsGit().getHoldLocks().getValue() != null 
                && !Globals.getConfigurationGlobalsGit().getHoldLocks().getValue().isEmpty()) {
            if (Category.LOCAL.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldLocks().getValue())) {
                types.add(ConfigurationType.LOCK);
            }
        } else {
            if (Category.LOCAL.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldLocks().getDefault())) {
                types.add(ConfigurationType.LOCK);
            }
        }
        if (Globals.getConfigurationGlobalsGit().getHoldNoticeBoards().getValue() != null 
                && !Globals.getConfigurationGlobalsGit().getHoldNoticeBoards().getValue().isEmpty()) {
            if (Category.LOCAL.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldNoticeBoards().getValue())) {
                types.add(ConfigurationType.NOTICEBOARD);
            }
        } else {
            if (Category.LOCAL.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldNoticeBoards().getDefault())) {
                types.add(ConfigurationType.NOTICEBOARD);
            }
        }
        if (Globals.getConfigurationGlobalsGit().getHoldFileOrderSources().getValue() != null 
                && !Globals.getConfigurationGlobalsGit().getHoldFileOrderSources().getValue().isEmpty()) {
            if (Category.LOCAL.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldFileOrderSources().getValue())) {
                types.add(ConfigurationType.FILEORDERSOURCE);
            }
        } else {
            if (Category.LOCAL.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldFileOrderSources().getDefault())) {
                types.add(ConfigurationType.FILEORDERSOURCE);
            }
        }
        if (Globals.getConfigurationGlobalsGit().getHoldScriptIncludes().getValue() != null 
                && !Globals.getConfigurationGlobalsGit().getHoldScriptIncludes().getValue().isEmpty()) {
            if (Category.LOCAL.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldScriptIncludes().getValue())) {
                types.add(ConfigurationType.INCLUDESCRIPT);
            }
        } else {
            if (Category.LOCAL.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldScriptIncludes().getDefault())) {
                types.add(ConfigurationType.INCLUDESCRIPT);
            }
        }
        if (Globals.getConfigurationGlobalsGit().getHoldJobs().getValue() != null 
                && !Globals.getConfigurationGlobalsGit().getHoldJobs().getValue().isEmpty()) {
            if (Category.LOCAL.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldJobs().getValue())) {
                types.add(ConfigurationType.JOBTEMPLATE);
            }
        } else {
            if (Category.LOCAL.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldJobs().getDefault())) {
                types.add(ConfigurationType.JOBTEMPLATE);
            }
        }
        if (Globals.getConfigurationGlobalsGit().getHoldJobResources().getValue() != null 
                && !Globals.getConfigurationGlobalsGit().getHoldJobResources().getValue().isEmpty()) {
            if (Category.LOCAL.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldJobResources().getValue())) {
                types.add(ConfigurationType.JOBRESOURCE);
            }
        } else {
            if (Category.LOCAL.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldJobResources().getDefault())) {
                types.add(ConfigurationType.JOBRESOURCE);
            }
        }
        if (Globals.getConfigurationGlobalsGit().getHoldSchedules().getValue() != null 
                && !Globals.getConfigurationGlobalsGit().getHoldSchedules().getValue().isEmpty()) {
            if (Category.LOCAL.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldSchedules().getValue())) {
                types.add(ConfigurationType.SCHEDULE);
            }
        } else {
            if (Category.LOCAL.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldSchedules().getDefault())) {
                types.add(ConfigurationType.SCHEDULE);
            }
        }
        if (Globals.getConfigurationGlobalsGit().getHoldCalendars().getValue() != null 
                && !Globals.getConfigurationGlobalsGit().getHoldCalendars().getValue().isEmpty()) {
            if (Category.LOCAL.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldCalendars().getValue())) {
                types.add(ConfigurationType.WORKINGDAYSCALENDAR);
                types.add(ConfigurationType.NONWORKINGDAYSCALENDAR);
            }
        } else {
            if (Category.LOCAL.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldCalendars().getDefault())) {
                types.add(ConfigurationType.WORKINGDAYSCALENDAR);
                types.add(ConfigurationType.NONWORKINGDAYSCALENDAR);
            }
        }
        // TODO REPORT
        return types;
    }

    private static List<ConfigurationType> getRolloutConfigurationTypes() {
        List<ConfigurationType> types = new ArrayList<ConfigurationType>();
        if (Globals.getConfigurationGlobalsGit().getHoldWorkflows().getValue() != null 
                && !Globals.getConfigurationGlobalsGit().getHoldWorkflows().getValue().isEmpty()) {
            if (Category.ROLLOUT.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldWorkflows().getValue())) {
                types.add(ConfigurationType.WORKFLOW);
            }
        } else {
            if (Category.ROLLOUT.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldWorkflows().getDefault())) {
                types.add(ConfigurationType.WORKFLOW);
            }
        }
        if (Globals.getConfigurationGlobalsGit().getHoldLocks().getValue() != null 
                && !Globals.getConfigurationGlobalsGit().getHoldLocks().getValue().isEmpty()) {
            if (Category.ROLLOUT.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldLocks().getValue())) {
                types.add(ConfigurationType.LOCK);
            }
        } else {
            if (Category.ROLLOUT.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldLocks().getDefault())) {
                types.add(ConfigurationType.LOCK);
            }
        }
        if (Globals.getConfigurationGlobalsGit().getHoldNoticeBoards().getValue() != null 
                && !Globals.getConfigurationGlobalsGit().getHoldNoticeBoards().getValue().isEmpty()) {
            if (Category.ROLLOUT.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldNoticeBoards().getValue())) {
                types.add(ConfigurationType.NOTICEBOARD);
            }
        } else {
            if (Category.ROLLOUT.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldNoticeBoards().getDefault())) {
                types.add(ConfigurationType.NOTICEBOARD);
            }
        }
        if (Globals.getConfigurationGlobalsGit().getHoldFileOrderSources().getValue() != null 
                && !Globals.getConfigurationGlobalsGit().getHoldFileOrderSources().getValue().isEmpty()) {
            if (Category.ROLLOUT.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldFileOrderSources().getValue())) {
                types.add(ConfigurationType.FILEORDERSOURCE);
            }
        } else {
            if (Category.ROLLOUT.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldFileOrderSources().getDefault())) {
                types.add(ConfigurationType.FILEORDERSOURCE);
            }
        }
        if (Globals.getConfigurationGlobalsGit().getHoldScriptIncludes().getValue() != null 
                && !Globals.getConfigurationGlobalsGit().getHoldScriptIncludes().getValue().isEmpty()) {
            if (Category.ROLLOUT.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldScriptIncludes().getValue())) {
                types.add(ConfigurationType.INCLUDESCRIPT);
            }
        } else {
            if (Category.ROLLOUT.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldScriptIncludes().getDefault())) {
                types.add(ConfigurationType.INCLUDESCRIPT);
            }
        }
        if (Globals.getConfigurationGlobalsGit().getHoldJobs().getValue() != null 
                && !Globals.getConfigurationGlobalsGit().getHoldJobs().getValue().isEmpty()) {
            if (Category.ROLLOUT.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldJobs().getValue())) {
                types.add(ConfigurationType.JOBTEMPLATE);
            }
        } else {
            if (Category.ROLLOUT.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldJobs().getDefault())) {
                types.add(ConfigurationType.JOBTEMPLATE);
            }
        }
        if (Globals.getConfigurationGlobalsGit().getHoldJobResources().getValue() != null 
                && !Globals.getConfigurationGlobalsGit().getHoldJobResources().getValue().isEmpty()) {
            if (Category.ROLLOUT.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldJobResources().getValue())) {
                types.add(ConfigurationType.JOBRESOURCE);
            }
        } else {
            if (Category.ROLLOUT.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldJobResources().getDefault())) {
                types.add(ConfigurationType.JOBRESOURCE);
            }
        }
        if (Globals.getConfigurationGlobalsGit().getHoldSchedules().getValue() != null 
                && !Globals.getConfigurationGlobalsGit().getHoldSchedules().getValue().isEmpty()) {
            if (Category.ROLLOUT.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldSchedules().getValue())) {
                types.add(ConfigurationType.SCHEDULE);
            }
        } else {
            if (Category.ROLLOUT.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldSchedules().getDefault())) {
                types.add(ConfigurationType.SCHEDULE);
            }
        }
        if (Globals.getConfigurationGlobalsGit().getHoldCalendars().getValue() != null 
                && !Globals.getConfigurationGlobalsGit().getHoldCalendars().getValue().isEmpty()) {
            if (Category.ROLLOUT.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldCalendars().getValue())) {
                types.add(ConfigurationType.WORKINGDAYSCALENDAR);
                types.add(ConfigurationType.NONWORKINGDAYSCALENDAR);
            }
        } else {
            if (Category.ROLLOUT.value().toLowerCase().equals(Globals.getConfigurationGlobalsGit().getHoldCalendars().getDefault())) {
                types.add(ConfigurationType.WORKINGDAYSCALENDAR);
                types.add(ConfigurationType.NONWORKINGDAYSCALENDAR);
            }
        }
        return types;
    }


    private static DBItemInventoryConfiguration createItem(Config newItem, String content, boolean valid) {
        Path newItemPath = Paths.get(newItem.getConfiguration().getPath());
        DBItemInventoryConfiguration item = new DBItemInventoryConfiguration();
        item.setId(null);
        item.setPath(newItem.getConfiguration().getPath());
        if(!item.getPath().startsWith("/")) {
            item.setPath("/" + item.getPath());
        }
        item.setFolder(newItemPath.getParent().toString().replace('\\', '/'));
        if(!item.getFolder().startsWith("/")) {
            item.setFolder("/" + item.getFolder());
        }
        item.setName(newItemPath.getFileName().toString());
        item.setDeployed(false);
        item.setReleased(false);
        item.setModified(Date.from(Instant.now()));
        item.setCreated(item.getModified());
        item.setDeleted(false);
        item.setAuditLogId(0L);
        item.setType(newItem.getConfiguration().getObjectType());
        item.setValid(valid);
        item.setContent(content);
        return item;
    }

}
