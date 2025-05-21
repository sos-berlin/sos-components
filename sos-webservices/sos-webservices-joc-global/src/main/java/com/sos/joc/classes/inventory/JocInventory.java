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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.classes.SOSAuthFolderPermissions;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.board.Board;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.common.IInventoryObject;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.descriptor.DeploymentDescriptor;
import com.sos.inventory.model.fileordersource.FileOrderSource;
import com.sos.inventory.model.instruction.InstructionType;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.job.ExecutableType;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.jobclass.JobClass;
import com.sos.inventory.model.jobresource.JobResource;
import com.sos.inventory.model.jobtemplate.JobTemplate;
import com.sos.inventory.model.lock.Lock;
import com.sos.inventory.model.report.Report;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.script.Script;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.inventory.search.WorkflowConverter;
import com.sos.joc.classes.order.OrderTags;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.classes.tag.JobTags;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryConfigurationTrash;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.InventoryJobTagDBLayer;
import com.sos.joc.db.inventory.items.InventoryDeploymentItem;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.db.search.DBItemSearchWorkflow;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.deploy.DeployHistoryFileOrdersSourceEvent;
import com.sos.joc.event.bean.deploy.DeployHistoryJobResourceEvent;
import com.sos.joc.event.bean.deploy.DeployHistoryWorkflowEvent;
import com.sos.joc.event.bean.deploy.DeployHistoryWorkflowPathEvent;
import com.sos.joc.event.bean.inventory.InventoryEvent;
import com.sos.joc.event.bean.inventory.InventoryFolderEvent;
import com.sos.joc.event.bean.inventory.InventoryJobTagEvent;
import com.sos.joc.event.bean.inventory.InventoryObjectEvent;
import com.sos.joc.event.bean.inventory.InventoryTagEvent;
import com.sos.joc.event.bean.inventory.InventoryTrashEvent;
import com.sos.joc.event.bean.inventory.InventoryTrashFolderEvent;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.exceptions.JocMissingCommentException;
import com.sos.joc.model.SuffixPrefix;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.IsReferencedBy;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.ItemStateEnum;
import com.sos.joc.model.inventory.common.RequestFilter;
import com.sos.joc.model.inventory.folder.Folder;
import com.sos.joc.model.publish.OperationType;

public class JocInventory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocInventory.class);
    public static final String APPLICATION_PATH = "inventory";
    public static final String ROOT_FOLDER = "/";
    public static final String DEFAULT_COPY_SUFFIX = "copy";
    public static final String DEFAULT_RESTORE_SUFFIX = "restored";
    public static final String DEFAULT_IMPORT_SUFFIX = "imported";
    // introduced with 2.3.0 - compatibility with scheduler.workflowName (deprecated, scheduler.workflowNames will be used)
    public static final boolean SCHEDULE_CONSIDER_WORKFLOW_NAME = true;
    // introduced with 2.3.0 - to find the code to handle the schedule workflows
    public static final int SCHEDULE_MIN_MULTIPLE_WORKFLOWS_SIZE = 2;

    public static final Map<ConfigurationType, String> SCHEMA_LOCATION = Collections.unmodifiableMap(new HashMap<ConfigurationType, String>() {

        private static final long serialVersionUID = 1L;

        {
            put(ConfigurationType.WORKINGDAYSCALENDAR, "classpath:/raml/inventory/schemas/calendar/calendar-schema.json");
            put(ConfigurationType.NONWORKINGDAYSCALENDAR, "classpath:/raml/inventory/schemas/calendar/calendar-schema.json");
            put(ConfigurationType.JOBTEMPLATE, "classpath:/raml/inventory/schemas/jobTemplate/jobTemplate-schema.json");
            put(ConfigurationType.JOBCLASS, "classpath:/raml/inventory/schemas/jobClass/jobClass-schema.json");
            put(ConfigurationType.JOBRESOURCE, "classpath:/raml/inventory/schemas/jobresource/jobResource-schema.json");
            put(ConfigurationType.LOCK, "classpath:/raml/inventory/schemas/lock/lock-schema.json");
            put(ConfigurationType.FILEORDERSOURCE, "classpath:/raml/inventory/schemas/fileordersource/fileOrderSource-schema.json");
            put(ConfigurationType.SCHEDULE, "classpath:/raml/inventory/schemas/schedule/schedule-schema.json");
            put(ConfigurationType.INCLUDESCRIPT, "classpath:/raml/inventory/schemas/script/script-schema.json");
            put(ConfigurationType.WORKFLOW, "classpath:/raml/inventory/schemas/workflow/workflow-schema.json");
            put(ConfigurationType.NOTICEBOARD, "classpath:/raml/inventory/schemas/board/board-schema.json");
            put(ConfigurationType.REPORT, "classpath:/raml/inventory/schemas/report/report-schema.json");
            put(ConfigurationType.FOLDER, "classpath:/raml/api/schemas/inventory/folder-schema.json");
            put(ConfigurationType.DEPLOYMENTDESCRIPTOR, "classpath:/raml/inventory/schemas/deploymentDescriptor/deploymentDescriptor-schema.json");
            put(ConfigurationType.DESCRIPTORFOLDER, "classpath:/raml/api/schemas/inventory/folder-schema.json");
        }
    });

    public static final Map<InstructionType, String> INSTRUCTION_SCHEMA_LOCATION = Collections.unmodifiableMap(
            new HashMap<InstructionType, String>() {

                private static final long serialVersionUID = 1L;

                {
                    put(InstructionType.EXECUTE_NAMED, "classpath:/raml/inventory/schemas/instruction/namedJob-schema.json");
                    put(InstructionType.FAIL, "classpath:/raml/inventory/schemas/instruction/fail-schema.json");
                    put(InstructionType.FINISH, "classpath:/raml/inventory/schemas/instruction/finish-schema.json");
                    put(InstructionType.FORK, "classpath:/raml/inventory/schemas/instruction/forkJoin-schema.json");
                    put(InstructionType.FORKLIST, "classpath:/raml/inventory/schemas/instruction/forkList-schema.json");
                    put(InstructionType.IF, "classpath:/raml/inventory/schemas/instruction/ifelse-schema.json");
                    put(InstructionType.CASE_WHEN, "classpath:/raml/inventory/schemas/instruction/caseWhen-schema.json");
                    put(InstructionType.LOCK, "classpath:/raml/inventory/schemas/instruction/lock-schema.json");
                    put(InstructionType.RETRY, "classpath:/raml/inventory/schemas/instruction/retryInCatch-schema.json");
                    put(InstructionType.TRY, "classpath:/raml/inventory/schemas/instruction/retry-schema.json");
                    put(InstructionType.PROMPT, "classpath:/raml/inventory/schemas/instruction/prompt-schema.json");
                    put(InstructionType.POST_NOTICE, "classpath:/raml/inventory/schemas/instruction/postNotice-schema.json");
                    put(InstructionType.POST_NOTICES, "classpath:/raml/inventory/schemas/instruction/postNotices-schema.json");
                    put(InstructionType.EXPECT_NOTICE, "classpath:/raml/inventory/schemas/instruction/expectNotice-schema.json");
                    put(InstructionType.EXPECT_NOTICES, "classpath:/raml/inventory/schemas/instruction/expectNotices-schema.json");
                    put(InstructionType.CONSUME_NOTICES, "classpath:/raml/inventory/schemas/instruction/consumeNotices-schema.json");
                    put(InstructionType.ADD_ORDER, "classpath:/raml/inventory/schemas/instruction/addOrder-schema.json");
                    put(InstructionType.CYCLE, "classpath:/raml/inventory/schemas/instruction/cycle-schema.json");
                    put(InstructionType.STICKY_SUBAGENT, "classpath:/raml/inventory/schemas/instruction/stickySubagent-schema.json");
                    put(InstructionType.OPTIONS, "classpath:/raml/inventory/schemas/instruction/options-schema.json");
                    put(InstructionType.BREAK, "classpath:/raml/inventory/schemas/instruction/break-schema.json");
                    put(InstructionType.SLEEP, "classpath:/raml/inventory/schemas/instruction/sleep-schema.json");
                }
            });
    
    public static final String FORKLIST_SCHEMA_WITHOUT_LICENSE = "classpath:/raml/inventory/schemas/instruction/forkListWithoutLicense-schema.json";

    public static final Map<ConfigurationType, Class<?>> CLASS_MAPPING = Collections.unmodifiableMap(new HashMap<ConfigurationType, Class<?>>() {

        private static final long serialVersionUID = 1L;

        {
            put(ConfigurationType.JOBTEMPLATE, JobTemplate.class);
            put(ConfigurationType.JOBCLASS, JobClass.class);
            put(ConfigurationType.JOBRESOURCE, JobResource.class);
            put(ConfigurationType.LOCK, Lock.class);
            put(ConfigurationType.FILEORDERSOURCE, FileOrderSource.class);
            put(ConfigurationType.WORKINGDAYSCALENDAR, Calendar.class);
            put(ConfigurationType.NONWORKINGDAYSCALENDAR, Calendar.class);
            put(ConfigurationType.SCHEDULE, Schedule.class);
            put(ConfigurationType.INCLUDESCRIPT, Script.class);
            put(ConfigurationType.WORKFLOW, Workflow.class);
            put(ConfigurationType.NOTICEBOARD, Board.class);
            put(ConfigurationType.REPORT, Report.class);
            put(ConfigurationType.FOLDER, Folder.class);
            put(ConfigurationType.DEPLOYMENTDESCRIPTOR, DeploymentDescriptor.class);
            put(ConfigurationType.DESCRIPTORFOLDER, Folder.class);
        }
    });

    public static final Set<ConfigurationType> DEPLOYABLE_OBJECTS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            ConfigurationType.JOBCLASS, ConfigurationType.FILEORDERSOURCE, ConfigurationType.LOCK, ConfigurationType.WORKFLOW,
            ConfigurationType.JOBRESOURCE, ConfigurationType.NOTICEBOARD)));

    public static final Set<ConfigurationType> RELEASABLE_OBJECTS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            ConfigurationType.SCHEDULE, ConfigurationType.INCLUDESCRIPT, ConfigurationType.NONWORKINGDAYSCALENDAR,
            ConfigurationType.WORKINGDAYSCALENDAR, ConfigurationType.JOBTEMPLATE, ConfigurationType.REPORT)));

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

    public static boolean isDescriptor (ConfigurationType type) {
        return EnumSet.of(ConfigurationType.DEPLOYMENTDESCRIPTOR, ConfigurationType.DESCRIPTORFOLDER).contains(type);
    }
    
    public static boolean isFolder(ConfigurationType type) {
        return ConfigurationType.FOLDER.equals(type) || ConfigurationType.DESCRIPTORFOLDER.equals(type);
    }

    public static boolean isFolder(Integer type) {
        return (ConfigurationType.FOLDER.intValue() == type) || (ConfigurationType.DESCRIPTORFOLDER.intValue() == type);
    }

    public static boolean isWorkflow(Integer type) {
        return ConfigurationType.WORKFLOW.intValue() == type;
    }

    public static boolean isCalendar(ConfigurationType type) {
        return ConfigurationType.WORKINGDAYSCALENDAR.equals(type) || ConfigurationType.NONWORKINGDAYSCALENDAR.equals(type);
    }

    public static boolean isCalendar(Integer type) {
        return Arrays.asList(ConfigurationType.WORKINGDAYSCALENDAR.intValue(), ConfigurationType.NONWORKINGDAYSCALENDAR.intValue()).contains(type);
    }

    public static List<Integer> getTypesFromObjectsWithReferences() {
        return Arrays.asList(ConfigurationType.WORKFLOW.intValue(), ConfigurationType.FILEORDERSOURCE.intValue(), ConfigurationType.SCHEDULE
                .intValue());
    }

    public static List<Integer> getTypesFromObjectsWithReferencesAndFolders() {
        return Arrays.asList(ConfigurationType.WORKFLOW.intValue(), ConfigurationType.FILEORDERSOURCE.intValue(), ConfigurationType.SCHEDULE
                .intValue(), ConfigurationType.FOLDER.intValue());
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
        Set<Integer> deployables = objectTypes.stream().filter(type -> isDeployable(type)).map(ConfigurationType::intValue).collect(Collectors.toSet());
        if (deployables.isEmpty()) {
            return getDeployableTypes();
        }
        return deployables;
    }

    public static Set<Integer> getDeployableTypesWithFolder(Collection<ConfigurationType> objectTypes) {
        Set<Integer> deployables = getDeployableTypes(objectTypes);
        deployables.add(ConfigurationType.FOLDER.intValue());
        return deployables;
    }

    public static Set<Integer> getReleasableTypes(Collection<ConfigurationType> objectTypes) {
        Set<Integer> releasables = getReleasableTypesStream(objectTypes).map(ConfigurationType::intValue).collect(Collectors.toSet());
        if (releasables.isEmpty()) {
            return getReleasableTypes();
        }
        return releasables;
    }
    
    public static Stream<ConfigurationType> getReleasableTypesStream(Collection<ConfigurationType> objectTypes) {
        if (objectTypes == null || objectTypes.isEmpty()) {
            return Stream.empty();
        }
        return objectTypes.stream().filter(type -> isReleasable(type));
    }

    public static Set<Integer> getReleasableTypesWithFolder(Collection<ConfigurationType> objectTypes) {
        Set<Integer> releasables = getReleasableTypes(objectTypes);
        releasables.add(ConfigurationType.FOLDER.intValue());
        return releasables;
    }
    
    public static Workflow workflowContent2Workflow(String content) throws JsonProcessingException {
        if (SOSString.isEmpty(content)) {
            return null;
        }
        return com.sos.joc.classes.inventory.WorkflowConverter.convertInventoryWorkflow(content);
    }

    public static IConfigurationObject content2IJSObject(String content, ConfigurationType type) throws JsonParseException, JsonMappingException,
            IOException {
        if (SOSString.isEmpty(content) || ConfigurationType.FOLDER.equals(type)) {
            return null;
        }
        switch (type) {
        case WORKFLOW:
            return com.sos.joc.classes.inventory.WorkflowConverter.convertInventoryWorkflow(content);
        case JOBTEMPLATE:
            return convertJobTemplate(content, JobTemplate.class);
        case SCHEDULE:
            return convertSchedule(content, Schedule.class);
        case FILEORDERSOURCE:
            return convertFileOrderSource(content, FileOrderSource.class);
        case JOBRESOURCE:
            return convertDefault(content, JobResource.class);
        case LOCK:
            return convertDefault(content, Lock.class);
        case NOTICEBOARD:
            return convertDefault(content, Board.class);
        case WORKINGDAYSCALENDAR:
        case NONWORKINGDAYSCALENDAR:
            return convertDefault(content, Calendar.class);
        default:
//            return convertDefault(content, (Class) CLASS_MAPPING.get(type));
            IConfigurationObject obj = (IConfigurationObject) Globals.objectMapper.readValue(content, CLASS_MAPPING.get(type));
            ((IInventoryObject) obj).setVersion(Globals.getStrippedInventoryVersion());
            return obj;
        }
    }

    public static IConfigurationObject content2IJSObject(String content, Integer typeNum) throws JsonParseException, JsonMappingException,
            IOException {
        return content2IJSObject(content, getType(typeNum));
    }
    
    public static <T extends FileOrderSource> T convertFileOrderSource(String content, Class<T> clazz) throws JsonMappingException,
            JsonProcessingException {
        // for compatibility directory -> directoryExpr
        T fos = Globals.objectMapper.readValue(content, clazz);
        if (fos.getDirectoryExpr() == null || fos.getDirectoryExpr().isEmpty()) {
            fos.setDirectoryExpr(JsonSerializer.quoteString(fos.getDirectory()));
            fos.setDirectory(null);
        }
        fos.setVersion(Globals.getStrippedInventoryVersion());
        return fos;
    }
    
    public static <T extends Schedule> T convertSchedule(String content, Class<T> clazz) throws JsonMappingException, JsonProcessingException {
        // JOC-1255 workflowName -> workflowNames
        T s = setWorkflowNames(Globals.objectMapper.readValue(content, clazz));
        s.setWorkflowName(null);
        s.setVersion(Globals.getStrippedInventoryVersion());
        return s;
    }

    public static <T extends JobTemplate> T convertJobTemplate(String content, Class<T> clazz) throws JsonMappingException, JsonProcessingException {
        // JOC-1255 workflowName -> workflowNames
        T jt = Globals.objectMapper.readValue(content, clazz);
        com.sos.joc.classes.inventory.WorkflowConverter.addInternalExecutableType(jt.getExecutable());
        jt.setVersion(Globals.getStrippedInventoryVersion());
        return jt;
    }

    public static <T extends IConfigurationObject> T convertDefault(String content, Class<T> clazz) throws JsonMappingException, JsonProcessingException {
        T obj = Globals.objectMapper.readValue(content, clazz);
        ((IInventoryObject) obj).setVersion(Globals.getStrippedInventoryVersion());
        return obj;
    }
    
    public static ConfigurationObject convert(DBItemInventoryConfiguration dbItem, SOSHibernateSession session)
            throws SOSHibernateException, JsonParseException, JsonMappingException, IOException {
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        ConfigurationObject cfg = new ConfigurationObject();
        ConfigurationType type = dbItem.getTypeAsEnum();
        cfg.setId(null);
        cfg.setDeliveryDate(Date.from(Instant.now()));
        cfg.setPath(dbItem.getPath());
        cfg.setObjectType(type);
        cfg.setValid(dbItem.getValid());
        cfg.setDeleted(dbItem.getDeleted());
        cfg.setState(ItemStateEnum.NO_CONFIGURATION_EXIST);
        cfg.setConfigurationDate(dbItem.getModified());
        cfg.setDeployed(dbItem.getDeployed());
        cfg.setReleased(dbItem.getReleased());
        cfg.setDeployments(null);
        cfg.setHasDeployments(false);
        cfg.setHasReleases(false);
        cfg.setIsReferencedBy(null);
        if (dbItem.getDeployed()) {
            DBItemDeploymentHistory lastDeployment = dbLayer.getLatestActiveDepHistoryItem(dbItem.getId());
            if (lastDeployment != null && OperationType.UPDATE.value().equals(lastDeployment.getOperation())) {
                dbItem.setContent(lastDeployment.getInvContent());
            } else {
                throw new DBMissingDataException(
                        String.format("Couldn't find deployed configuration: %1$s:%2$s ", type.value().toLowerCase(), dbItem.getPath()));
            }
        }
        cfg.setConfiguration(JocInventory.content2IJSObject(dbItem.getContent(), dbItem.getType()));
        if (JocInventory.isDeployable(type)) {
            cfg.setReleased(false);
            InventoryDeploymentItem lastDeployment = dbLayer.getLastDeploymentHistory(dbItem.getId());
            cfg.setHasDeployments(lastDeployment != null);
            if (dbItem.getDeployed()) {
                if (cfg.getConfiguration() != null) {
                    cfg.setState(ItemStateEnum.DRAFT_NOT_EXIST);
                }
            } else {
                if (cfg.getConfiguration() != null) {
                    if (lastDeployment == null) {
                        cfg.setState(ItemStateEnum.DEPLOYMENT_NOT_EXIST);
                    } else {
                        if (lastDeployment.getDeploymentDate().after(dbItem.getModified())) {
                            cfg.setState(ItemStateEnum.DEPLOYMENT_IS_NEWER);
                        } else {
                            cfg.setState(ItemStateEnum.DRAFT_IS_NEWER);
                        }
                    }
                } 
            }
            // JOC-1498 - IsReferencedBy
            if (ConfigurationType.WORKFLOW.equals(type)) {
                IsReferencedBy isRef = new IsReferencedBy();
                isRef.setAdditionalProperty("fileOrderSources", dbLayer.getNumOfUsedFileOrderSourcesByWorkflowName(dbItem.getName()).intValue());
                isRef.setAdditionalProperty("schedules", dbLayer.getNumOfUsedSchedulesByWorkflowName(dbItem.getName()).intValue());
                isRef.setAdditionalProperty("workflows", dbLayer.getNumOfAddOrderWorkflowsByWorkflowName(dbItem.getName()).intValue());
                cfg.setIsReferencedBy(isRef);
            }
        } else if (JocInventory.isReleasable(type)) {
            cfg.setDeployed(false);
            List<Date> releasedModifieds = dbLayer.getReleasedItemPropertyByConfigurationId(dbItem.getId(), "modified");
            Date releasedLastModified = null;
            if (releasedModifieds != null && !releasedModifieds.isEmpty()) {
                releasedLastModified = releasedModifieds.get(0);
                cfg.setHasReleases(true);
            }
            if (dbItem.getReleased()) {
                if (cfg.getConfiguration() != null) {
                    cfg.setState(ItemStateEnum.DRAFT_NOT_EXIST);
                }
            } else {
                if (cfg.getConfiguration() != null) {
                    if (releasedLastModified == null) {
                        cfg.setState(ItemStateEnum.RELEASE_NOT_EXIST);
                    } else {
                        if (releasedLastModified.after(dbItem.getModified())) {
                            cfg.setState(ItemStateEnum.RELEASE_IS_NEWER);
                        } else {
                            cfg.setState(ItemStateEnum.DRAFT_IS_NEWER);
                        }
                    }
                }
            }
        }
        return cfg;
    }
    
    public static void makeParentDirs(InventoryDBLayer dbLayer, Path parentFolder, Long auditLogId, ConfigurationType folderType) throws SOSHibernateException {
        if (parentFolder != null) {
            String newFolder = parentFolder.toString().replace('\\', '/');
            if (!ROOT_FOLDER.equals(newFolder)) {
                DBItemInventoryConfiguration newDbFolder = dbLayer.getConfiguration(newFolder, folderType.intValue());
                if (newDbFolder == null) {
                    newDbFolder = new DBItemInventoryConfiguration();
                    newDbFolder.setPath(newFolder);
                    if(parentFolder.getParent() != null) {
                        newDbFolder.setFolder(parentFolder.getParent().toString().replace('\\', '/'));
                    } else {
                        newDbFolder.setFolder(newFolder);
                    }
                    newDbFolder.setName(parentFolder.getFileName().toString());
                    newDbFolder.setDeployed(false);
                    newDbFolder.setReleased(false);
                    newDbFolder.setModified(Date.from(Instant.now()));
                    newDbFolder.setAuditLogId(auditLogId == null ? 0L : auditLogId);
                    newDbFolder.setContent(null);
                    newDbFolder.setCreated(newDbFolder.getModified());
                    newDbFolder.setDeleted(false);
                    newDbFolder.setId(null);
                    newDbFolder.setTitle(null);
                    newDbFolder.setType(folderType);
                    newDbFolder.setValid(true);
                    dbLayer.getSession().save(newDbFolder);
                    if(parentFolder.getParent() != null ) {
                        makeParentDirs(dbLayer, parentFolder.getParent(), auditLogId, folderType);
                    }
                }
            }
        }
    }

    public static void makeParentDirs(InventoryDBLayer dbLayer, Path folder, ConfigurationType folderType) throws SOSHibernateException {
        makeParentDirs(dbLayer, folder, null, folderType);
    }

    public static void makeParentDirsForTrash(InventoryDBLayer dbLayer, Path folder, ConfigurationType folderType) throws SOSHibernateException {
        makeParentDirsForTrash(dbLayer, folder, null, folderType);
    }

    public static void makeParentDirsForTrash(InventoryDBLayer dbLayer, Path parentFolder, Long auditLogId, ConfigurationType folderType)
            throws SOSHibernateException {
        if (parentFolder != null) {
            String newFolder = parentFolder.toString().replace('\\', '/');
            if (!ROOT_FOLDER.equals(newFolder)) {
                DBItemInventoryConfigurationTrash newDbFolder = dbLayer.getTrashConfiguration(newFolder, folderType.intValue());
                if (newDbFolder == null) {
                    newDbFolder = new DBItemInventoryConfigurationTrash();
                    newDbFolder.setPath(newFolder);
                    newDbFolder.setFolder(parentFolder.getParent().toString().replace('\\', '/'));
                    newDbFolder.setName(parentFolder.getFileName().toString());
                    newDbFolder.setModified(Date.from(Instant.now()));
                    newDbFolder.setAuditLogId(auditLogId == null ? 0L : auditLogId);
                    newDbFolder.setContent(null);
                    newDbFolder.setCreated(newDbFolder.getModified());
                    newDbFolder.setId(null);
                    newDbFolder.setTitle(null);
                    newDbFolder.setType(folderType);
                    newDbFolder.setValid(true);
                    dbLayer.getSession().save(newDbFolder);
                    makeParentDirsForTrash(dbLayer, parentFolder.getParent(), auditLogId, folderType);
                }
            }
        }
    }

    public static String pathToName(String path) {
        if (!path.contains("/")) {
            return path;
        }
        if (ROOT_FOLDER.equals(path)) {
            return "";
        }
        return Paths.get(path).getFileName().toString();
    }

    public static List<DBItemInventoryConfiguration> deleteEmptyFolders(InventoryDBLayer dbLayer, DBItemInventoryConfiguration folder)
            throws SOSHibernateException {
        return deleteEmptyFolders(dbLayer, folder, false);
    }
    
    public static List<DBItemInventoryConfiguration> deleteEmptyFolders(InventoryDBLayer dbLayer, DBItemInventoryConfiguration folder, 
            boolean forDescriptors) throws SOSHibernateException {
        List<DBItemInventoryConfiguration> folderContent = dbLayer.getFolderContent(folder.getPath(), true, null, forDescriptors);
        if (folderContent == null) {
            folderContent = new ArrayList<DBItemInventoryConfiguration>();
        }
        if (!ROOT_FOLDER.equals(folder.getPath())) {
            folderContent.add(folder);
        }
        return deleteEmptyFolders(dbLayer.getSession(), folderContent, forDescriptors);
    }

    public static List<DBItemInventoryConfiguration> deleteEmptyFolders(InventoryDBLayer dbLayer, String folder) throws SOSHibernateException {
        return deleteEmptyFolders(dbLayer, folder, false);
    }
    
    public static List<DBItemInventoryConfiguration> deleteEmptyFolders(InventoryDBLayer dbLayer, String folder, boolean forDescriptors)
            throws SOSHibernateException {
        List<DBItemInventoryConfiguration> folderContent = dbLayer.getFolderContent(folder, true, null, forDescriptors);
        if (folderContent == null) {
            folderContent = new ArrayList<DBItemInventoryConfiguration>();
        }
        if (!ROOT_FOLDER.equals(folder)) {
            DBItemInventoryConfiguration dbFolder = null;
            if(forDescriptors) {
                dbFolder = dbLayer.getConfiguration(folder, ConfigurationType.DESCRIPTORFOLDER.intValue());
            } else {
                dbFolder = dbLayer.getConfiguration(folder, ConfigurationType.FOLDER.intValue());
            }
            if (dbFolder != null) {
                folderContent.add(dbFolder);
            }
        }
        return deleteEmptyFolders(dbLayer.getSession(), folderContent, forDescriptors);
    }

    private static List<DBItemInventoryConfiguration> deleteEmptyFolders(SOSHibernateSession session,
            List<DBItemInventoryConfiguration> folderContent, boolean forDescriptors) throws SOSHibernateException {
        List<DBItemInventoryConfiguration> deletedFolders = new ArrayList<>();
        if (!folderContent.isEmpty()) {
            LinkedHashSet<DBItemInventoryConfiguration> folders = null;
            if(forDescriptors) {
                folders = folderContent.stream().filter(i -> ConfigurationType.DESCRIPTORFOLDER.intValue() == i
                        .getType()).sorted(Comparator.comparing(DBItemInventoryConfiguration::getPath).reversed())
                        .collect(Collectors.toCollection(LinkedHashSet::new));
            } else {
                folders = folderContent.stream().filter(i -> ConfigurationType.FOLDER.intValue() == i
                        .getType()).sorted(Comparator.comparing(DBItemInventoryConfiguration::getPath).reversed())
                        .collect(Collectors.toCollection(LinkedHashSet::new));
            }
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

    public static DBItemJocAuditLog storeAuditLog(JocAuditLog auditLog, AuditParams auditParams) {
        return storeAuditLog(auditLog, auditParams, null);
    }

    public static DBItemJocAuditLog storeAuditLog(JocAuditLog auditLog, AuditParams auditParams, Collection<AuditLogDetail> details) {
        if (ClusterSettings.getForceCommentsForAuditLog(Globals.getConfigurationGlobalsJoc())) {
            String comment = null;
            if (auditParams != null) {
                comment = auditParams.getComment();
            }
            if (comment == null || comment.isEmpty()) {
                throw new JocMissingCommentException();
            }
        }
        if (auditLog != null) {
            DBItemJocAuditLog auditItem = auditLog.storeAuditLogEntry(auditParams);
            auditLog.logAuditMessage(auditParams, auditItem.getId());
            if (details != null) {
                JocAuditLog.storeAuditLogDetails(details, auditItem.getId(), auditItem.getCreated());
            }
            return auditItem;
        } else {
            DBItemJocAuditLog auditItem = new DBItemJocAuditLog();
            auditItem.setId(0L);
            auditItem.setCreated(Date.from(Instant.now()));
            return auditItem;
        }
    }

    public static void postEvent(String folder) {
        EventBus.getInstance().post(new InventoryEvent(folder));
    }
    
    public static void postFolderEvent(String folder) {
        EventBus.getInstance().post(new InventoryFolderEvent(folder));
    }

    public static void postTrashEvent(String folder) {
        EventBus.getInstance().post(new InventoryTrashEvent(folder));
    }

    public static void postTrashFolderEvent(String folder) {
        EventBus.getInstance().post(new InventoryTrashFolderEvent(folder));
    }
    
    public static void postObjectEvent(String path, ConfigurationType objectType) {
        EventBus.getInstance().post(new InventoryObjectEvent(path, objectType.value()));
    }
    
    public static void postTaggingEvent(String tag) {
        EventBus.getInstance().post(new InventoryTagEvent(tag));
    }
    
    public static void postJobTaggingEvent(String tag) {
        EventBus.getInstance().post(new InventoryJobTagEvent(tag));
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
                SOSCheckJavaVariableName.test(type.value().toLowerCase(), name);
                folder = normalizeFolder(p.getParent());
            }
        }

        public InventoryPath(final java.nio.file.Path inventoryPath, ConfigurationType type) {
            if (inventoryPath != null) {
                path = inventoryPath.toString().replace('\\', '/');
                name = inventoryPath.getFileName().toString();
                SOSCheckJavaVariableName.test(type.value().toLowerCase(), name);
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
            SOSAuthFolderPermissions folderPermissions) throws Exception {
        return getConfiguration(dbLayer, in.getId(), in.getPath(), in.getObjectType(), folderPermissions);
    }

    public static DBItemInventoryConfiguration getConfiguration(InventoryDBLayer dbLayer, Long id, String path, ConfigurationType type,
            SOSAuthFolderPermissions folderPermissions) throws Exception {
        return getConfiguration(dbLayer, id, path, type, folderPermissions, false);
    }

    public static DBItemInventoryConfiguration getConfiguration(InventoryDBLayer dbLayer, Long id, String path, ConfigurationType type,
            SOSAuthFolderPermissions folderPermissions, boolean withIsNotPermittedParentFolder) throws Exception {
        DBItemInventoryConfiguration config = null;
        String name = null;

        if (id != null) {
            config = dbLayer.getConfiguration(id);
            if (config == null) {
                throw new DBMissingDataException(String.format("Couldn't find the configuration: %s", id));
            }
            if (isFolder(config.getType())) {
                boolean isPermittedForFolder = folderPermissions.isPermittedForFolder(config.getPath());
                if (!isPermittedForFolder && !isNotPermittedParentFolder(folderPermissions, config.getPath(), withIsNotPermittedParentFolder)) {
                    throw new JocFolderPermissionsException("Access denied for folder: " + config.getPath());
                }
            } else if (!folderPermissions.isPermittedForFolder(config.getFolder())) {
                throw new JocFolderPermissionsException("Access denied for folder: " + config.getFolder());
            }
            // temp. because of rename error on root folder
            config.setPath(config.getPath().replace("//+", "/"));
        } else {
            if (!isFolder(type) && path != null && !path.contains("/")) {
                name = path;
                path = null;
            }
            if (path != null) {
                if (JocInventory.ROOT_FOLDER.equals(path) && ConfigurationType.FOLDER.equals(type)) {
                    config = new DBItemInventoryConfiguration();
                    config.setId(0L);
                    config.setPath(path);
                    config.setName("");
                    config.setType(type);
                    config.setFolder(path);
                    config.setDeleted(false);
                    config.setValid(true);
                    config.setDeployed(false);
                    config.setReleased(false);
                } else {
                    Path p = normalizePath(path);
                    path = p.toString().replace('\\', '/');
                    
                    if (isFolder(type)) {
                        boolean isPermittedForFolder = folderPermissions.isPermittedForFolder(path);
                        if (!isPermittedForFolder && !isNotPermittedParentFolder(folderPermissions, path, withIsNotPermittedParentFolder)) {
                            throw new JocFolderPermissionsException("Access denied for folder: " + path);
                        }
                    } else if (ROOT_FOLDER.equals(p.toString().replace('\\', '/'))) {
                        throw new JocBadRequestException(String.format("Invalid object name '%1$s'.", p.toString().replace('\\', '/')));
                    } else if (!folderPermissions.isPermittedForFolder(p.getParent().toString().replace('\\', '/'))) {
                        throw new JocFolderPermissionsException("Access denied for folder: " + p.getParent().toString().replace('\\', '/'));
                    }
                    config = dbLayer.getConfiguration(path, type.intValue());
                    if (config == null) {
                        throw new DBMissingDataException(String.format("Couldn't find the %s: %s", type.value().toLowerCase(), path));
                    }
                }
            } else if (name != null) {// name
                List<DBItemInventoryConfiguration> configs = dbLayer.getConfigurationByName(name, type.intValue());
                if (configs == null || configs.isEmpty()) {
                    throw new DBMissingDataException(String.format("Couldn't find the %s: %s", type.value().toLowerCase(), name));
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

    private static boolean isNotPermittedParentFolder(SOSAuthFolderPermissions folderPermissions, String path,
            boolean withIsNotPermittedParentFolder) {
        if (!withIsNotPermittedParentFolder) {
            return true;
        }
        Set<String> notPermittedParentFolders = folderPermissions.getNotPermittedParentFolders().getOrDefault("", Collections.emptySet());
        return notPermittedParentFolders.contains(path);
    }

    public static DBItemInventoryConfigurationTrash getTrashConfiguration(InventoryDBLayer dbLayer, RequestFilter in,
            SOSAuthFolderPermissions folderPermissions) throws Exception {
        return getTrashConfiguration(dbLayer, in.getId(), in.getPath(), in.getObjectType(), folderPermissions);
    }

    public static DBItemInventoryConfigurationTrash getTrashConfiguration(InventoryDBLayer dbLayer, Long id, String path, ConfigurationType type,
            SOSAuthFolderPermissions folderPermissions) throws Exception {
        DBItemInventoryConfigurationTrash config = null;
        if (id != null) {
            config = dbLayer.getTrashConfiguration(id);
            if (config == null) {
                throw new DBMissingDataException(String.format("Couldn't find the configuration: %s", id));
            }
            if (!folderPermissions.isPermittedForFolder(config.getFolder())) {
                throw new JocFolderPermissionsException("Access denied for folder: " + config.getFolder());
            }
        } else if (path != null) {
            if (JocInventory.ROOT_FOLDER.equals(path) && (ConfigurationType.FOLDER.equals(type) || ConfigurationType.DESCRIPTORFOLDER.equals(type))) {
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
                    throw new DBMissingDataException(String.format("Couldn't find the %s: %s", type.value().toLowerCase(), path));
                }
            }
        }
        return config;
    }

    public static Path normalizePath(String path) {
        return Paths.get(JocInventory.ROOT_FOLDER).resolve(path).normalize();
    }

    public static String toString(IConfigurationObject config) throws JsonProcessingException {
        return config == null ? "null" : JsonSerializer.serializeAsString(config);
    }

    public static String hash(IConfigurationObject config) throws JsonProcessingException {
        return SOSString.hash256(toString(config));
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
            handleWorkflowSearch(dbLayer, (Workflow) config, item.getId());
        }
    }
    
    private static void handleReleasedJobTemplate(InventoryDBLayer dbLayer, DBItemInventoryConfiguration item, IConfigurationObject config)
            throws SOSHibernateException {
        if (ConfigurationType.JOBTEMPLATE.intValue().equals(item.getType())) {
            // Only released job templates are assigned to jobs.
            // If job template is renamed or moved then the assignment is missing
            DBItemInventoryReleasedConfiguration releasedItem = dbLayer.getReleasedConfigurationByInvId(item.getId());
            if (releasedItem != null && ConfigurationType.JOBTEMPLATE.intValue().equals(item.getType()) && !item.getPath().equals(releasedItem
                    .getPath())) {
                releasedItem.setFolder(item.getFolder());
                releasedItem.setName(item.getName());
                releasedItem.setPath(item.getPath());
                dbLayer.getSession().update(releasedItem);
            }
        }
    }
    
    private static void handleTags(SOSHibernateSession session, DBItemInventoryConfiguration item, IConfigurationObject config)
            throws JsonMappingException, JsonProcessingException {
        if (ConfigurationType.WORKFLOW.intValue().equals(item.getType())) {
            if (config == null && !SOSString.isEmpty(item.getContent())) {
                config = workflowContent2Workflow(item.getContent());
            }
            JobTags.update(((Workflow) config).getJobs(), item, new InventoryJobTagDBLayer(session));
            OrderTags.updateTagsFromInstructions((Workflow) config, item);
            
        } else if (ConfigurationType.FILEORDERSOURCE.intValue().equals(item.getType())) {
            if (config == null && !SOSString.isEmpty(item.getContent())) {
                config = convertFileOrderSource(item.getContent(), FileOrderSource.class);
            }
            OrderTags.updateTagsFromFileOrderSource((FileOrderSource) config, item);
            
        } else if (ConfigurationType.SCHEDULE.intValue().equals(item.getType())) {
            if (config == null && !SOSString.isEmpty(item.getContent())) {
                config = convertSchedule(item.getContent(), Schedule.class);
            }
            OrderTags.updateTagsFromOrderPreparation((Schedule) config, item);
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
            item.setArgs(null);
            item.setJobsScripts(null);
            item.setInstructions(null);
            item.setInstructionsArgs(null);
        } else {
            WorkflowConverter converter = new WorkflowConverter();
            converter.process(workflow);

            item.setJobsCount(converter.getJobs().getNames().size());
            item.setJobs(converter.getJobs().getMainInfo().toString());
            item.setArgs(converter.getArgInfo().toString());
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
        handleTags(dbLayer.getSession(), item, config);
        
        dbLayer.getSession().update(item);

        handleWorkflowSearch(dbLayer, item, config);
        handleReleasedJobTemplate(dbLayer, item, config);
    }

    public static void insertConfiguration(InventoryDBLayer dbLayer, DBItemInventoryConfiguration item) throws SOSHibernateException,
            JsonParseException, JsonMappingException, JsonProcessingException, IOException {
        insertConfiguration(dbLayer, item, null);
    }

    public static void insertConfiguration(InventoryDBLayer dbLayer, DBItemInventoryConfiguration item, IConfigurationObject config)
            throws SOSHibernateException, JsonParseException, JsonMappingException, JsonProcessingException, IOException {
        handleTags(dbLayer.getSession(), item, config);
        
        dbLayer.getSession().save(item);
        
        handleWorkflowSearch(dbLayer, item, config);
    }

    public static void insertOrUpdateConfiguration(InventoryDBLayer dbLayer, DBItemInventoryConfiguration item) throws SOSHibernateException,
            JsonParseException, JsonMappingException, JsonProcessingException, IOException {
        insertOrUpdateConfiguration(dbLayer, item, null);
    }

    public static void insertOrUpdateConfiguration(InventoryDBLayer dbLayer, DBItemInventoryConfiguration item, IConfigurationObject config)
            throws SOSHibernateException, JsonParseException, JsonMappingException, JsonProcessingException, IOException {
        DBItemInventoryConfiguration alreadyExists = dbLayer.getConfiguration(item.getPath(), item.getType());
        if (alreadyExists != null) {
            updateConfiguration(dbLayer, item, config);
        } else {
            insertConfiguration(dbLayer, item, config);
        }
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

    public static void deleteInventoryConfigurationAndPutToTrash(DBItemInventoryConfiguration item, InventoryDBLayer dbLayer, ConfigurationType folderType) {
        if (item != null) {
            try {
                List<DBItemInventoryConfigurationTrash> trashItems = dbLayer.getTrashConfigurationByName(item.getName(), item.getType());
                deleteConfiguration(dbLayer, item);
                Date now = Date.from(Instant.now());
                boolean createParentFolder = true;
                DBItemInventoryConfigurationTrash trashItem = null;
                if (trashItems.isEmpty()) {
                    trashItem = new DBItemInventoryConfigurationTrash();
                    trashItem.setId(null);
                    trashItem.setCreated(now);
                    trashItem.setType(item.getType());
                    trashItem.setName(item.getName());
                } else {
                    trashItem = trashItems.remove(0);
                    createParentFolder = !trashItem.getPath().equals(item.getPath());
                    for (DBItemInventoryConfigurationTrash tItem : trashItems) {
                        dbLayer.getSession().delete(tItem);
                    }
                }
                if (createParentFolder) {
                    trashItem.setPath(item.getPath());
                    trashItem.setFolder(item.getFolder());
                }
                trashItem.setAuditLogId(item.getAuditLogId());
                trashItem.setContent(item.getContent());
                trashItem.setTitle(item.getTitle());
                trashItem.setValid(item.getValid());
                trashItem.setModified(now);
                if (trashItem.getId() == null) {
                    dbLayer.getSession().save(trashItem);
                    makeParentDirsForTrash(dbLayer, Paths.get(trashItem.getFolder()), trashItem.getAuditLogId(), folderType);
                } else {
                    dbLayer.getSession().update(trashItem);
                    if (createParentFolder) {
                        makeParentDirsForTrash(dbLayer, Paths.get(trashItem.getFolder()), trashItem.getAuditLogId(), folderType);
                    }
                }
            } catch (SOSHibernateInvalidSessionException ex) {
                throw new DBConnectionRefusedException(ex);
            } catch (Exception ex) {
                throw new DBInvalidDataException(ex);
            }
        }
    }

    public static Set<String> deepCopy(DBItemInventoryConfiguration config, String newName, InventoryDBLayer dbLayer) throws JsonParseException,
            JsonMappingException, SOSHibernateException, JsonProcessingException, IOException {
        return deepCopy(config, newName, Collections.emptyList(), dbLayer);
    }

    public static Set<String> deepCopy(DBItemInventoryConfiguration config, String newName, List<DBItemInventoryConfiguration> items,
            InventoryDBLayer dbLayer) throws JsonParseException, JsonMappingException, SOSHibernateException, JsonProcessingException,
                IOException {
        Set<String> events = new HashSet<>();
        if (config.getName().equals(newName)) {
            return events;
        }
        Predicate<String> workflowNamePredicate = Pattern.compile("\"workflowName\"\\s*:\\s*\"" + config.getName() + "\"").asPredicate();
        switch (config.getTypeAsEnum()) {
        case LOCK: // determine Workflows with Lock instructions
            List<DBItemInventoryConfiguration> workflows = dbLayer.getUsedWorkflowsByLockId(config.getName());
            if (workflows != null && !workflows.isEmpty()) {
                Predicate<String> lockNamePredicate = Pattern.compile("\"lockName\"\\s*:\\s*\"" + config.getName() + "\"").asPredicate();
                for (DBItemInventoryConfiguration workflow : workflows) {
                    boolean changed = lockNamePredicate.test(workflow.getContent());
                    if (changed) {
                        workflow.setContent(workflow.getContent().replaceAll("(\"lockName\"\\s*:\\s*\")" + config.getName() + "\"", "$1" + newName
                                + "\""));
                        workflow.setDeployed(false);
                        int i = items.indexOf(workflow);
                        if (i != -1) {
                            items.get(i).setContent(workflow.getContent());
                            items.get(i).setDeployed(false);
                        } else {
                            JocInventory.updateConfiguration(dbLayer, workflow);
                            events.add(workflow.getFolder());
                        }
                    }
                }
            }
            break;
        case JOBRESOURCE: // determine Workflows and JobTemplates with Jobs containing JobResource
            List<DBItemInventoryConfiguration> workflows2 = dbLayer.getUsedWorkflowsByJobResource(config.getName());
            if (workflows2 != null && !workflows2.isEmpty()) {
                for (DBItemInventoryConfiguration workflow : workflows2) {
                    boolean changed = false;
                    Workflow w = Globals.objectMapper.readValue(workflow.getContent(), Workflow.class);
                    // JobResources on Workflow level
                    if (w.getJobResourceNames() != null && w.getJobResourceNames().contains(config.getName())) {
                        for (int i = 0; i < w.getJobResourceNames().size(); i++) {
                            if (w.getJobResourceNames().get(i).equals(config.getName())) {
                                w.getJobResourceNames().set(i, newName);
                                changed = true;
                            }
                        }
                    }
                    // JobResources on Job level
                    if (w.getJobs() != null) {
                        for (Map.Entry<String, Job> entry : w.getJobs().getAdditionalProperties().entrySet()) {
                            Job j = entry.getValue();
                            if (j.getJobResourceNames() != null && j.getJobResourceNames().contains(config.getName())) {
                                for (int i = 0; i < j.getJobResourceNames().size(); i++) {
                                    if (j.getJobResourceNames().get(i).equals(config.getName())) {
                                        j.getJobResourceNames().set(i, newName);
                                        changed = true;
                                    }
                                }
                            }
                        }
                    }
                    if (changed) {
                        workflow.setContent(Globals.objectMapper.writeValueAsString(w));
                        workflow.setDeployed(false);
                        int i = items.indexOf(workflow);
                        if (i != -1) {
                            items.get(i).setContent(workflow.getContent());
                            items.get(i).setDeployed(false);
                        } else {
                            JocInventory.updateConfiguration(dbLayer, workflow);
                            events.add(workflow.getFolder());
                        }
                    }
                }
            }
            List<DBItemInventoryConfiguration> jobTemplates = dbLayer.getUsedJobTemplatesByJobResource(config.getName());
            if (jobTemplates != null && !jobTemplates.isEmpty()) {
                for (DBItemInventoryConfiguration jobTemplate : jobTemplates) {
                    boolean changed = false;
                    JobTemplate jt = Globals.objectMapper.readValue(jobTemplate.getContent(), JobTemplate.class);
                    if (jt.getJobResourceNames() != null && jt.getJobResourceNames().contains(config.getName())) {
                        for (int i = 0; i < jt.getJobResourceNames().size(); i++) {
                            if (jt.getJobResourceNames().get(i).equals(config.getName())) {
                                jt.getJobResourceNames().set(i, newName);
                                changed = true;
                            }
                        }
                        if (changed) {
                            jobTemplate.setContent(Globals.objectMapper.writeValueAsString(jt));
                            jobTemplate.setReleased(false);
                            int i = items.indexOf(jobTemplate);
                            if (i != -1) {
                                items.get(i).setContent(jobTemplate.getContent());
                                items.get(i).setReleased(false);
                            } else {
                                JocInventory.updateConfiguration(dbLayer, jobTemplate);
                                events.add(jobTemplate.getFolder());
                            }
                        }
                    }
                }
            }
            break;

        case NOTICEBOARD: // determine Workflows with PostNotice or ExpectNotice reference
            List<DBItemInventoryConfiguration> workflow3 = dbLayer.getUsedWorkflowsByBoardName(config.getName());
            if (workflow3 != null && !workflow3.isEmpty()) {
                for (DBItemInventoryConfiguration workflow : workflow3) {
                    Workflow w = Globals.objectMapper.readValue(workflow.getContent(), Workflow.class);
                    // TODO updateWorkflowBoardname should return true iff something changed
                    WorkflowsHelper.updateWorkflowBoardname(Collections.singletonMap(config.getName(), newName), w.getInstructions());
                    workflow.setContent(Globals.objectMapper.writeValueAsString(w));
                    workflow.setDeployed(false);
                    int i = items.indexOf(workflow);
                    if (i != -1) {
                        items.get(i).setContent(workflow.getContent());
                        items.get(i).setDeployed(false);
                    } else {
                        JocInventory.updateConfiguration(dbLayer, workflow);
                        events.add(workflow.getFolder());
                    }
                }
            }
            break;

        case WORKFLOW: // determine Schedules and FileOrderSources with Workflow reference
            List<DBItemInventoryConfiguration> schedules = dbLayer.getUsedSchedulesByWorkflowName(config.getName());
            if (schedules != null && !schedules.isEmpty()) {
                for (DBItemInventoryConfiguration schedule : schedules) {
                    boolean changed = false;
                    Schedule s = Globals.objectMapper.readValue(schedule.getContent(), Schedule.class);

                    if (s.getWorkflowNames() == null) {
                        s.setWorkflowNames(new ArrayList<>());
                    }
                    if (SCHEDULE_CONSIDER_WORKFLOW_NAME) {
                        if (s.getWorkflowName() != null) {
                            if (s.getWorkflowNames().size() == 0) {
                                s.getWorkflowNames().add(s.getWorkflowName());
                            }
                        }
                    }

                    List<String> wn = new ArrayList<>();
                    for (String w : s.getWorkflowNames()) {
                        if (w.equals(config.getName())) {
                            wn.add(newName);
                            changed = true;
                        } else {
                            wn.add(w);
                        }
                    }
                    s.setWorkflowNames(wn);

                    // tmp
                    if (s.getWorkflowNames().size() > 0) {
                        s.setWorkflowName(s.getWorkflowNames().get(0));
                    }
                    
                    if (changed) {
                        schedule.setContent(Globals.objectMapper.writeValueAsString(s));
                        schedule.setReleased(false);
                        int i = items.indexOf(schedule);
                        if (i != -1) {
                            items.get(i).setContent(schedule.getContent());
                            items.get(i).setReleased(false);
                        } else {
                            JocInventory.updateConfiguration(dbLayer, schedule);
                            events.add(schedule.getFolder());
                        }
                    }
                }
            }
            List<DBItemInventoryConfiguration> fileOrderSources = dbLayer.getUsedFileOrderSourcesByWorkflowName(config.getName());
            if (fileOrderSources != null && !fileOrderSources.isEmpty()) {
                for (DBItemInventoryConfiguration fileOrderSource : fileOrderSources) {
                    boolean changed = workflowNamePredicate.test(fileOrderSource.getContent());
                    if (changed) {
                        fileOrderSource.setContent(fileOrderSource.getContent().replaceAll("(\"workflowName\"\\s*:\\s*\")" + config.getName() + "\"",
                                "$1" + newName + "\""));
                        fileOrderSource.setDeployed(false);
                        int i = items.indexOf(fileOrderSource);
                        if (i != -1) {
                            items.get(i).setContent(fileOrderSource.getContent());
                            items.get(i).setDeployed(false);
                        } else {
                            JocInventory.updateConfiguration(dbLayer, fileOrderSource);
                            events.add(fileOrderSource.getFolder());
                        }
                    }
                }
            }
            List<DBItemInventoryConfiguration> addOrderWorkflows = dbLayer.getUsedWorkflowsByAddOrdersWorkflowName(config.getName());
            if (addOrderWorkflows != null && !addOrderWorkflows.isEmpty()) {
                for (DBItemInventoryConfiguration addOrderWorkflow : addOrderWorkflows) {
                    boolean changed = workflowNamePredicate.test(addOrderWorkflow.getContent());
                    if (changed) {
                        addOrderWorkflow.setContent(addOrderWorkflow.getContent().replaceAll("(\"workflowName\"\\s*:\\s*\")" + config.getName()
                                + "\"", "$1" + newName + "\""));
                        addOrderWorkflow.setDeployed(false);
                        int i = items.indexOf(addOrderWorkflow);
                        if (i != -1) {
                            items.get(i).setContent(addOrderWorkflow.getContent());
                            items.get(i).setDeployed(false);
                        } else {
                            JocInventory.updateConfiguration(dbLayer, addOrderWorkflow);
                            events.add(addOrderWorkflow.getFolder());
                        }
                    }
                }
            }
            break;
        case WORKINGDAYSCALENDAR: // determine Schedules with Calendar reference
        case NONWORKINGDAYSCALENDAR:
            List<DBItemInventoryConfiguration> schedules1 = dbLayer.getUsedSchedulesByCalendarName(config.getName());
            if (schedules1 != null && !schedules1.isEmpty()) {
                Predicate<String> calendarNamePredicate = Pattern.compile("\"calendarName\"\\s*:\\s*\"" + config.getName() + "\"").asPredicate();
                for (DBItemInventoryConfiguration schedule : schedules1) {
                    boolean changed = calendarNamePredicate.test(schedule.getContent());
                    if (changed) {
                        schedule.setContent(schedule.getContent().replaceAll("(\"calendarName\"\\s*:\\s*\")" + config.getName() + "\"", "$1" + newName
                                + "\""));
                        schedule.setReleased(false);
                        int i = items.indexOf(schedule);
                        if (i != -1) {
                            items.get(i).setContent(schedule.getContent());
                            items.get(i).setReleased(false);
                        } else {
                            JocInventory.updateConfiguration(dbLayer, schedule);
                            events.add(schedule.getFolder());
                        }
                    }
                }
            }
            break;
        case INCLUDESCRIPT: // determine Workflows with script reference in INCLUDE line of a job script
            List<DBItemInventoryConfiguration> workflowsOrJobTemplates = dbLayer.getWorkflowsAndJobTemplatesWithIncludedScripts();
            Predicate<String> hasScriptInclude = Pattern.compile(JsonConverter.scriptIncludeComments + JsonConverter.scriptInclude + "[ \t]+" + config
                    .getName() + "\\s*").asPredicate();
            if (workflowsOrJobTemplates != null && !workflowsOrJobTemplates.isEmpty()) {
                for (DBItemInventoryConfiguration workflowOrJobTemplate : workflowsOrJobTemplates) {
                    if (hasScriptInclude.test(workflowOrJobTemplate.getContent())) {
                        if (ConfigurationType.WORKFLOW.equals(workflowOrJobTemplate.getTypeAsEnum())) {
                            Workflow w = Globals.objectMapper.readValue(workflowOrJobTemplate.getContent(), Workflow.class);
                            if (w.getJobs() != null) {
                                Map<String, Job> replacedJobs = new HashMap<>();
                                w.getJobs().getAdditionalProperties().forEach((jobName, job) -> {
                                    if (job.getExecutable() != null && ExecutableType.ShellScriptExecutable.equals(job.getExecutable().getTYPE())) {
                                        ExecutableScript es = job.getExecutable().cast();
                                        if (es.getScript() != null && hasScriptInclude.test(es.getScript())) {
                                            String[] scriptLines = es.getScript().split("\n");
                                            for (int i = 0; i < scriptLines.length; i++) {
                                                String line = scriptLines[i];
                                                if (hasScriptInclude.test(line)) {
                                                    Matcher m = JsonConverter.scriptIncludePattern.matcher(line);
                                                    if (m.find()) {
                                                        if (config.getName().equals(m.group(2))) {
                                                            scriptLines[i] = m.group(1) + JsonConverter.scriptInclude + " " + newName + " " + m.group(
                                                                    3);
                                                        }
                                                    }
                                                }
                                            }
                                            es.setScript(String.join("\n", scriptLines));
                                            replacedJobs.put(jobName, job);
                                        }
                                    }
                                });
                                replacedJobs.forEach((jobName, job) -> w.getJobs().setAdditionalProperty(jobName, job));
                                workflowOrJobTemplate.setContent(Globals.objectMapper.writeValueAsString(w));
                                workflowOrJobTemplate.setDeployed(false);
                            }
                        } else if (ConfigurationType.JOBTEMPLATE.equals(workflowOrJobTemplate.getTypeAsEnum())) {
                            JobTemplate jt = Globals.objectMapper.readValue(workflowOrJobTemplate.getContent(), JobTemplate.class);
                            if (jt.getExecutable() != null && ExecutableType.ShellScriptExecutable.equals(jt.getExecutable().getTYPE())) {
                                ExecutableScript es = jt.getExecutable().cast();
                                if (es.getScript() != null && hasScriptInclude.test(es.getScript())) {
                                    String[] scriptLines = es.getScript().split("\n");
                                    for (int i = 0; i < scriptLines.length; i++) {
                                        String line = scriptLines[i];
                                        if (hasScriptInclude.test(line)) {
                                            Matcher m = JsonConverter.scriptIncludePattern.matcher(line);
                                            if (m.find()) {
                                                if (config.getName().equals(m.group(2))) {
                                                    scriptLines[i] = m.group(1) + JsonConverter.scriptInclude + " " + newName + " " + m.group(
                                                            3);
                                                }
                                            }
                                        }
                                    }
                                    es.setScript(String.join("\n", scriptLines));
                                    workflowOrJobTemplate.setContent(Globals.objectMapper.writeValueAsString(jt));
                                    workflowOrJobTemplate.setReleased(false);
                                }
                            }
                        }
                        int i = items.indexOf(workflowOrJobTemplate);
                        if (i != -1) {
                            items.get(i).setContent(workflowOrJobTemplate.getContent());
                            if (ConfigurationType.WORKFLOW.equals(workflowOrJobTemplate.getTypeAsEnum())) {
                                items.get(i).setDeployed(false);
                            } else if (ConfigurationType.JOBTEMPLATE.equals(workflowOrJobTemplate.getTypeAsEnum())) {
                                items.get(i).setReleased(false);
                            }
                        } else {
                            JocInventory.updateConfiguration(dbLayer, workflowOrJobTemplate);
                            events.add(workflowOrJobTemplate.getFolder());
                        }
                    }
                }
            }
            break;
            
        case JOBTEMPLATE:
            List<DBItemInventoryConfiguration> workflows5 = dbLayer.getUsedWorkflowsByJobTemplateName(config.getName());
            if (workflows5 != null && !workflows5.isEmpty()) {
                for (DBItemInventoryConfiguration workflow : workflows5) {
                    boolean changed = false;
                    Workflow w = Globals.objectMapper.readValue(workflow.getContent(), Workflow.class);
                    if (w.getJobs() != null) {
                        for (Map.Entry<String, Job> entry : w.getJobs().getAdditionalProperties().entrySet()) {
                            Job j = entry.getValue();
                            if (j.getJobTemplate() != null && j.getJobTemplate().getName() != null && j.getJobTemplate().getName().equals(config
                                    .getName())) {
                                j.getJobTemplate().setName(newName);
                                changed = true;
                            }
                        }
                    }
                    if (changed) { // TODO is it really ok that setDeployed(false)? I think, NO
                        workflow.setContent(Globals.objectMapper.writeValueAsString(w));
                        //workflow.setDeployed(false);
                        int i = items.indexOf(workflow);
                        if (i != -1) {
                            items.get(i).setContent(workflow.getContent());
                            //items.get(i).setDeployed(false);
                        } else {
                            JocInventory.updateConfiguration(dbLayer, workflow);
                            events.add(workflow.getFolder());
                        }
                    }
                }
            }
            break;
            
        default:
            break;
        }
        return events;
    }

    public static SuffixPrefix getSuffixPrefix(String _suffix, String _prefix, SuffixPrefix setting, String defaultValue, String _name,
            ConfigurationType type, InventoryDBLayer dbLayer) throws SOSHibernateException {
        String prefix = _prefix == null ? "" : _prefix.trim().replaceFirst("-+$", "");
        String suffix = _suffix == null ? "" : _suffix.trim().replaceFirst("^-+", "");
        String name = isFolder(type) ? null : _name;

        if (!suffix.isEmpty()) { // suffix beats prefix
            prefix = "";
        } else if (prefix.isEmpty()) {
            suffix = setting.getSuffix();
            if (suffix.isEmpty()) {
                prefix = setting.getPrefix();
            }
        }
        if (suffix.isEmpty() && prefix.isEmpty()) {
            suffix = defaultValue;
        }

        if (!suffix.isEmpty()) {
            SOSCheckJavaVariableName.test("suffix", suffix);
            // determine number of suffix "-suffix<number>"
            Integer num = dbLayer.getSuffixNumber(suffix, name, type.intValue());
            if (num > 0) {
                suffix += num;
            }
        } else if (!prefix.isEmpty()) {
            SOSCheckJavaVariableName.test("prefix", prefix);
            // determine number of prefix "prefix<number>-"
            Integer num = dbLayer.getPrefixNumber(prefix, name, type.intValue());
            if (num > 0) {
                prefix += num;
            }
        }

        SuffixPrefix suffixPrefix = new SuffixPrefix();
        suffixPrefix.setPrefix(prefix);
        suffixPrefix.setSuffix(suffix);

        return suffixPrefix;
    }

    public static List<String> getSearchReplace(SuffixPrefix suffixPrefix) {
        return suffixPrefix.getSuffix().isEmpty() ? Arrays.asList("^(" + suffixPrefix.getPrefix().replaceFirst("[0-9]*$", "") + "[0-9]*-)?(.*)$",
                suffixPrefix.getPrefix() + "-$2") : Arrays.asList("(.*?)(-" + suffixPrefix.getSuffix().replaceFirst("[0-9]*$", "") + "[0-9]*)?$",
                        "$1-" + suffixPrefix.getSuffix());
    }

    public static <T extends Schedule> T setWorkflowNames(T schedule) {
        if (schedule != null) {
            if (JocInventory.SCHEDULE_CONSIDER_WORKFLOW_NAME) {
                List<String> wn = new ArrayList<>();
                if (schedule.getWorkflowName() == null) {
                    if (schedule.getWorkflowNames() != null) {
                        wn.addAll(schedule.getWorkflowNames());
                    }
                } else {
                    if (schedule.getWorkflowNames() == null) {
                        wn.add(schedule.getWorkflowName());
                    } else {
                        wn.addAll(schedule.getWorkflowNames());
                    }
                }
                schedule.setWorkflowNames(wn);
            }
        }
        return schedule;
    }

    public static List<String> getWorkflowNamesFromScheduleJson (String json) {
        // pattern: ^.*workflowNames\"\:\[(.*?)\].*$
        String regex = "^.*workflowNames\\\"\\:\\[(.*?)\\].*$";
        List<String> workflows = new ArrayList<String>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(json);
        if(matcher.matches()) {
            String workflowNamesArray = matcher.group(1);
            String[] workflowNamesSplitted = workflowNamesArray.split(",");
            for(int i=0; i < workflowNamesSplitted.length; i++) {
                workflows.add(workflowNamesSplitted[i].trim().substring(1, workflowNamesSplitted[i].trim().length() -1));
            }
        }
        return workflows;
    }

    public static void postDeployHistoryEvent(Collection<DBItemDeploymentHistory> dbItems) {
        if (dbItems != null) {
            EnumSet<DeployType> eSet = EnumSet.of(DeployType.WORKFLOW, DeployType.JOBRESOURCE, DeployType.FILEORDERSOURCE);
            Map<Integer, List<DBItemDeploymentHistory>> items = dbItems.stream().filter(item -> eSet.contains(item.getTypeAsEnum())).collect(
                    Collectors.groupingBy(DBItemDeploymentHistory::getType));

            if (items.containsKey(DeployType.WORKFLOW.intValue())) {
                Set<String> controllerIds = new HashSet<>();
                items.get(DeployType.WORKFLOW.intValue()).forEach(dbItem -> {
                    controllerIds.add(dbItem.getControllerId());
                    // Consumer: WorkflowPaths
                    EventBus.getInstance().post(new DeployHistoryWorkflowPathEvent(dbItem.getName(), dbItem.getPath()));
                });
                // Consumer: WorkflowRefs
                controllerIds.stream().map(DeployHistoryWorkflowEvent::new).forEach(EventBus.getInstance()::post);
            }

            if (items.containsKey(DeployType.JOBRESOURCE.intValue())) {
                items.get(DeployType.JOBRESOURCE.intValue()).forEach(dbItem -> {
                    // Consumer: SystemMonitoringModel
                    EventBus.getInstance().post(new DeployHistoryJobResourceEvent(dbItem.getControllerId(), dbItem.getName(), dbItem.getCommitId(),
                            dbItem.getPath(), ConfigurationType.JOBRESOURCE.intValue()));
                });
            }

            if (items.containsKey(DeployType.FILEORDERSOURCE.intValue())) {
                // Consumer: WorkflowRefs
                items.get(DeployType.FILEORDERSOURCE.intValue()).stream().map(DBItemDeploymentHistory::getControllerId).distinct().map(
                        DeployHistoryFileOrdersSourceEvent::new).forEach(EventBus.getInstance()::post);
            }
        }
    }
    
    public static void postDeployHistoryEventWhenDeleted(Collection<DBItemDeploymentHistory> dbItems) {
        if (dbItems != null) {
            EnumSet<DeployType> eSet = EnumSet.of(DeployType.WORKFLOW, DeployType.FILEORDERSOURCE);
            Map<Integer, Set<String>> items = dbItems.stream().filter(item -> eSet.contains(item.getTypeAsEnum())).collect(Collectors.groupingBy(
                    DBItemDeploymentHistory::getType, Collectors.mapping(DBItemDeploymentHistory::getControllerId, Collectors.toSet())));

            // Consumer: WorkflowRefs
            items.getOrDefault(DeployType.WORKFLOW.intValue(), Collections.emptySet()).stream().map(DeployHistoryWorkflowEvent::new).forEach(EventBus
                    .getInstance()::post);
            // Consumer: WorkflowRefs
            items.getOrDefault(DeployType.FILEORDERSOURCE.intValue(), Collections.emptySet()).stream().map(DeployHistoryFileOrdersSourceEvent::new)
                    .forEach(EventBus.getInstance()::post);
        }
    }
    
    public static boolean isJsonHashEqual(String json1, String json2, ConfigurationType type) throws IOException {
        return SOSString.hashMD5(
                Globals.prettyPrintObjectMapper.writeValueAsString(
                    Globals.objectMapper.readValue(json1, CLASS_MAPPING.get(type))))
            .equals(
                SOSString.hashMD5(
                    Globals.prettyPrintObjectMapper.writeValueAsString(
                        Globals.objectMapper.readValue(json2, CLASS_MAPPING.get(type)))));
    }
    
}
