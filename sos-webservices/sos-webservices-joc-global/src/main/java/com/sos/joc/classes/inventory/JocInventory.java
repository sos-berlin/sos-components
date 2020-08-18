package com.sos.joc.classes.inventory;

import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.model.agent.AgentRef;
import com.sos.jobscheduler.model.deploy.DeployObject;
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.InventoryMeta;
import com.sos.joc.db.inventory.InventoryMeta.ConfigurationType;
import com.sos.joc.model.common.JobSchedulerObjectType;

public class JocInventory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocInventory.class);
    public static final String APPLICATION_PATH = "inventory";
    public static final String ROOT_FOLDER = "/";

    public static String getResourceImplPath(final String path) {
        return String.format("./%s/%s", APPLICATION_PATH, path);
    }

    public static void deleteConfigurations(Set<Long> ids) {
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

    public static Integer getType(JobSchedulerObjectType type) {
        Integer result = null;
        try {
            result = InventoryMeta.ConfigurationType.valueOf(type.name()).value();
        } catch (Exception e) {
        }
        return result;
    }

    public static ConfigurationType getType(Integer type) {
        ConfigurationType result = null;
        try {
            result = InventoryMeta.ConfigurationType.fromValue(type);
        } catch (Exception e) {
        }
        return result;
    }

    public static ConfigurationType getType(String type) {
        ConfigurationType result = null;
        try {
            result = InventoryMeta.ConfigurationType.valueOf(type);
        } catch (Exception e) {
        }
        return result;
    }

    public static JobSchedulerObjectType getJobSchedulerType(Integer type) {
        JobSchedulerObjectType result = null;
        try {
            result = JobSchedulerObjectType.fromValue(InventoryMeta.ConfigurationType.fromValue(type).name());
        } catch (Exception e) {
        }
        return result;
    }

    // TODO beans
    public static String convertDeployableContent2Joc(String content, ConfigurationType type) {
        if (SOSString.isEmpty(content)) {
            return "{}";
        }
        switch (type) {
        case AGENTCLUSTER:
            try {
                AgentRef agent = Globals.objectMapper.readValue(content, AgentRef.class);
                StringBuilder sb = new StringBuilder("{");
                sb.append("\"maxProcess\":").append(agent.getMaxProcesses() == null ? 1 : agent.getMaxProcesses());
                sb.append(",\"hosts\":[{\"url\":\"").append(agent.getUri()).append("\"}]");
                sb.append(",\"select\":\"first\"");
                sb.append("}");
                content = sb.toString();
            } catch (Throwable e) {
                LOGGER.error(e.toString(), e);
            }
            return content;
        default:
            return content;
        }
    }

    public static DeployObject convertJocContent2Deployable(String content, ConfigurationType type) throws Exception {
        switch (type) {
        case WORKFLOW:
            content = "{\"TYPE\":\"Workflow\"," + content.substring(1);
            // content = "{\"TYPE\":\"Workflow\",\"path\":\"" + in.getPath() + "\"," + in.getConfiguration().substring(1);
            content = content.replaceAll("\\{\"success\":\"(\\d)*\"\\}", "\\{\"success\":[$1]\\}");
            content = content.replaceAll("\\{\"failure\":\"(\\d)*\"\\}", "\\{\"failure\":[$1]\\}");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(content);
            }
            return Globals.objectMapper.readValue(content, Workflow.class);

        case AGENTCLUSTER:
            JsonObject jo = readJsonObject(content);
            Integer maxProcess = getAsInt(jo, "maxProcess");
            String uri = null;
            JsonArray arr = getAsJsonArray(jo, "hosts");
            if (arr != null && arr.size() > 0) {
                JsonObject o = arr.getValuesAs(JsonObject.class).get(0);
                uri = getAsString(o, "url");
            }
            AgentRef agentRef = new AgentRef();
            agentRef.setTYPE(DeployType.AGENT_REF);
            agentRef.setMaxProcesses(maxProcess);
            agentRef.setUri(uri);
            return agentRef;
        default:
            return null;
        }
    }

    // TODO tmp solution
    public static JsonObject readJsonObject(String input) throws Exception {
        StringReader sr = null;
        JsonReader jr = null;
        try {
            sr = new StringReader(input);
            jr = Json.createReader(sr);
            return jr.readObject();
        } catch (Throwable e) {
            LOGGER.error(String.format("[readJsonObject][%s]%s", input, e.toString()));
            // throw e;
        } finally {
            if (jr != null) {
                jr.close();
            }
            if (sr != null) {
                sr.close();
            }
        }
        return null;
    }

    public static Integer getAsInt(JsonObject o, String property) {
        if (o != null) {
            try {
                JsonNumber n = o.getJsonNumber(property);
                if (n != null) {
                    return n.intValue();
                }
            } catch (Throwable e) {
            }
        }
        return null;
    }

    public static JsonArray getAsJsonArray(JsonObject o, String property) {
        if (o != null) {
            try {
                JsonArray n = o.getJsonArray(property);
                if (n != null) {
                    return n;
                }
            } catch (Throwable e) {
            }
        }
        return null;
    }

    public static Boolean getAsBoolean(JsonObject o, String property) {
        if (o != null) {
            try {
                return o.getBoolean(property);
            } catch (Throwable e) {
            }
        }
        return null;
    }

    public static String getAsString(JsonObject o, String property) {
        if (o != null) {
            try {
                return o.getString(property);
            } catch (Throwable e) {
            }
        }
        return null;
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
