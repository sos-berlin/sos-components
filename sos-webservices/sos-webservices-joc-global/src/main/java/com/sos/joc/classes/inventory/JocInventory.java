package com.sos.joc.classes.inventory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.rest.SOSShiroFolderPermissions;
import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
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
import com.sos.joc.classes.JOCDefaultResponse;
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
    
    public static Collection<Integer> getCalendarTypes() {
        return Arrays.asList(ConfigurationType.WORKINGDAYSCALENDAR.intValue(), ConfigurationType.NONWORKINGDAYSCALENDAR.intValue());
    }
    
    public static boolean isDeployable(ConfigurationType type) {
        return DEPLOYABLE_OBJECTS.contains(type);
    }
    
    public static boolean isReleasable(ConfigurationType type) {
        return RELEASABLE_OBJECTS.contains(type);
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
                folder = normalizeFolder(p.getParent());
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
        DBItemInventoryConfiguration config = null;
        if (in.getId() != null) {
            config = dbLayer.getConfiguration(in.getId());
            if (config == null) {
                throw new DBMissingDataException(String.format("configuration not found: %s", in.getId()));
            }
            if (!folderPermissions.isPermittedForFolder(config.getFolder())) {
                throw new JocFolderPermissionsException("Access denied for folder: " + config.getFolder());
            }
        } else {
            if (!folderPermissions.isPermittedForFolder(in.getPath())) {
                throw new JocFolderPermissionsException("Access denied for folder: " + in.getPath());
            }
            if (JocInventory.ROOT_FOLDER.equals(in.getPath()) && ConfigurationType.FOLDER.equals(in.getObjectType())) {
                config = new DBItemInventoryConfiguration();
                config.setId(0L);
                config.setPath(in.getPath());
                config.setType(in.getObjectType());
                config.setFolder(in.getPath());
                config.setDeleted(false);
                config.setValid(true);
                config.setDeployed(false);
                config.setReleased(false);
            } else {
                config = dbLayer.getConfiguration(in.getPath(), in.getObjectType().intValue());
                if (config == null) {
                    throw new DBMissingDataException(String.format("%s not found: %s", in.getObjectType().value().toLowerCase(), in.getPath()));
                }
            }
        }
        return config;
    }

}
