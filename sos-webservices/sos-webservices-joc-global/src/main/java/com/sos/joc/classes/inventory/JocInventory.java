package com.sos.joc.classes.inventory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.model.agent.AgentRef;
import com.sos.jobscheduler.model.job.Job;
import com.sos.jobscheduler.model.jobclass.JobClass;
import com.sos.jobscheduler.model.junction.Junction;
import com.sos.jobscheduler.model.lock.Lock;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.model.calendar.Calendar;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
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
            put(ConfigurationType.CALENDAR, Calendar.class);
            put(ConfigurationType.JOB, Job.class);
            put(ConfigurationType.JOBCLASS, JobClass.class);
            put(ConfigurationType.JUNCTION, Junction.class);
            put(ConfigurationType.LOCK, Lock.class);
            put(ConfigurationType.ORDER, OrderTemplate.class);
            put(ConfigurationType.WORKFLOW, Workflow.class);
            put(ConfigurationType.FOLDER, Folder.class);
        }
    });

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

    public static IConfigurationObject content2IJSObject(String content, Integer typeNum) throws JsonParseException, JsonMappingException,
            IOException {
        ConfigurationType type = getType(typeNum);
        if (SOSString.isEmpty(content) || ConfigurationType.FOLDER.equals(type)) {
            return null;
        }
        return (IConfigurationObject) Globals.objectMapper.readValue(content, CLASS_MAPPING.get(type));
    }

    public static boolean long2boolean(Long val) {
        return val != null && val.longValue() > 0;
    }

    public static class InventoryPath {

        private String path = "";
        private String name = "";
        private String folder = ROOT_FOLDER;
        private String parentFolder = ROOT_FOLDER;

        public InventoryPath(final String inventoryPath) {
            if (!SOSString.isEmpty(inventoryPath)) {
                path = Globals.normalizePath(inventoryPath);
                Path p = Paths.get(path);
                name = p.getFileName().toString();
                folder = normalizeFolder(p.getParent());
                if (folder.equals(ROOT_FOLDER)) {
                    parentFolder = ROOT_FOLDER;
                } else {
                    parentFolder = normalizeFolder(p.getParent().getParent());
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

        public String getParentFolder() {
            return parentFolder;
        }

        private String normalizeFolder(Path folder) {
            String s = folder.toString().replace('\\', '/');
            return SOSString.isEmpty(s) ? ROOT_FOLDER : s;
        }
    }

}
