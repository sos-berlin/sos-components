package com.sos.joc.classes.inventory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.rest.SOSShiroFolderPermissions;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.model.agent.AgentRef;
import com.sos.jobscheduler.model.job.Job;
import com.sos.jobscheduler.model.jobclass.JobClass;
import com.sos.jobscheduler.model.junction.Junction;
import com.sos.jobscheduler.model.lock.Lock;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.CheckJavaVariableName;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.model.calendar.Calendar;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.RequestFilter;
import com.sos.joc.model.inventory.folder.Folder;
import com.sos.webservices.order.initiator.model.OrderTemplate;

public class JocInventory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocInventory.class);
    public static final String APPLICATION_PATH = "inventory";
    public static final String ROOT_FOLDER = "/";
    
    public static final Map<ConfigurationType, String> SCHEMA_LOCATION = Collections.unmodifiableMap(new HashMap<ConfigurationType, String>() {

        private static final long serialVersionUID = 1L;

        {
            put(ConfigurationType.AGENTCLUSTER, "classpath:/raml/jobscheduler/schemas/agent/agentRef-schema.json");
            put(ConfigurationType.WORKINGDAYSCALENDAR, "classpath:/raml/joc/schemas/calendar/calendar-schema.json");
            put(ConfigurationType.NONWORKINGDAYSCALENDAR, "classpath:/raml/joc/schemas/calendar/calendar-schema.json");
            put(ConfigurationType.JOB, "classpath:/raml/jobscheduler/schemas/job/job-schema.json");
            put(ConfigurationType.JOBCLASS, "classpath:/raml/jobscheduler/schemas/jobClass/jobClass-schema.json");
            put(ConfigurationType.JUNCTION, "classpath:/raml/jobscheduler/schemas/junction/junction-schema.json");
            put(ConfigurationType.LOCK, "classpath:/raml/jobscheduler/schemas/lock/lock-schema.json");
            put(ConfigurationType.ORDER, "classpath:/raml/orderManagement/schemas/orders/orderTemplate-schema.json");
            put(ConfigurationType.WORKFLOW, "classpath:/raml/jobscheduler/schemas/workflow/workflow-schema.json");
            put(ConfigurationType.FOLDER, "classpath:/raml/jobscheduler/schemas/inventory/folder/folder-schema.json");
        }
    });
    
    public static final Map<ConfigurationType, Class<?>> CLASS_MAPPING = Collections.unmodifiableMap(new HashMap<ConfigurationType, Class<?>>() {

        private static final long serialVersionUID = 1L;

        {
            put(ConfigurationType.AGENTCLUSTER, AgentRef.class);
            put(ConfigurationType.JOB, Job.class);
            put(ConfigurationType.JOBCLASS, JobClass.class);
            put(ConfigurationType.JUNCTION, Junction.class);
            put(ConfigurationType.LOCK, Lock.class);
            put(ConfigurationType.WORKINGDAYSCALENDAR, Calendar.class);
            put(ConfigurationType.NONWORKINGDAYSCALENDAR, Calendar.class);
            put(ConfigurationType.ORDER, OrderTemplate.class);
            put(ConfigurationType.WORKFLOW, Workflow.class);
            put(ConfigurationType.FOLDER, Folder.class);
        }
    });
    
    public static final Set<ConfigurationType> DEPLOYABLE_OBJECTS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            ConfigurationType.AGENTCLUSTER, ConfigurationType.JOB, ConfigurationType.JOBCLASS, ConfigurationType.JUNCTION, ConfigurationType.LOCK,
            ConfigurationType.WORKFLOW)));

    public static final Set<ConfigurationType> RELEASABLE_OBJECTS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(ConfigurationType.ORDER,
            ConfigurationType.NONWORKINGDAYSCALENDAR, ConfigurationType.WORKINGDAYSCALENDAR)));
    
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
                if (session != null && session.isTransactionOpened()) {
                    Globals.rollback(session);
                }
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
        if (ConfigurationType.ORDER.equals(type)) {
            content = content.replaceAll("\"suppress\"", "\"SUPPRESS\"");
        }
        return (IConfigurationObject) Globals.objectMapper.readValue(content, CLASS_MAPPING.get(type));
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
                if (ConfigurationType.FOLDER.equals(type)) {
                    folder = path;
                } else {
                    folder = normalizeFolder(p.getParent());
                }
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
    
    public static DBItemInventoryConfiguration getConfiguration(InventoryDBLayer dbLayer, Long id,
            String path, ConfigurationType type, SOSShiroFolderPermissions folderPermissions) throws Exception {
        DBItemInventoryConfiguration config = null;
        if (id != null) {
            config = dbLayer.getConfiguration(id);
            if (config == null) {
                throw new DBMissingDataException(String.format("configuration not found: %s", id));
            }
            if (!folderPermissions.isPermittedForFolder(config.getFolder())) {
                throw new JocFolderPermissionsException("Access denied for folder: " + config.getFolder());
            }
            // temp. because of rename error on rot folder
            config.setPath(config.getPath().replace("//+", "/"));
        } else {
            if (!folderPermissions.isPermittedForFolder(path)) {
                throw new JocFolderPermissionsException("Access denied for folder: " + path);
            }
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
                config = dbLayer.getConfiguration(path, type.intValue());
                if (config == null) {
                    throw new DBMissingDataException(String.format("%s not found: %s", type.value().toLowerCase(), path));
                }
            }
        }
        return config;
    }

}
