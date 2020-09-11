package com.sos.joc.inventory.impl;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.model.jobclass.JobClass;
import com.sos.jobscheduler.model.junction.Junction;
import com.sos.jobscheduler.model.lock.Lock;
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
import com.sos.joc.db.inventory.DBItemInventoryWorkflowOrder;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IStoreConfigurationResource;
import com.sos.joc.model.calendar.Calendar;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.AgentClusterSchedulingType;
import com.sos.joc.model.inventory.common.CalendarType;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.ItemStateEnum;
import com.sos.joc.model.inventory.common.LockType;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class StoreConfigurationResourceImpl extends JOCResourceImpl implements IStoreConfigurationResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreConfigurationResourceImpl.class);
    public static final Map<ConfigurationType, String> SCHEMA_LOCATION = Collections.unmodifiableMap(new HashMap<ConfigurationType, String>() {

        private static final long serialVersionUID = 1L;

        {
            put(ConfigurationType.AGENTCLUSTER, "classpath:/raml/jobscheduler/schemas/agent/agentRef-schema.json");
            put(ConfigurationType.CALENDAR, "classpath:/raml/joc/schemas/calendar/calendar-schema.json");
            put(ConfigurationType.JOB, "classpath:/raml/jobscheduler/schemas/job/job-schema.json");
            put(ConfigurationType.JOBCLASS, "classpath:/raml/jobscheduler/schemas/jobClass/jobClass-schema.json");
            put(ConfigurationType.JUNCTION, "classpath:/raml/jobscheduler/schemas/junction/junction-schema.json");
            put(ConfigurationType.LOCK, "classpath:/raml/jobscheduler/schemas/lock/lock-schema.json");
            put(ConfigurationType.ORDER, "classpath:/raml/orderManagement/schemas/orders/orderTemplate-schema.json");
            put(ConfigurationType.WORKFLOW, "classpath:/raml/jobscheduler/schemas/workflow/workflow-schema.json");
            put(ConfigurationType.FOLDER, "classpath:/raml/jobscheduler/schemas/inventory/folder/folder-schema.json");
        }
    });

    @Override
    public JOCDefaultResponse store(final String accessToken, final byte[] inBytes) {
        try {
            JsonValidator.validateFailFast(inBytes, ConfigurationObject.class);
            ConfigurationObject in = Globals.objectMapper.readValue(inBytes, ConfigurationObject.class);

            JOCDefaultResponse response = checkPermissions(accessToken, in);
            if (response == null) {
                response = store(in);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private JOCDefaultResponse store(ConfigurationObject in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            session.beginTransaction();
            DBItemInventoryConfiguration config = null;
            if (in.getId() != null && in.getId() > 0) {
                config = dbLayer.getConfiguration(in.getId());
            } else {
                config = dbLayer.getConfiguration(in.getPath(), in.getObjectType().intValue());
            }

            // TMP solution - read json
            
            //JsonObject inConfig = JocInventory.readJsonObject(in.getConfiguration());

            if (config == null) {
                config = new DBItemInventoryConfiguration();
                config.setType(in.getObjectType());
                config = setProperties(in, config, true);
                config.setCreated(new Date());
                createAuditLog(config, in.getObjectType());
                session.save(config);
            } else {
                // TODO
                if (1 == 2 && in.getConfiguration() != null) {
                    if (in.getValid() != null) {
                        if (!in.getValid().equals(config.getValide())) {
                            config.setValide(in.getValid());
                            config.setDeployed(false);
                            config.setModified(new Date());
                            session.update(config);
                        }
                    }
                    session.commit();

                    ConfigurationObject item = new ConfigurationObject();
                    item.setId(config.getId());
                    item.setDeliveryDate(new Date());
                    item.setPath(config.getPath());
                    item.setConfigurationDate(config.getModified());
                    item.setObjectType(JocInventory.getType(config.getType()));
                    item.setValid(config.getValide());
                    item.setDeployed(config.getDeployed());
                    return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(item));
                }

                config = setProperties(in, config, false);
                session.update(config);
            }

            switch (in.getObjectType()) {
            case WORKFLOW:
                // Workflow w = (Workflow) in.getConfiguration();
                break;
            case JOBCLASS:
                JobClass jobClass = (JobClass) in.getConfiguration();
                Integer maxProcesses = 30;
                Integer inConfigMaxProcesses = jobClass.getMaxProcesses();
                if (inConfigMaxProcesses != null) {
                    maxProcesses = inConfigMaxProcesses;
                }

                DBItemInventoryJobClass jc = dbLayer.getJobClass(config.getId());
                if (jc == null) {
                    jc = new DBItemInventoryJobClass();
                    jc.setCid(config.getId());
                    jc.setMaxProcesses(maxProcesses);
                    session.save(jc);
                } else {
                    jc.setMaxProcesses(maxProcesses);
                    session.update(jc);
                }
                break;
            case AGENTCLUSTER:
                DBItemInventoryAgentCluster ac = dbLayer.getAgentCluster(config.getId());
                if (ac == null) {
                    ac = new DBItemInventoryAgentCluster();
                    ac.setCid(config.getId());

                    ac.setNumberOfAgents(1L);// TODO
                    ac.setSchedulingType(AgentClusterSchedulingType.FIXED_PRIORITY);// TODO
                    session.save(ac);
                } else {

                    // ac.setNumberOfAgents(1L);// TODO
                    // ac.setSchedulingType(AgentClusterSchedulingType.FIXED_PRIORITY);// TODO
                    // session.update(ac);
                }
                break;
            case LOCK:
                Lock lock = (Lock) in.getConfiguration();
                LockType lockType = LockType.EXCLUSIVE;
                Integer maxNonExclusive = 0;

                Integer inConfigMaxNonExclusive = lock.getMaxNonExclusive();
                if (inConfigMaxNonExclusive != null) {
                    maxNonExclusive = inConfigMaxNonExclusive;
                }
                
                if (maxNonExclusive > 0) {
                    lockType = LockType.SHARED;
                }
                

                DBItemInventoryLock l = dbLayer.getLock(config.getId());
                if (l == null) {
                    l = new DBItemInventoryLock();
                    l.setCid(config.getId());
                    l.setType(lockType);
                    l.setMaxNonExclusive(maxNonExclusive);
                    session.save(l);
                } else {
                    l.setType(lockType);
                    l.setMaxNonExclusive(maxNonExclusive);
                    session.update(l);
                }
                break;
            case JUNCTION:
                Junction junction = (Junction) in.getConfiguration();
                Integer lifeTime = 0;
                Integer inConfigLifeTime = junction.getLifetime();
                if (inConfigLifeTime != null) {
                    lifeTime = inConfigLifeTime;
                }

                DBItemInventoryJunction j = dbLayer.getJunction(config.getId());
                if (j == null) {
                    j = new DBItemInventoryJunction();
                    j.setCid(config.getId());
                    j.setLifetime(lifeTime + "");
                    session.save(j);
                } else {
                    j.setLifetime(lifeTime + "");
                    session.update(j);
                }
                break;
            case ORDER:
                DBItemInventoryWorkflowOrder wo = dbLayer.getWorkflowOrder(config.getId());
                if (wo == null) {
                    wo = new DBItemInventoryWorkflowOrder();
                    wo.setCid(config.getId());

                    wo.setCidWorkflow(0L); // TODO
                    wo.setCidCalendar(0L);
                    wo.setCidNwCalendar(0L);

                    session.save(wo);
                } else {
                    // TODO update
                    // session.update(wo);
                }
                break;
            case CALENDAR:
                Calendar calendar = (Calendar) in.getConfiguration();
                CalendarType calType = CalendarType.WORKINGDAYSCALENDAR;
                String inConfigType = calendar.getType().value();
                if (!SOSString.isEmpty(inConfigType)) {
                    if ("NON_WORKING_DAYS".equals(inConfigType)) {
                        calType = CalendarType.NONWORKINGDAYSCALENDAR;
                    }
                }

                DBItemInventoryCalendar c = dbLayer.getCalendar(config.getId());
                if (c == null) {
                    c = new DBItemInventoryCalendar();
                    c.setCid(config.getId());
                    c.setType(calType);
                    session.save(c);
                } else {
                    c.setType(calType);
                    session.update(c);
                }
                break;
            default:
                break;
            }
            session.commit();

            ConfigurationObject item = new ConfigurationObject();
            item.setId(config.getId());
            item.setDeliveryDate(new Date());
            item.setPath(config.getPath());
            item.setConfigurationDate(config.getModified());
            item.setObjectType(JocInventory.getType(config.getType()));
            item.setValid(config.getValide());
            item.setDeployed(false);
            item.setState(ItemStateEnum.DRAFT_IS_NEWER);// TODO

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(item));
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private void createAuditLog(DBItemInventoryConfiguration config, ConfigurationType objectType) throws Exception {
        InventoryAudit audit = new InventoryAudit(objectType, config.getPath(), config.getFolder());
        logAuditMessage(audit);
        DBItemJocAuditLog auditItem = storeAuditLogEntry(audit);
        if (auditItem != null) {
            config.setAuditLogId(auditItem.getId());
        }
    }

    private DBItemInventoryConfiguration setProperties(ConfigurationObject in, DBItemInventoryConfiguration item, boolean isNew) throws Exception {

        if (isNew) {
            InventoryPath path = new InventoryPath(in.getPath());
            item.setPath(path.getPath());
            item.setName(path.getName());
            if (ConfigurationType.FOLDER.equals(in.getObjectType())) {
                // TODO why setFolder and setParentFolder not the same than other objects?
                item.setFolder(path.getPath());
                item.setParentFolder(path.getFolder());
                item.setValide(true);
            } else {
                item.setFolder(path.getFolder());
                item.setParentFolder(path.getParentFolder());
                item.setValide(false);
            }
            item.setDocumentationId(0L);
        }
        item.setTitle(null);
        
        if (!ConfigurationType.FOLDER.equals(in.getObjectType())) {
            if (in.getConfiguration() == null) {
                item.setContent(null);
                item.setValide(false);
            } else {
                item.setValide(in.getValid() == null ? true : in.getValid());
                
                IConfigurationObject obj = in.getConfiguration();
                //"path" is required in schemas except for JOB and FOLDER
                if (!ConfigurationType.JOB.equals(in.getObjectType())) {
                    obj.setPath(item.getPath());
                }
                validate(item, in, obj);
            }
        }
        
        //item.setContentJoc(in.getConfiguration());
        item.setDeployed(false);
        item.setModified(Date.from(Instant.now()));
        return item;
    }

    private static void validate(DBItemInventoryConfiguration item, ConfigurationObject in, IConfigurationObject obj) {
        try {
            byte[] objBytes = Globals.objectMapper.writeValueAsBytes(obj);
            JsonValidator.validateFailFast(objBytes, URI.create(SCHEMA_LOCATION.get(in.getObjectType())));
            item.setContent(new String(objBytes, StandardCharsets.UTF_8));
        } catch (Throwable e) {
            item.setContent(null);
            item.setValide(false);
            LOGGER.warn(String.format("[invalid][client valid=%s][%s] %s", in.getValid(), in.getConfiguration().toString(), e.toString()));
        }
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, final ConfigurationObject in) throws Exception {
        SOSPermissionJocCockpit permissions = getPermissonsJocCockpit("", accessToken);
        boolean permission = permissions.getInventory().getConfigurations().isEdit();
        return init(IMPL_PATH, in, accessToken, "", permission);
    }

}
