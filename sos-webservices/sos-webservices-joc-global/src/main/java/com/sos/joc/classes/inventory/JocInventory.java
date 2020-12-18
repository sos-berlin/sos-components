package com.sos.joc.classes.inventory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.model.instruction.InstructionType;
import com.sos.jobscheduler.model.job.Job;
import com.sos.jobscheduler.model.jobclass.JobClass;
import com.sos.jobscheduler.model.junction.Junction;
import com.sos.jobscheduler.model.lock.Lock;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.CheckJavaVariableName;
import com.sos.joc.classes.inventory.search.WorkflowConverter;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.search.DBItemSearchWorkflow;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.model.calendar.Calendar;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.RequestFilter;
import com.sos.joc.model.inventory.folder.Folder;
import com.sos.webservices.order.initiator.model.Schedule;

public class JocInventory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocInventory.class);
    public static final String APPLICATION_PATH = "inventory";
    public static final String ROOT_FOLDER = "/";

    public static final Map<ConfigurationType, String> SCHEMA_LOCATION = Collections.unmodifiableMap(new HashMap<ConfigurationType, String>() {

        private static final long serialVersionUID = 1L;

        {
            put(ConfigurationType.WORKINGDAYSCALENDAR, "classpath:/raml/joc/schemas/calendar/calendar-schema.json");
            put(ConfigurationType.NONWORKINGDAYSCALENDAR, "classpath:/raml/joc/schemas/calendar/calendar-schema.json");
            put(ConfigurationType.JOB, "classpath:/raml/jobscheduler/schemas/job/job-schema.json");
            put(ConfigurationType.JOBCLASS, "classpath:/raml/jobscheduler/schemas/jobClass/jobClass-schema.json");
            put(ConfigurationType.JUNCTION, "classpath:/raml/jobscheduler/schemas/junction/junction-schema.json");
            put(ConfigurationType.LOCK, "classpath:/raml/jobscheduler/schemas/lock/lock-schema.json");
            put(ConfigurationType.SCHEDULE, "classpath:/raml/orderManagement/schemas/orders/schedule-schema.json");
            put(ConfigurationType.WORKFLOW, "classpath:/raml/jobscheduler/schemas/workflow/workflow-schema.json");
            put(ConfigurationType.FOLDER, "classpath:/raml/jobscheduler/schemas/inventory/folder/folder-schema.json");
        }
    });

    public static final Map<InstructionType, String> INSTRUCTION_SCHEMA_LOCATION = Collections.unmodifiableMap(
            new HashMap<InstructionType, String>() {

                private static final long serialVersionUID = 1L;

                {
                    // TODO put(InstructionType.AWAIT, "classpath:/raml/jobscheduler/schemas/instruction/await-schema.json");
                    put(InstructionType.EXECUTE_NAMED, "classpath:/raml/jobscheduler/schemas/instruction/namedJob-schema.json");
                    put(InstructionType.FAIL, "classpath:/raml/jobscheduler/schemas/instruction/fail-schema.json");
                    put(InstructionType.FINISH, "classpath:/raml/jobscheduler/schemas/instruction/finish-schema.json");
                    put(InstructionType.FORK, "classpath:/raml/jobscheduler/schemas/instruction/forkJoin-schema.json");
                    put(InstructionType.IF, "classpath:/raml/jobscheduler/schemas/instruction/ifelse-schema.json");
                    // TODO put(InstructionType.PUBLISH, "classpath:/raml/jobscheduler/schemas/instruction/publish-schema.json");
                    put(InstructionType.RETRY, "classpath:/raml/jobscheduler/schemas/instruction/retryInCatch-schema.json");
                    put(InstructionType.TRY, "classpath:/raml/jobscheduler/schemas/instruction/retry-schema.json");
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

    public static void deleteConfigurations(Set<Long> ids) {
        if (ids != null && ids.size() > 0) {
            SOSHibernateSession session = null;
            try {
                session = Globals.createSosHibernateStatelessConnection(getResourceImplPath("deleteConfigurations"));
                session.setAutoCommit(false);
                InventoryDBLayer dbLayer = new InventoryDBLayer(session);

                session.beginTransaction();
                // List<Object[]> items = dbLayer.getConfigurationProperties(ids, "id,type");
                // for (Object[] item : items) {
                // Long id = (Long) item[0];
                // Integer type = (Integer) item[1];
                // TODO handle types
                // dbLayer.deleteConfiguration(id);
                // }
                dbLayer.deleteConfigurations(ids);
                session.commit();
            } catch (Throwable e) {
                LOGGER.error(e.toString(), e);
                Globals.rollback(session);
            } finally {
                Globals.disconnect(session);
            }
        }
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
    
    public static Path makeParentDir(InventoryDBLayer dbLayer, Path folder) throws JsonParseException, JsonMappingException, SOSHibernateException,
            JsonProcessingException, IOException {
        if (folder == null) {
            return null;
        }
        String f = folder.toString().replace('\\', '/');
        if (ROOT_FOLDER.equals(f)) {
            return null;
        }
        DBItemInventoryConfiguration dbFolder = dbLayer.getConfiguration(f, ConfigurationType.FOLDER.intValue());
        if (dbFolder != null) { // folder already exists
            return null;
        }
        DBItemInventoryConfiguration item = new DBItemInventoryConfiguration();
        item.setType(ConfigurationType.FOLDER);
        InventoryPath path = new InventoryPath(folder, ConfigurationType.FOLDER);
        item.setPath(path.getPath());
        item.setName(path.getName());
        item.setFolder(path.getFolder());
        item.setValid(false);
        item.setDocumentationId(0L);
        item.setTitle(null);
        item.setTitle(null);
        item.setValid(true);
        item.setDeployed(false);
        item.setReleased(false);
        item.setContent(null);
        item.setModified(Date.from(Instant.now()));
        item.setCreated(Date.from(Instant.now()));
        insertConfiguration(dbLayer, item, null);
        if (ROOT_FOLDER.equals(path.getFolder())) {
            return null;
        }
        return folder.getParent();
    }
    
    public static void makeParentDirs(InventoryDBLayer dbLayer, Path folder) throws JsonParseException, JsonMappingException, SOSHibernateException,
            JsonProcessingException, IOException {
        Path parent = makeParentDir(dbLayer, folder);
        while (parent != null) {
            parent = makeParentDir(dbLayer, parent);
        }
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

        groupedWorkflows.keySet().forEach(inventoryId -> {
            List<DBItemDeploymentHistory> list = groupedWorkflows.get(inventoryId);
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

            item = new DBItemSearchWorkflow();
            item.setInventoryConfigurationId(inventoryId);
            item.setDeployed(deployed);
            item.setContentHash(hash);
            item.setCreated(new Date());
            item.setModified(item.getCreated());

            item = convert(item, workflow);
            dbLayer.getSession().save(item);

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
                InventoryDBLayer dbLayer = new InventoryDBLayer(session);
                // dbLayer.deleteSearchWorkflowByInventoryId(id, deployed)
            }

        }
    }

}
