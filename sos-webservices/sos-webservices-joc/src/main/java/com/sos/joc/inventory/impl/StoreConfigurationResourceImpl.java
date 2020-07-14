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
import com.sos.joc.db.inventory.InventoryMeta.ConfigurationType;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IStoreConfigurationResource;
import com.sos.joc.model.inventory.common.ConfigurationItem;
import com.sos.schema.JsonValidator;

@Path("inventory")
public class StoreConfigurationResourceImpl extends JOCResourceImpl implements IStoreConfigurationResource {

    private static final String API_CALL = "./inventory/store";

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

    private Date store(ConfigurationItem in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            session.beginTransaction();

            DBItemInventoryConfiguration result = dbLayer.getConfiguration(in.getPath(), JocInventory.getType(in.getObjectType()));
            ConfigurationType type = null;
            if (result == null) {
                type = JocInventory.getType(in.getObjectType().name());
            } else {
                type = JocInventory.getType(result.getType());
            }
            if (type == null) {
                throw new Exception(String.format("unsupported store type=%s", in.getObjectType()));
            }

            if (result == null) {
                InventoryPath path = new InventoryPath(in.getPath());

                result = new DBItemInventoryConfiguration();
                result.setType(type);
                result.setPath(path.getPath());
                result.setName(path.getName());
                result.setFolder(path.getFolder());
                result.setParentFolder(path.getParentFolder());
                result.setTitle(null);
                result.setDocumentationId(0L);
                result.setCreated(new Date());
                result.setModified(new Date());

                InventoryAudit audit = new InventoryAudit(in);
                logAuditMessage(audit);
                audit.setStartTime(result.getCreated());
                DBItemJocAuditLog auditItem = storeAuditLogEntry(audit);
                if (auditItem != null) {
                    result.setAuditLogId(auditItem.getId());
                }

                session.save(result);
            } else {
                result.setModified(new Date());
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
                    session.save(w);
                } else {
                    w.setContentJoc(in.getConfiguration());
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
                    session.save(jc);
                } else {
                    jc.setContent(in.getConfiguration());
                    session.update(jc);
                }
                break;
            case AGENTCLUSTER:
                DBItemInventoryAgentCluster ac = dbLayer.getAgentCluster(result.getId());
                if (ac == null) {
                    ac = new DBItemInventoryAgentCluster();
                    ac.setConfigId(result.getId());
                    ac.setContent(in.getConfiguration());
                    session.save(ac);
                } else {
                    ac.setContent(in.getConfiguration());
                    session.update(ac);
                }
                break;
            case LOCK:
                DBItemInventoryLock l = dbLayer.getLock(result.getId());
                if (l == null) {
                    l = new DBItemInventoryLock();
                    l.setConfigId(result.getId());
                    l.setContent(in.getConfiguration());
                    session.save(l);
                } else {
                    l.setContent(in.getConfiguration());
                    session.update(l);
                }
                break;
            case JUNCTION:
                DBItemInventoryJunction j = dbLayer.getJunction(result.getId());
                if (j == null) {
                    j = new DBItemInventoryJunction();
                    j.setConfigId(result.getId());
                    j.setContent(in.getConfiguration());
                    session.save(j);
                } else {
                    j.setContent(in.getConfiguration());
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

            return result.getModified();
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, final ConfigurationItem in) throws Exception {
        SOSPermissionJocCockpit permissions = getPermissonsJocCockpit("", accessToken);
        boolean permission = permissions.getJobschedulerMaster().getAdministration().getConfigurations().isEdit();

        JOCDefaultResponse response = init(API_CALL, in, accessToken, "", permission);
        if (response == null) {
            String path = normalizePath(in.getPath());
            if (!folderPermissions.isPermittedForFolder(getParent(path))) {
                return accessDeniedResponse();
            }
        }
        return response;
    }

}
