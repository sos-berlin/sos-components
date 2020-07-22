package com.sos.joc.inventory.impl;

import java.io.StringReader;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.InventoryAudit;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.JocInventory.InventoryPath;
import com.sos.joc.db.inventory.DBItemInventoryAgentCluster;
import com.sos.joc.db.inventory.DBItemInventoryCalendar;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryJobClass;
import com.sos.joc.db.inventory.DBItemInventoryJunction;
import com.sos.joc.db.inventory.DBItemInventoryLock;
import com.sos.joc.db.inventory.DBItemInventoryWorkflow;
import com.sos.joc.db.inventory.DBItemInventoryWorkflowJob;
import com.sos.joc.db.inventory.DBItemInventoryWorkflowOrder;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.InventoryMeta.AgentClusterSchedulingType;
import com.sos.joc.db.inventory.InventoryMeta.CalendarType;
import com.sos.joc.db.inventory.InventoryMeta.ConfigurationType;
import com.sos.joc.db.inventory.InventoryMeta.LockType;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IStoreConfigurationResource;
import com.sos.joc.model.inventory.common.ConfigurationItem;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class StoreConfigurationResourceImpl extends JOCResourceImpl implements IStoreConfigurationResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreConfigurationResourceImpl.class);

    @Override
    public JOCDefaultResponse store(final String accessToken, final byte[] inBytes) {
        try {
            JsonValidator.validateFailFast(inBytes, ConfigurationItem.class);
            ConfigurationItem in = Globals.objectMapper.readValue(inBytes, ConfigurationItem.class);

            checkRequiredParameter("objectType", in.getObjectType());
            checkRequiredParameter("path", in.getPath());
            in.setPath(Globals.normalizePath(in.getPath()));

            JOCDefaultResponse response = checkPermissions(accessToken, in);
            if (response == null) {
                response = JOCDefaultResponse.responseStatus200(store(in));
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private ConfigurationItem store(ConfigurationItem in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            session.beginTransaction();

            DBItemInventoryConfiguration config = null;
            if (in.getId() != null && in.getId() > 0L) {
                config = dbLayer.getConfiguration(in.getId(), JocInventory.getType(in.getObjectType()));
            }
            if (config == null) {// TODO temp
                config = dbLayer.getConfiguration(in.getPath(), JocInventory.getType(in.getObjectType()));
            }

            ConfigurationType type = null;
            if (config == null) {
                type = JocInventory.getType(in.getObjectType().name());
            } else {
                type = JocInventory.getType(config.getType());
            }
            if (type == null) {
                throw new Exception(String.format("unsupported configuration type=%s", in.getObjectType()));
            }

            if (config == null) {
                config = new DBItemInventoryConfiguration();
                config.setType(type);
                config = setProperties(in, config, type);
                config.setCreated(new Date());

                InventoryAudit audit = new InventoryAudit(in);
                logAuditMessage(audit);
                audit.setStartTime(config.getCreated());
                DBItemJocAuditLog auditItem = storeAuditLogEntry(audit);
                if (auditItem != null) {
                    config.setAuditLogId(auditItem.getId());
                }

                session.save(config);
            } else {
                config = setProperties(in, config, type);
                session.update(config);
            }

            // TMP solution - read json
            JsonObject inConfig = readJsonObject(in.getConfiguration());
            switch (type) {
            case WORKFLOW:
                // TODO setContent, workflowJobs etc
                DBItemInventoryWorkflow w = dbLayer.getWorkflow(config.getId());
                if (w == null) {
                    w = new DBItemInventoryWorkflow();
                    w.setCid(config.getId());

                    w.setContent(in.getConfiguration());// TODO
                    w.setContentJoc(in.getConfiguration());
                    // w.setContentSigned(val);//TODO
                    session.save(w);
                } else {
                    w.setContent(in.getConfiguration());// TODO
                    w.setContentJoc(in.getConfiguration());
                    // w.setContent(in.getConfiguration());// TODO
                    // w.setContentSigned(val); //TODO
                    session.update(w);
                }
                break;
            case JOB:
                DBItemInventoryWorkflowJob wj = dbLayer.getWorkflowJob(config.getId());
                if (wj != null) {
                    // item.setConfiguration(wj.getContent());
                }
                break;
            case JOBCLASS:
                Long maxProcess = 30L;
                Long inConfigMaxProces = getJsonPropertyAsLong(inConfig, "maxProcess");
                if (inConfigMaxProces != null) {
                    maxProcess = inConfigMaxProces;
                }

                DBItemInventoryJobClass jc = dbLayer.getJobClass(config.getId());
                if (jc == null) {
                    jc = new DBItemInventoryJobClass();
                    jc.setCid(config.getId());
                    jc.setContent(in.getConfiguration());
                    jc.setMaxProcesses(maxProcess);
                    session.save(jc);
                } else {
                    jc.setContent(in.getConfiguration());
                    jc.setMaxProcesses(maxProcess);
                    session.update(jc);
                }
                break;
            case AGENTCLUSTER:
                DBItemInventoryAgentCluster ac = dbLayer.getAgentCluster(config.getId());
                if (ac == null) {
                    ac = new DBItemInventoryAgentCluster();
                    ac.setCid(config.getId());
                    ac.setContent(in.getConfiguration());

                    ac.setNumberOfAgents(1L);// TODO
                    ac.setSchedulingType(AgentClusterSchedulingType.FIXED_PRIORITY);// TODO
                    session.save(ac);
                } else {
                    ac.setContent(in.getConfiguration());

                    // ac.setNumberOfAgents(1L);// TODO
                    // ac.setSchedulingType(AgentClusterSchedulingType.FIXED_PRIORITY);// TODO
                    session.update(ac);
                }
                break;
            case LOCK:
                LockType lockType = LockType.EXCLUSIVE;
                Long maxNonExclusive = 0L;

                Boolean inConfigNonExclusive = getJsonPropertyAsBoolean(inConfig, "nonExclusive");
                if (inConfigNonExclusive != null && !inConfigNonExclusive) {
                    lockType = LockType.SHARED;
                }
                Long inConfigMaxNonExclusive = getJsonPropertyAsLong(inConfig, "maxNonExclusive");
                if (inConfigMaxNonExclusive != null) {
                    maxNonExclusive = inConfigMaxNonExclusive;
                }

                DBItemInventoryLock l = dbLayer.getLock(config.getId());
                if (l == null) {
                    l = new DBItemInventoryLock();
                    l.setCid(config.getId());
                    l.setContent(in.getConfiguration());
                    l.setType(lockType);
                    l.setMaxNonExclusive(maxNonExclusive);
                    session.save(l);
                } else {
                    l.setContent(in.getConfiguration());
                    l.setType(lockType);
                    l.setMaxNonExclusive(maxNonExclusive);
                    session.update(l);
                }
                break;
            case JUNCTION:
                String lifeTime = ".";
                String inConfigLifeTime = getJsonPropertyAsString(inConfig, "lifetime");
                if (!SOSString.isEmpty(inConfigLifeTime)) {
                    lifeTime = inConfigLifeTime;
                }

                DBItemInventoryJunction j = dbLayer.getJunction(config.getId());
                if (j == null) {
                    j = new DBItemInventoryJunction();
                    j.setCid(config.getId());
                    j.setContent(in.getConfiguration());
                    j.setLifetime(lifeTime);
                    session.save(j);
                } else {
                    j.setContent(in.getConfiguration());
                    j.setLifetime(lifeTime);
                    session.update(j);
                }
                break;
            case ORDER:
                DBItemInventoryWorkflowOrder wo = dbLayer.getWorkflowOrder(config.getId());
                if (wo == null) {
                    wo = new DBItemInventoryWorkflowOrder();
                    wo.setCid(config.getId());
                    wo.setContent(in.getConfiguration());

                    wo.setCidWorkflow(0L); // TODO
                    wo.setCidCalendar(0L);
                    wo.setCidNwCalendar(0L);

                    session.save(wo);
                } else {
                    wo.setContent(in.getConfiguration());
                    // TODO update

                    session.update(wo);
                }
                break;
            case CALENDAR:
                CalendarType calType = CalendarType.WORKINGDAYSCALENDAR;
                String inConfigType = getJsonPropertyAsString(inConfig, "type");
                if (!SOSString.isEmpty(inConfigType)) {
                    if ("NON_WORKING_DAYS".equals(inConfigType)) {
                        calType = CalendarType.NONWORKINGDAYSCALENDAR;
                    }
                }

                DBItemInventoryCalendar c = dbLayer.getCalendar(config.getId());
                if (c == null) {
                    c = new DBItemInventoryCalendar();
                    c.setCid(config.getId());
                    c.setContent(in.getConfiguration());
                    c.setType(calType);
                    session.save(c);
                } else {
                    c.setContent(in.getConfiguration());
                    c.setType(calType);
                    session.update(c);
                }
                break;
            default:
                break;
            }
            session.commit();

            ConfigurationItem item = new ConfigurationItem();
            item.setId(config.getId());
            item.setDeliveryDate(new Date());
            item.setPath(config.getPath());
            item.setConfigurationDate(config.getModified());
            item.setObjectType(in.getObjectType());

            return item;
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    // TODO tmp solution
    private JsonObject readJsonObject(String input) throws Exception {
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

    private Long getJsonPropertyAsLong(JsonObject o, String property) {
        if (o != null) {
            try {
                JsonNumber n = o.getJsonNumber(property);
                if (n != null) {
                    return n.longValue();
                }
            } catch (Throwable e) {
            }
        }
        return null;
    }

    private Boolean getJsonPropertyAsBoolean(JsonObject o, String property) {
        if (o != null) {
            try {
                return o.getBoolean(property);
            } catch (Throwable e) {
            }
        }
        return null;
    }

    private String getJsonPropertyAsString(JsonObject o, String property) {
        if (o != null) {
            try {
                return o.getString(property);
            } catch (Throwable e) {
            }
        }
        return null;
    }

    private DBItemInventoryConfiguration setProperties(ConfigurationItem in, DBItemInventoryConfiguration item, ConfigurationType type) {
        InventoryPath path = new InventoryPath(in.getPath());

        item.setPath(path.getPath());
        item.setName(path.getName());
        if (type.equals(ConfigurationType.FOLDER)) {
            item.setFolder(path.getPath());
            item.setParentFolder(path.getFolder());
        } else {
            item.setFolder(path.getFolder());
            item.setParentFolder(path.getParentFolder());
        }
        item.setTitle(null);
        item.setDocumentationId(0L);
        item.setModified(new Date());

        return item;
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, final ConfigurationItem in) throws Exception {
        SOSPermissionJocCockpit permissions = getPermissonsJocCockpit("", accessToken);
        boolean permission = permissions.getJobschedulerMaster().getAdministration().getConfigurations().isEdit();

        JOCDefaultResponse response = init(IMPL_PATH, in, accessToken, "", permission);
        if (response == null) {
            String path = normalizePath(in.getPath());
            if (!folderPermissions.isPermittedForFolder(getParent(path))) {
                return accessDeniedResponse();
            }
        }
        return response;
    }

}
