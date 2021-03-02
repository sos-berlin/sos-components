package com.sos.joc.classes.inventory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.rest.SOSShiroFolderPermissions;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.Schedule;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.instruction.InstructionType;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.jobclass.JobClass;
import com.sos.inventory.model.junction.Junction;
import com.sos.inventory.model.lock.Lock;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.CheckJavaVariableName;
import com.sos.joc.classes.inventory.search.WorkflowConverter;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryConfigurationTrash;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.search.DBItemSearchWorkflow;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.inventory.InventoryEvent;
import com.sos.joc.event.bean.inventory.InventoryTrashEvent;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.RequestFilter;
import com.sos.joc.model.inventory.folder.Folder;

public class JocInventory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocInventory.class);
    public static final String APPLICATION_PATH = "inventory";
    public static final String ROOT_FOLDER = "/";
    public static final String DEFAULT_COPY_SUFFIX = "copy";
    public static final String DEFAULT_RESTORE_SUFFIX = "restored";

    public static final Map<ConfigurationType, String> SCHEMA_LOCATION = Collections.unmodifiableMap(new HashMap<ConfigurationType, String>() {

        private static final long serialVersionUID = 1L;

        {
            put(ConfigurationType.WORKINGDAYSCALENDAR, "classpath:/raml/inventory/schemas/calendar/calendar-schema.json");
            put(ConfigurationType.NONWORKINGDAYSCALENDAR, "classpath:/raml/inventory/schemas/calendar/calendar-schema.json");
            put(ConfigurationType.JOB, "classpath:/raml/inventory/schemas/job/job-schema.json");
            put(ConfigurationType.JOBCLASS, "classpath:/raml/inventory/schemas/jobClass/jobClass-schema.json");
            put(ConfigurationType.JUNCTION, "classpath:/raml/inventory/schemas/junction/junction-schema.json");
            put(ConfigurationType.LOCK, "classpath:/raml/inventory/schemas/lock/lock-schema.json");
            put(ConfigurationType.SCHEDULE, "classpath:/raml/inventory/schemas/schedule/schedule-schema.json");
            put(ConfigurationType.WORKFLOW, "classpath:/raml/inventory/schemas/workflow/workflow-schema.json");
            put(ConfigurationType.FOLDER, "classpath:/raml/api/schemas/inventory/folder-schema.json");
        }
    });

    public static final Map<InstructionType, String> INSTRUCTION_SCHEMA_LOCATION = Collections.unmodifiableMap(
            new HashMap<InstructionType, String>() {

                private static final long serialVersionUID = 1L;

                {
                    // TODO put(InstructionType.AWAIT, "classpath:/raml/inventory/schemas/instruction/await-schema.json");
                    put(InstructionType.EXECUTE_NAMED, "classpath:/raml/inventory/schemas/instruction/namedJob-schema.json");
                    put(InstructionType.FAIL, "classpath:/raml/inventory/schemas/instruction/fail-schema.json");
                    put(InstructionType.FINISH, "classpath:/raml/inventory/schemas/instruction/finish-schema.json");
                    put(InstructionType.FORK, "classpath:/raml/inventory/schemas/instruction/forkJoin-schema.json");
                    put(InstructionType.IF, "classpath:/raml/inventory/schemas/instruction/ifelse-schema.json");
                    put(InstructionType.LOCK, "classpath:/raml/inventory/schemas/instruction/lock-schema.json");
                    // TODO put(InstructionType.PUBLISH, "classpath:/raml/inventory/schemas/instruction/publish-schema.json");
                    put(InstructionType.RETRY, "classpath:/raml/inventory/schemas/instruction/retryInCatch-schema.json");
                    put(InstructionType.TRY, "classpath:/raml/inventory/schemas/instruction/retry-schema.json");
                }
            });

    public static final Map<ConfigurationType, Class<?>> CLASS_MAPPING = Collections.unmodifiableMap(new HashMap<ConfigurationType, Class<?>>() {

        private static final long serialVersionUID = 1L;

        {
            put(ConfigurationType.JOB, Job.class);
            put(ConfigurationType.JOBCLASS, JobClass.class);
            put(ConfigurationType.JUNCTION, Junction.class);
            put(ConfigurationType.LOCK, Lock.class);
            put(ConfigurationType.WORKINGDAYSCALENDAR, Calendar.class);
            put(ConfigurationType.NONWORKINGDAYSCALENDAR, Calendar.class);
            put(ConfigurationType.SCHEDULE, Schedule.class);
            put(ConfigurationType.WORKFLOW, Workflow.class);
            put(ConfigurationType.FOLDER, Folder.class);
        }
    });

    public static final Set<ConfigurationType> DEPLOYABLE_OBJECTS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(ConfigurationType.JOB,
            ConfigurationType.JOBCLASS, ConfigurationType.JUNCTION, ConfigurationType.LOCK, ConfigurationType.WORKFLOW)));

    public static final Set<ConfigurationType> RELEASABLE_OBJECTS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            ConfigurationType.SCHEDULE, ConfigurationType.NONWORKINGDAYSCALENDAR, ConfigurationType.WORKINGDAYSCALENDAR)));

    public static String getResourceImplPath(final String path) {
        return String.format("./%s/%s", APPLICATION_PATH, path);
    }

    public static ConfigurationType getType(Integer type) {
        ConfigurationType result = null;
        try {
            result = ConfigurationType.fromValue(type);
        } catch (Exception e) {
        }
        return result;
    }

    public static ConfigurationType getType(String type) {
        ConfigurationType result = null;
        try {
            result = ConfigurationType.fromValue(type);
        } catch (Exception e) {
        }
        return result;
    }

    public static boolean isFolder(ConfigurationType type) {
        return ConfigurationType.FOLDER.equals(type);
    }

    public static boolean isCalendar(ConfigurationType type) {
        return ConfigurationType.WORKINGDAYSCALENDAR.equals(type) || ConfigurationType.NONWORKINGDAYSCALENDAR.equals(type);
    }

    public static boolean isCalendar(Integer type) {
        return Arrays.asList(ConfigurationType.WORKINGDAYSCALENDAR.intValue(), ConfigurationType.NONWORKINGDAYSCALENDAR.intValue()).contains(type);
    }
    
    public static List<Integer> getTypesFromObjectsWithReferences() {
        return Arrays.asList(ConfigurationType.WORKFLOW.intValue(), ConfigurationType.SCHEDULE.intValue());
    }
    
    public static List<Integer> getTypesFromObjectsWithReferencesAndFolders() {
        return Arrays.asList(ConfigurationType.WORKFLOW.intValue(), ConfigurationType.SCHEDULE.intValue(), ConfigurationType.FOLDER.intValue());
    }

    public static List<Integer> getCalendarTypes() {
        return Arrays.asList(ConfigurationType.WORKINGDAYSCALENDAR.intValue(), ConfigurationType.NONWORKINGDAYSCALENDAR.intValue());
    }

    public static Set<Integer> getDeployableTypes() {
        return DEPLOYABLE_OBJECTS.stream().map(ConfigurationType::intValue).collect(Collectors.toSet());
    }

    public static Set<Integer> getReleasableTypes() {
        return RELEASABLE_OBJECTS.stream().map(ConfigurationType::intValue).collect(Collectors.toSet());
    }

    public static boolean isDeployable(ConfigurationType type) {
        return DEPLOYABLE_OBJECTS.contains(type);
    }

    public static boolean isReleasable(ConfigurationType type) {
        return RELEASABLE_OBJECTS.contains(type);
    }

    public static Set<Integer> getDeployableTypes(Collection<ConfigurationType> objectTypes) {
        if (objectTypes == null || objectTypes.isEmpty()) {
            return getDeployableTypes();
        }
        return objectTypes.stream().filter(type -> isDeployable(type)).map(ConfigurationType::intValue).collect(Collectors.toSet());
    }

    public static Set<Integer> getDeployableTypesWithFolder(Collection<ConfigurationType> objectTypes) {
        Set<Integer> deployables = getDeployableTypes(objectTypes);
        deployables.add(ConfigurationType.FOLDER.intValue());
        return deployables;
    }

    public static Set<Integer> getReleasableTypes(Collection<ConfigurationType> objectTypes) {
        if (objectTypes == null || objectTypes.isEmpty()) {
            return getReleasableTypes();
        }
        return objectTypes.stream().filter(type -> isReleasable(type)).map(ConfigurationType::intValue).collect(Collectors.toSet());
    }

    public static Set<Integer> getReleasableTypesWithFolder(Collection<ConfigurationType> objectTypes) {
        Set<Integer> releasables = getReleasableTypes(objectTypes);
        releasables.add(ConfigurationType.FOLDER.intValue());
        return releasables;
    }

    public static IConfigurationObject content2IJSObject(String content, Integer typeNum) throws JsonParseException, JsonMappingException,
            IOException {
        ConfigurationType type = getType(typeNum);
        if (SOSString.isEmpty(content) || ConfigurationType.FOLDER.equals(type)) {
            return null;
        }
        // temp. compatibility for whenHolidays enum
        if (ConfigurationType.SCHEDULE.equals(type)) {
            content = content.replaceAll("\"suppress\"", "\"SUPPRESS\"");
        }
        return (IConfigurationObject) Globals.objectMapper.readValue(content, CLASS_MAPPING.get(type));
    }

    public static void makeParentDirs(InventoryDBLayer dbLayer, Path parentFolder, Long auditLogId) throws SOSHibernateException {
        if (parentFolder != null) {
            String newFolder = parentFolder.toString().replace('\\', '/');
            if (!ROOT_FOLDER.equals(newFolder)) {
                DBItemInventoryConfiguration newDbFolder = dbLayer.getConfiguration(newFolder, ConfigurationType.FOLDER.intValue());
                if (newDbFolder == null) {
                    newDbFolder = new DBItemInventoryConfiguration();
                    newDbFolder.setPath(newFolder);
                    newDbFolder.setFolder(parentFolder.getParent().toString().replace('\\', '/'));
                    newDbFolder.setName(parentFolder.getFileName().toString());
                    newDbFolder.setDeployed(false);
                    newDbFolder.setReleased(false);
                    newDbFolder.setModified(Date.from(Instant.now()));
                    newDbFolder.setAuditLogId(auditLogId == null ? 0L : auditLogId);
                    newDbFolder.setContent(null);
                    newDbFolder.setCreated(newDbFolder.getModified());
                    newDbFolder.setDeleted(false);
                    newDbFolder.setDocumentationId(0L);
                    newDbFolder.setId(null);
                    newDbFolder.setTitle(null);
                    newDbFolder.setType(ConfigurationType.FOLDER);
                    newDbFolder.setValid(true);
                    dbLayer.getSession().save(newDbFolder);
                    makeParentDirs(dbLayer, parentFolder.getParent(), auditLogId);
                }
            }
        }
    }

    public static void makeParentDirs(InventoryDBLayer dbLayer, Path folder) throws SOSHibernateException {
        makeParentDirs(dbLayer, folder, null);
    }

    public static void makeParentDirsForTrash(InventoryDBLayer dbLayer, Path folder) throws SOSHibernateException {
        makeParentDirsForTrash(dbLayer, folder, null);
    }

    public static void makeParentDirsForTrash(InventoryDBLayer dbLayer, Path parentFolder, Long auditLogId) throws SOSHibernateException {
        if (parentFolder != null) {
            String newFolder = parentFolder.toString().replace('\\', '/');
            if (!ROOT_FOLDER.equals(newFolder)) {
                DBItemInventoryConfigurationTrash newDbFolder = dbLayer.getTrashConfiguration(newFolder, ConfigurationType.FOLDER.intValue());
                if (newDbFolder == null) {
                    newDbFolder = new DBItemInventoryConfigurationTrash();
                    newDbFolder.setPath(newFolder);
                    newDbFolder.setFolder(parentFolder.getParent().toString().replace('\\', '/'));
                    newDbFolder.setName(parentFolder.getFileName().toString());
                    newDbFolder.setModified(Date.from(Instant.now()));
                    newDbFolder.setAuditLogId(auditLogId == null ? 0L : auditLogId);
                    newDbFolder.setContent(null);
                    newDbFolder.setCreated(newDbFolder.getModified());
                    newDbFolder.setDocumentationId(0L);
                    newDbFolder.setId(null);
                    newDbFolder.setTitle(null);
                    newDbFolder.setType(ConfigurationType.FOLDER);
                    newDbFolder.setValid(true);
                    dbLayer.getSession().save(newDbFolder);
                    makeParentDirsForTrash(dbLayer, parentFolder.getParent(), auditLogId);
                }
            }
        }
    }

    public static String pathToName(String path) {
        return Paths.get(path).getFileName().toString();
    }

    public static List<DBItemInventoryConfiguration> deleteEmptyFolders(InventoryDBLayer dbLayer, DBItemInventoryConfiguration folder)
            throws SOSHibernateException {
        List<DBItemInventoryConfiguration> folderContent = dbLayer.getFolderContent(folder.getPath(), true, null);
        if (folderContent == null) {
            folderContent = new ArrayList<DBItemInventoryConfiguration>();
        }
        if (!ROOT_FOLDER.equals(folder.getPath())) {
            folderContent.add(folder);
        }
        return deleteEmptyFolders(dbLayer.getSession(), folderContent);
    }

    public static List<DBItemInventoryConfiguration> deleteEmptyFolders(InventoryDBLayer dbLayer, String folder) throws SOSHibernateException {
        List<DBItemInventoryConfiguration> folderContent = dbLayer.getFolderContent(folder, true, null);
        if (folderContent == null) {
            folderContent = new ArrayList<DBItemInventoryConfiguration>();
        }
        if (!ROOT_FOLDER.equals(folder)) {
            DBItemInventoryConfiguration dbFolder = dbLayer.getConfiguration(folder, ConfigurationType.FOLDER.intValue());
            if (dbFolder != null) {
                folderContent.add(dbFolder);
            }
        }
        return deleteEmptyFolders(dbLayer.getSession(), folderContent);
    }

    private static List<DBItemInventoryConfiguration> deleteEmptyFolders(SOSHibernateSession session,
            List<DBItemInventoryConfiguration> folderContent) throws SOSHibernateException {
        List<DBItemInventoryConfiguration> deletedFolders = new ArrayList<>();
        if (!folderContent.isEmpty()) {
            LinkedHashSet<DBItemInventoryConfiguration> folders = folderContent.stream().filter(i -> ConfigurationType.FOLDER.intValue() == i
                    .getType()).sorted(Comparator.comparing(DBItemInventoryConfiguration::getPath).reversed()).collect(Collectors.toCollection(
                            LinkedHashSet::new));
            for (DBItemInventoryConfiguration folder : folders) {
                if (!folderContent.stream().parallel().anyMatch(i -> folder.getPath().equals(i.getFolder()))) {
                    session.delete(folder);
                    folderContent.remove(folder);
                    deletedFolders.add(folder);
                }
            }
        }
        return deletedFolders;
    }

    public static void postEvent(String folder) {
        EventBus.getInstance().post(new InventoryEvent(folder));
    }
    
    public static void postTrashEvent(String folder) {
        EventBus.getInstance().post(new InventoryTrashEvent(folder));
    }

    public static class InventoryPath {

        private String path = "";
        private String name = "";
        private String folder = ROOT_FOLDER;

        public InventoryPath(final String inventoryPath, ConfigurationType type) {
            if (!SOSString.isEmpty(inventoryPath)) {
                path = Globals.normalizePath(inventoryPath);
                Path p = Paths.get(path);
                name = p.getFileName().toString();
                CheckJavaVariableName.test(type.value().toLowerCase(), name);
                folder = normalizeFolder(p.getParent());
            }
        }

        public InventoryPath(final java.nio.file.Path inventoryPath, ConfigurationType type) {
            if (inventoryPath != null) {
                path = inventoryPath.toString().replace('\\', '/');
                name = inventoryPath.getFileName().toString();
                CheckJavaVariableName.test(type.value().toLowerCase(), name);
                folder = normalizeFolder(inventoryPath.getParent());
            }
        }

        public String getPath() {
            return path;
        }

        public String getName() {
            return name;
        }

        public String getFolder() {
            return folder;
        }

        private String normalizeFolder(Path folder) {
            if (folder == null) {
                return ROOT_FOLDER;
            }
            String s = folder.toString().replace('\\', '/');
            return SOSString.isEmpty(s) ? ROOT_FOLDER : s;
        }
    }

    public static DBItemInventoryConfiguration getConfiguration(InventoryDBLayer dbLayer, RequestFilter in,
            SOSShiroFolderPermissions folderPermissions) throws Exception {
        return getConfiguration(dbLayer, in.getId(), in.getPath(), in.getObjectType(), folderPermissions);
    }

    public static DBItemInventoryConfiguration getConfiguration(InventoryDBLayer dbLayer, Long id, String path, ConfigurationType type,
            SOSShiroFolderPermissions folderPermissions) throws Exception {
        DBItemInventoryConfiguration config = null;
        String name = null;
        if (id != null) {
            config = dbLayer.getConfiguration(id);
            if (config == null) {
                throw new DBMissingDataException(String.format("configuration not found: %s", id));
            }
            if (!folderPermissions.isPermittedForFolder(config.getFolder())) {
                throw new JocFolderPermissionsException("Access denied for folder: " + config.getFolder());
            }
            // temp. because of rename error on root folder
            config.setPath(config.getPath().replace("//+", "/"));
        } else {
            if (!ConfigurationType.FOLDER.equals(type) && path != null && !path.contains("/")) {
                name = path;
                path = null;
            }
            if (path != null) {
                if (JocInventory.ROOT_FOLDER.equals(path) && ConfigurationType.FOLDER.equals(type)) {
                    config = new DBItemInventoryConfiguration();
                    config.setId(0L);
                    config.setPath(path);
                    config.setType(type);
                    config.setFolder(path);
                    config.setDeleted(false);
                    config.setValid(true);
                    config.setDeployed(false);
                    config.setReleased(false);
                } else {
                    path = normalizePath(path).toString().replace('\\', '/');
                    if (!folderPermissions.isPermittedForFolder(path)) {
                        throw new JocFolderPermissionsException("Access denied for folder: " + path);
                    }
                    config = dbLayer.getConfiguration(path, type.intValue());
                    if (config == null) {
                        throw new DBMissingDataException(String.format("%s not found: %s", type.value().toLowerCase(), path));
                    }
                }
            } else if (name != null) {// name
                List<DBItemInventoryConfiguration> configs = dbLayer.getConfigurationByName(name, type.intValue());
                if (configs == null || configs.isEmpty()) {
                    throw new DBMissingDataException(String.format("configuration not found: %s", name));
                }
                config = configs.get(0); // TODO
                if (!folderPermissions.isPermittedForFolder(config.getFolder())) {
                    throw new JocFolderPermissionsException("Access denied for folder: " + config.getFolder());
                }
                // temp. because of rename error on root folder
                config.setPath(config.getPath().replace("//+", "/"));
            }
        }
        return config;
    }
    
    public static DBItemInventoryConfigurationTrash getTrashConfiguration(InventoryDBLayer dbLayer, RequestFilter in,
            SOSShiroFolderPermissions folderPermissions) throws Exception {
        return getTrashConfiguration(dbLayer, in.getId(), in.getPath(), in.getObjectType(), folderPermissions);
    }

    public static DBItemInventoryConfigurationTrash getTrashConfiguration(InventoryDBLayer dbLayer, Long id, String path, ConfigurationType type,
            SOSShiroFolderPermissions folderPermissions) throws Exception {
        DBItemInventoryConfigurationTrash config = null;
        if (id != null) {
            config = dbLayer.getTrashConfiguration(id);
            if (config == null) {
                throw new DBMissingDataException(String.format("configuration not found: %s", id));
            }
            if (!folderPermissions.isPermittedForFolder(config.getFolder())) {
                throw new JocFolderPermissionsException("Access denied for folder: " + config.getFolder());
            }
        } else if (path != null) {
            if (JocInventory.ROOT_FOLDER.equals(path) && ConfigurationType.FOLDER.equals(type)) {
                config = new DBItemInventoryConfigurationTrash();
                config.setId(0L);
                config.setPath(path);
                config.setType(type);
                config.setFolder(path);
                config.setValid(true);
            } else {
                path = normalizePath(path).toString().replace('\\', '/');
                if (!folderPermissions.isPermittedForFolder(path)) {
                    throw new JocFolderPermissionsException("Access denied for folder: " + path);
                }
                config = dbLayer.getTrashConfiguration(path, type.intValue());
                if (config == null) {
                    throw new DBMissingDataException(String.format("%s not found: %s", type.value().toLowerCase(), path));
                }
            }
        }
        return config;
    }

    public static Path normalizePath(String path) {
        return Paths.get(JocInventory.ROOT_FOLDER).resolve(path).normalize();
    }

    public static String toString(IConfigurationObject config) throws JsonProcessingException {
        return config == null ? "null" : Globals.objectMapper.writeValueAsString(config);
    }

    public static String hash(IConfigurationObject config) throws JsonProcessingException {
        return SOSString.hash(toString(config));
    }

    public static void handleWorkflowSearch(SOSHibernateSession session, Set<DBItemDeploymentHistory> deployments, boolean deleteDeployments) {
        if (deployments == null || deployments.size() == 0) {
            return;
        }

        Map<Long, List<DBItemDeploymentHistory>> groupedWorkflows = deployments.stream().filter(h -> h.getType().equals(ConfigurationType.WORKFLOW
                .intValue())).collect(Collectors.groupingBy(DBItemDeploymentHistory::getInventoryConfigurationId));

        groupedWorkflows.forEach((inventoryId, list) -> {
            if (list.size() > 0) {
                String content = list.get(0).getInvContent();
                if (!SOSString.isEmpty(content)) {
                    try {
                        Workflow workflow = Globals.objectMapper.readValue(content, Workflow.class);
                        workflow.setVersionId(null); // make same versionId for all deployments

                        String controllerId = list.get(0).getControllerId();
                        List<Long> deploymentIds = list.stream().map(DBItemDeploymentHistory::getId).collect(Collectors.toList());
                        try {
                            handleWorkflowSearch(new InventoryDBLayer(session), workflow, inventoryId, controllerId, deploymentIds,
                                    deleteDeployments);
                        } catch (Throwable e) {
                            LOGGER.error(e.toString(), e);
                        }
                    } catch (Throwable e) {
                        LOGGER.error(e.toString(), e);
                    }
                }
            }

        });
    }

    private static void handleWorkflowSearch(InventoryDBLayer dbLayer, DBItemInventoryConfiguration item, IConfigurationObject config)
            throws JsonParseException, JsonMappingException, IOException, SOSHibernateException {
        if (ConfigurationType.WORKFLOW.intValue().equals(item.getType())) {
            if (config == null && !SOSString.isEmpty(item.getContent())) {
                config = Globals.objectMapper.readValue(item.getContent(), Workflow.class);
            }
            JocInventory.handleWorkflowSearch(dbLayer, (Workflow) config, item.getId());
        }
    }

    public static void handleWorkflowSearch(InventoryDBLayer dbLayer, Workflow workflow, Long inventoryId) throws JsonProcessingException,
            SOSHibernateException {
        handleWorkflowSearch(dbLayer, workflow, inventoryId, null, null, false);
    }

    public static void handleWorkflowSearch(InventoryDBLayer dbLayer, Workflow workflow, Long inventoryId, String controllerId,
            List<Long> deploymentIds, boolean deleteDeployments) throws JsonProcessingException, SOSHibernateException {

        String hash = hash(workflow);
        boolean deployed = deploymentIds != null;

        DBItemSearchWorkflow item = dbLayer.getSearchWorkflow(inventoryId, deployed ? hash : null);
        if (item == null) {
            if (deleteDeployments) {
                return;
            }
            if (deployed) {
                DBItemSearchWorkflow draft = dbLayer.getSearchWorkflow(inventoryId, null);
                if (draft != null) {
                    item = draft;
                }
            }
            if (item == null) {
                item = new DBItemSearchWorkflow();
            }

            item.setInventoryConfigurationId(inventoryId);
            item.setDeployed(deployed);
            item.setContentHash(hash);
            item = convert(item, workflow);
            if (item.getCreated() == null) {
                item.setCreated(new Date());
                item.setModified(item.getCreated());
                dbLayer.getSession().save(item);
            } else {
                item.setModified(new Date());
                dbLayer.getSession().update(item);
            }
            if (deployed) {
                dbLayer.searchWorkflow2DeploymentHistory(item.getId(), inventoryId, controllerId, deploymentIds, false);
            }
        } else {
            if (deployed) {// same hash
                dbLayer.searchWorkflow2DeploymentHistory(item.getId(), inventoryId, controllerId, deploymentIds, deleteDeployments);
            } else {
                if (!hash.equals(item.getContentHash())) {
                    item.setContentHash(hash);
                    item.setModified(new Date());
                    item = convert(item, workflow);
                    dbLayer.getSession().update(item);
                }
            }
        }
    }

    private static DBItemSearchWorkflow convert(DBItemSearchWorkflow item, Workflow workflow) {
        if (workflow == null) {
            item.setJobsCount(0);
            item.setJobs(null);
            item.setJobsArgs(null);
            item.setJobsScripts(null);
            item.setInstructions(null);
            item.setInstructionsArgs(null);
        } else {
            WorkflowConverter converter = new WorkflowConverter();
            converter.process(workflow);

            item.setJobsCount(converter.getJobs().getNames().size());
            item.setJobs(converter.getJobs().getMainInfo().toString());
            item.setJobsArgs(converter.getJobs().getArgInfo().toString());
            item.setJobsScripts(converter.getJobs().getScriptInfo().toString());
            item.setInstructions(converter.getInstructions().getMainInfo().toString());
            item.setInstructionsArgs(converter.getInstructions().getArgInfo().toString());
        }
        return item;
    }

    public static void deleteConfiguration(InventoryDBLayer dbLayer, DBItemInventoryConfiguration item) throws SOSHibernateException {
        if (ConfigurationType.WORKFLOW.intValue().equals(item.getType())) {
            dbLayer.deleteSearchWorkflowByInventoryId(item.getId(), false);
        }
        dbLayer.getSession().delete(item);
    }

    public static void updateConfiguration(InventoryDBLayer dbLayer, DBItemInventoryConfiguration item) throws JsonParseException,
            JsonMappingException, SOSHibernateException, JsonProcessingException, IOException {
        updateConfiguration(dbLayer, item, null);
    }

    public static void updateConfiguration(InventoryDBLayer dbLayer, DBItemInventoryConfiguration item, IConfigurationObject config)
            throws SOSHibernateException, JsonParseException, JsonMappingException, JsonProcessingException, IOException {
        dbLayer.getSession().update(item);

        handleWorkflowSearch(dbLayer, item, config);
    }

    public static void insertConfiguration(InventoryDBLayer dbLayer, DBItemInventoryConfiguration item) throws SOSHibernateException,
            JsonParseException, JsonMappingException, JsonProcessingException, IOException {
        insertConfiguration(dbLayer, item, null);
    }

    public static void insertConfiguration(InventoryDBLayer dbLayer, DBItemInventoryConfiguration item, IConfigurationObject config)
            throws SOSHibernateException, JsonParseException, JsonMappingException, JsonProcessingException, IOException {
        dbLayer.getSession().save(item);

        handleWorkflowSearch(dbLayer, item, config);
    }

    public static void setConfigurationsDeployed(SOSHibernateSession session, Set<DBItemInventoryConfiguration> configs)
            throws SOSHibernateException {
        if (configs.size() > 0) {
            for (DBItemInventoryConfiguration config : configs) {
                config.setDeployed(true);
                config.setModified(Date.from(Instant.now()));
                session.update(config);
            }

            List<Long> workflows = configs.stream().filter(c -> c.getType().equals(ConfigurationType.WORKFLOW.intValue())).map(
                    DBItemInventoryConfiguration::getId).collect(Collectors.toList());
            if (workflows.size() > 0) {
                // InventoryDBLayer dbLayer = new InventoryDBLayer(session);
                // dbLayer.deleteSearchWorkflowByInventoryId(id, deployed)
            }

        }
    }

    public static void deleteInventoryConfigurationAndPutToTrash(DBItemInventoryConfiguration item, InventoryDBLayer dbLayer) {
        if (item != null) {
            try {
                DBItemInventoryConfigurationTrash trashItem = dbLayer.getTrashConfiguration(item.getPath(), item.getType());
                deleteConfiguration(dbLayer, item);
                Date now = Date.from(Instant.now());
                if (trashItem == null) {
                    trashItem = new DBItemInventoryConfigurationTrash();
                    trashItem.setId(null);
                    trashItem.setPath(item.getPath());
                    trashItem.setName(item.getName());
                    trashItem.setFolder(item.getFolder());
                    trashItem.setCreated(now);
                    trashItem.setType(item.getType());
                }
                trashItem.setAuditLogId(item.getAuditLogId());
                trashItem.setContent(item.getContent());
                trashItem.setDocumentationId(item.getDocumentationId());
                trashItem.setTitle(item.getTitle());
                trashItem.setValid(item.getValid());
                trashItem.setModified(now);
                if (trashItem.getId() == null) {
                    dbLayer.getSession().save(trashItem);
                    makeParentDirsForTrash(dbLayer, Paths.get(trashItem.getFolder()), trashItem.getAuditLogId());
                } else {
                    dbLayer.getSession().update(trashItem);
                }
            } catch (SOSHibernateInvalidSessionException ex) {
                throw new DBConnectionRefusedException(ex);
            } catch (Exception ex) {
                throw new DBInvalidDataException(ex);
            }
        }
    } //List<DBItemInventoryConfiguration> dBFolderContent
    
    public static Set<String> deepCopy(DBItemInventoryConfiguration config, String newName, InventoryDBLayer dbLayer) throws JsonParseException,
            JsonMappingException, SOSHibernateException, JsonProcessingException, IOException {
        return deepCopy(config, newName, Collections.emptyList(), dbLayer);
    }

    public static Set<String> deepCopy(DBItemInventoryConfiguration config, String newName, List<DBItemInventoryConfiguration> items,
            InventoryDBLayer dbLayer) throws JsonParseException, JsonMappingException, SOSHibernateException, JsonProcessingException, IOException {
        Set<String> events = new HashSet<>();
        switch (config.getTypeAsEnum()) {
        case LOCK: // determine Workflows with Lock instructions
            List<DBItemInventoryConfiguration> workflows = dbLayer.getUsedWorkflowsByLockId(config.getName());
            if (workflows != null && !workflows.isEmpty()) {
                for (DBItemInventoryConfiguration workflow : workflows) {
                    workflow.setContent(workflow.getContent().replaceAll("(\"lockId\"\\s*:\\s*\")" + config.getName() + "\"", "$1" + newName + "\""));
                    workflow.setDeployed(false);
                    int i = items.indexOf(workflow);
                    if (i != -1) {
                        items.get(i).setContent(workflow.getContent());
                        items.get(i).setDeployed(false);
                    } else{
                        JocInventory.updateConfiguration(dbLayer, workflow);
                        events.add(workflow.getFolder());
                    }
                }
            }
            break;
        case WORKFLOW: // determine Schedules with Workflow reference
            List<DBItemInventoryConfiguration> schedules = dbLayer.getUsedSchedulesByWorkflowName(config.getName());
            if (schedules != null && !schedules.isEmpty()) {
                for (DBItemInventoryConfiguration schedule : schedules) {
                    schedule.setContent(schedule.getContent().replaceAll("(\"workflowName\"\\s*:\\s*\")" + config.getName() + "\"", "$1" + newName
                            + "\""));
                    schedule.setReleased(false);
                    int i = items.indexOf(schedule);
                    if (i != -1) {
                        items.get(i).setContent(schedule.getContent());
                        items.get(i).setReleased(false);
                    } else{
                        JocInventory.updateConfiguration(dbLayer, schedule);
                        events.add(schedule.getFolder());
                    }
                }
            }
            break;
        case WORKINGDAYSCALENDAR: // determine Schedules with Calendar reference
        case NONWORKINGDAYSCALENDAR:
            List<DBItemInventoryConfiguration> schedules1 = dbLayer.getUsedSchedulesByCalendarName(config.getName());
            if (schedules1 != null && !schedules1.isEmpty()) {
                for (DBItemInventoryConfiguration schedule : schedules1) {
                    schedule.setContent(schedule.getContent().replaceAll("(\"calendarName\"\\s*:\\s*\")" + config.getName() + "\"", "$1" + newName
                            + "\""));
                    schedule.setReleased(false);
                    int i = items.indexOf(schedule);
                    if (i != -1) {
                        items.get(i).setContent(schedule.getContent());
                        items.get(i).setReleased(false);
                    } else{
                        JocInventory.updateConfiguration(dbLayer, schedule);
                        events.add(schedule.getFolder());
                    }
                }
            }
            break;
        default:
            break;
        }
        return events;
    }
}
