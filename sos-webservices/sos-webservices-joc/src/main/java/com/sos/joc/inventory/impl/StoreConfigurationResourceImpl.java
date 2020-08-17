package com.sos.joc.inventory.impl;

import java.util.Date;

import javax.json.JsonObject;
import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.model.agent.AgentRef;
import com.sos.jobscheduler.model.workflow.Workflow;
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
import com.sos.joc.db.inventory.InventoryMeta.AgentClusterSchedulingType;
import com.sos.joc.db.inventory.InventoryMeta.CalendarType;
import com.sos.joc.db.inventory.InventoryMeta.ConfigurationType;
import com.sos.joc.db.inventory.InventoryMeta.LockType;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IStoreConfigurationResource;
import com.sos.joc.model.inventory.common.ItemStateEnum;
import com.sos.joc.model.inventory.read.configuration.ResponseItem;
import com.sos.joc.model.inventory.store.RequestFilter;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class StoreConfigurationResourceImpl extends JOCResourceImpl implements IStoreConfigurationResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreConfigurationResourceImpl.class);

    @Override
    public JOCDefaultResponse store(final String accessToken, final byte[] inBytes) {
        try {
            JsonValidator.validateFailFast(inBytes, RequestFilter.class);
            RequestFilter in = Globals.objectMapper.readValue(inBytes, RequestFilter.class);

            checkRequiredParameter("path", in.getPath());
            checkRequiredParameter("objectType", in.getObjectType());

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

    private JOCDefaultResponse store(RequestFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            session.beginTransaction();
            DBItemInventoryConfiguration config = null;
            if (in.getId() != null && in.getId() > 0) {
                config = dbLayer.getConfiguration(in.getId());
            } else {
                config = dbLayer.getConfiguration(in.getPath(), JocInventory.getType(in.getObjectType()));
            }

            ConfigurationType type = getConfigurationType(config, in);

            // TMP solution - read json
            JsonObject inConfig = JocInventory.readJsonObject(in.getConfiguration());

            if (config == null) {
                config = new DBItemInventoryConfiguration();
                config.setType(type);
                config = setProperties(in, config, type, inConfig, true);
                config.setCreated(new Date());
                createAuditLog(config, in);
                session.save(config);
            } else {
                if (config.getContentJoc() != null && config.getContentJoc().contentEquals(in.getConfiguration())) {
                    if (in.getValide() != null) {
                        if (!in.getValide().equals(config.getValide())) {
                            config.setValide(in.getValide());
                            config.setDeployed(false);
                            config.setModified(new Date());
                            session.update(config);
                        }
                    }
                    session.commit();

                    ResponseItem item = new ResponseItem();
                    item.setId(config.getId());
                    item.setDeliveryDate(new Date());
                    item.setPath(config.getPath());
                    item.setConfigurationDate(config.getModified());
                    item.setObjectType(JocInventory.getJobSchedulerType(config.getType()));
                    item.setValide(config.getValide());
                    item.setDeployed(config.getDeployed());
                    return JOCDefaultResponse.responseStatus200(item);
                }

                config = setProperties(in, config, type, inConfig, false);
                session.update(config);
            }

            switch (type) {
            case WORKFLOW:
                // Workflow o = readWorkflow(in.getConfiguration());
                break;
            case JOBCLASS:
                Integer maxProcess = 30;
                Integer inConfigMaxProces = JocInventory.getAsInt(inConfig, "maxProcess");
                if (inConfigMaxProces != null) {
                    maxProcess = inConfigMaxProces;
                }

                DBItemInventoryJobClass jc = dbLayer.getJobClass(config.getId());
                if (jc == null) {
                    jc = new DBItemInventoryJobClass();
                    jc.setCid(config.getId());
                    jc.setMaxProcesses(maxProcess);
                    session.save(jc);
                } else {
                    jc.setMaxProcesses(maxProcess);
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
                LockType lockType = LockType.EXCLUSIVE;
                Integer maxNonExclusive = 0;

                Boolean inConfigNonExclusive = JocInventory.getAsBoolean(inConfig, "nonExclusive");
                if (inConfigNonExclusive != null && !inConfigNonExclusive) {
                    lockType = LockType.SHARED;
                }
                Integer inConfigMaxNonExclusive = JocInventory.getAsInt(inConfig, "maxNonExclusive");
                if (inConfigMaxNonExclusive != null) {
                    maxNonExclusive = inConfigMaxNonExclusive;
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
                String lifeTime = ".";
                String inConfigLifeTime = JocInventory.getAsString(inConfig, "lifetime");
                if (!SOSString.isEmpty(inConfigLifeTime)) {
                    lifeTime = inConfigLifeTime;
                }

                DBItemInventoryJunction j = dbLayer.getJunction(config.getId());
                if (j == null) {
                    j = new DBItemInventoryJunction();
                    j.setCid(config.getId());
                    j.setLifetime(lifeTime);
                    session.save(j);
                } else {
                    j.setLifetime(lifeTime);
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
                CalendarType calType = CalendarType.WORKINGDAYSCALENDAR;
                String inConfigType = JocInventory.getAsString(inConfig, "type");
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

            ResponseItem item = new ResponseItem();
            item.setId(config.getId());
            item.setDeliveryDate(new Date());
            item.setPath(config.getPath());
            item.setConfigurationDate(config.getModified());
            item.setObjectType(JocInventory.getJobSchedulerType(config.getType()));
            item.setValide(config.getValide());
            item.setDeployed(false);
            item.setState(ItemStateEnum.DRAFT_IS_NEWER);// TODO

            return JOCDefaultResponse.responseStatus200(item);
        } catch (Throwable e) {
            if (session != null && session.isTransactionOpened()) {
                Globals.rollback(session);
            }
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private ConfigurationType getConfigurationType(DBItemInventoryConfiguration config, RequestFilter in) throws Exception {
        ConfigurationType type = null;
        if (config == null) {
            type = JocInventory.getType(in.getObjectType().name());
        } else {
            type = JocInventory.getType(config.getType());
        }
        if (type == null) {
            throw new Exception(String.format("unsupported configuration type=%s", in.getObjectType()));
        }
        return type;
    }

    private void createAuditLog(DBItemInventoryConfiguration config, RequestFilter in) throws Exception {
        InventoryAudit audit = new InventoryAudit(in.getObjectType(), config.getPath());
        logAuditMessage(audit);
        audit.setStartTime(config.getCreated());
        DBItemJocAuditLog auditItem = storeAuditLogEntry(audit);
        if (auditItem != null) {
            config.setAuditLogId(auditItem.getId());
        }
    }

    private DBItemInventoryConfiguration setProperties(RequestFilter in, DBItemInventoryConfiguration item, ConfigurationType type,
            JsonObject inConfig, boolean isNew) throws Exception {

        if (isNew) {
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
            item.setDocumentationId(0L);
        }
        item.setValide(in.getValide() == null ? false : in.getValide());
        item.setTitle(null);

        // TODO use beans
        switch (type) {

        case WORKFLOW:
            if (inConfig == null) {
                item.setContent(in.getConfiguration());
            } else {
                if (SOSString.isEmpty(in.getConfiguration()) || in.getConfiguration().equals("{}")) {
                    item.setContent(in.getConfiguration());
                } else {
                    try {
                        Workflow w = (Workflow) JocInventory.convertJocContent2Deployable(in.getConfiguration(), type);
                        w.setPath(in.getPath());
                        item.setContent(Globals.objectMapper.writeValueAsString(w));
                    } catch (Throwable e) {
                        LOGGER.error(String.format("[%s]%s", in.getConfiguration(), e.toString()), e);
                        item.setContent(null);
                    }
                }
            }
            break;

        case AGENTCLUSTER:
            if (inConfig == null) {
                item.setContent(in.getConfiguration());
            } else {
                try {
                    AgentRef ar = (AgentRef) JocInventory.convertJocContent2Deployable(in.getConfiguration(), type);
                    ar.setPath(in.getPath());
                    item.setContent(Globals.objectMapper.writeValueAsString(ar));
                } catch (Throwable e) {
                    LOGGER.error(String.format("[%s]%s", in.getConfiguration(), e.toString()), e);
                    item.setContent(null);
                }
            }
            break;
        default:
            item.setContent(in.getConfiguration());// TODO parse for controller....
            break;
        }

        item.setContentJoc(in.getConfiguration());
        item.setDeployed(false);
        item.setModified(new Date());

        return item;
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, final RequestFilter in) throws Exception {
        SOSPermissionJocCockpit permissions = getPermissonsJocCockpit("", accessToken);
        boolean permission = permissions.getJS7Controller().getAdministration().getConfigurations().isEdit();
        return init(IMPL_PATH, in, accessToken, "", permission);
    }

}
