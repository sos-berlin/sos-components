package com.sos.joc.inventory.impl;

import java.util.Date;

import javax.ws.rs.Path;

import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.InventoryAudit;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.JocInventory.InventoryPath;
import com.sos.joc.db.inventory.DBItemInventoryAgentCluster;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryJobClass;
import com.sos.joc.db.inventory.DBItemInventoryJunction;
import com.sos.joc.db.inventory.DBItemInventoryLock;
import com.sos.joc.db.inventory.DBItemInventoryWorkflow;
import com.sos.joc.db.inventory.DBItemInventoryWorkflowJob;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.InventoryMeta.AgentClusterSchedulingType;
import com.sos.joc.db.inventory.InventoryMeta.ConfigurationType;
import com.sos.joc.db.inventory.InventoryMeta.LockType;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IStoreConfigurationResource;
import com.sos.joc.model.inventory.common.ConfigurationItem;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class StoreConfigurationResourceImpl extends JOCResourceImpl implements IStoreConfigurationResource {

    @Override
    public JOCDefaultResponse store(final String accessToken, final byte[] inBytes) {
        try {
            JsonValidator.validateFailFast(inBytes, ConfigurationItem.class);
            ConfigurationItem in = Globals.objectMapper.readValue(inBytes, ConfigurationItem.class);

            checkRequiredParameter("objectType", in.getObjectType());
            checkRequiredParameter("path", in.getPath());

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

            DBItemInventoryConfiguration result = null;
            if (in.getId() != null && in.getId() > 0L) {
                result = dbLayer.getConfiguration(in.getId(), JocInventory.getType(in.getObjectType()));
            }
            if (result == null) {// TODO temp
                result = dbLayer.getConfiguration(in.getPath(), JocInventory.getType(in.getObjectType()));
            }

            ConfigurationType type = null;
            if (result == null) {
                type = JocInventory.getType(in.getObjectType().name());
            } else {
                type = JocInventory.getType(result.getType());
            }
            if (type == null) {
                throw new Exception(String.format("unsupported configuration type=%s", in.getObjectType()));
            }

            if (result == null) {
                result = new DBItemInventoryConfiguration();
                result.setType(type);
                result = setProperties(in, result, type);
                result.setCreated(new Date());

                InventoryAudit audit = new InventoryAudit(in);
                logAuditMessage(audit);
                audit.setStartTime(result.getCreated());
                DBItemJocAuditLog auditItem = storeAuditLogEntry(audit);
                if (auditItem != null) {
                    result.setAuditLogId(auditItem.getId());
                }

                session.save(result);
            } else {
                result = setProperties(in, result, type);
                session.update(result);
            }

            switch (type) {
            case WORKFLOW:
                // TODO setContent, workflowJobs etc
                DBItemInventoryWorkflow w = dbLayer.getWorkflow(result.getId());
                if (w == null) {
                    w = new DBItemInventoryWorkflow();
                    w.setConfigId(result.getId());
                    w.setContentJoc(in.getConfiguration());

                    w.setContent(in.getConfiguration());// TODO
                    // w.setContentSigned(val);//TODO
                    session.save(w);
                } else {
                    w.setContentJoc(in.getConfiguration());

                    // w.setContent(in.getConfiguration());// TODO
                    // w.setContentSigned(val); //TODO
                    session.update(w);
                }
                break;
            case JOB:
                DBItemInventoryWorkflowJob wj = dbLayer.getWorkflowJob(result.getId());
                if (wj != null) {
                    // item.setConfiguration(wj.getContent());
                }
                break;
            case JOBCLASS:
                DBItemInventoryJobClass jc = dbLayer.getJobClass(result.getId());
                if (jc == null) {
                    jc = new DBItemInventoryJobClass();
                    jc.setConfigId(result.getId());
                    jc.setContent(in.getConfiguration());

                    jc.setMaxProcesses(30L);// TODO
                    session.save(jc);
                } else {
                    jc.setContent(in.getConfiguration());

                    // jc.setMaxProcesses(30L);// TODO
                    session.update(jc);
                }
                break;
            case AGENTCLUSTER:
                DBItemInventoryAgentCluster ac = dbLayer.getAgentCluster(result.getId());
                if (ac == null) {
                    ac = new DBItemInventoryAgentCluster();
                    ac.setConfigId(result.getId());
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
                DBItemInventoryLock l = dbLayer.getLock(result.getId());
                if (l == null) {
                    l = new DBItemInventoryLock();
                    l.setConfigId(result.getId());
                    l.setContent(in.getConfiguration());

                    l.setType(LockType.EXCLUSIVE); // TODO
                    l.setMaxNonExclusive(0L);// TODO
                    session.save(l);
                } else {
                    l.setContent(in.getConfiguration());

                    // l.setType(LockType.EXCLUSIVE); // TODO
                    // l.setMaxNonExclusive(0L);// TODO
                    session.update(l);
                }
                break;
            case JUNCTION:
                DBItemInventoryJunction j = dbLayer.getJunction(result.getId());
                if (j == null) {
                    j = new DBItemInventoryJunction();
                    j.setConfigId(result.getId());
                    j.setContent(in.getConfiguration());

                    j.setLifetime("xxxx");// TODO
                    session.save(j);
                } else {
                    j.setContent(in.getConfiguration());

                    // j.setLifetime("xxxx");// TODO
                    session.update(j);
                }
                break;
            case ORDER:
                break;
            case CALENDAR:
                break;
            default:
                break;
            }
            session.commit();

            ConfigurationItem item = new ConfigurationItem();
            item.setId(result.getId());
            item.setDeliveryDate(new Date());
            item.setPath(result.getPath());
            item.setConfigurationDate(result.getModified());
            item.setObjectType(in.getObjectType());

            return item;
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
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
