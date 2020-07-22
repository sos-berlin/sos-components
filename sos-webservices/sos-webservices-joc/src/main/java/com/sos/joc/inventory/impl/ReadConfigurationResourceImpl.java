package com.sos.joc.inventory.impl;

import java.util.Date;

import javax.ws.rs.Path;

import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
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
import com.sos.joc.db.inventory.InventoryMeta.ConfigurationType;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IReadConfigurationResource;
import com.sos.joc.model.inventory.common.ConfigurationItem;
import com.sos.joc.model.inventory.common.Filter;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class ReadConfigurationResourceImpl extends JOCResourceImpl implements IReadConfigurationResource {

    @Override
    public JOCDefaultResponse read(final String accessToken, final byte[] inBytes) {
        try {
            JsonValidator.validateFailFast(inBytes, Filter.class);
            Filter in = Globals.objectMapper.readValue(inBytes, Filter.class);

            checkRequiredParameter("objectType", in.getObjectType());
            checkRequiredParameter("path", in.getPath());// for check permissions
            in.setPath(Globals.normalizePath(in.getPath()));

            JOCDefaultResponse response = checkPermissions(accessToken, in);
            if (response == null) {
                response = JOCDefaultResponse.responseStatus200(read(in));
            }
            return response;

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private ConfigurationItem read(Filter in) throws Exception {
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

            if (config == null) {
                throw new Exception(String.format("configuration not found: %s", SOSString.toString(in)));
            }
            ConfigurationType type = JocInventory.getType(config.getType());
            if (type == null) {
                throw new Exception(String.format("unsupported configuration type: %s (%s)", config.getType(), SOSHibernate.toString(config)));
            }

            ConfigurationItem item = new ConfigurationItem();
            item.setId(config.getId());
            item.setDeliveryDate(new Date());
            item.setPath(config.getPath());
            item.setConfigurationDate(config.getModified());
            item.setObjectType(in.getObjectType());

            switch (type) {
            case WORKFLOW:
                DBItemInventoryWorkflow w = dbLayer.getWorkflow(config.getId());
                if (w != null) {
                    item.setConfiguration(w.getContentJoc());
                }
                break;
            case JOB:
                DBItemInventoryWorkflowJob wj = dbLayer.getWorkflowJob(config.getId());
                if (wj != null) {
                    // item.setConfiguration(wj.getContent());
                }
                break;
            case JOBCLASS:
                DBItemInventoryJobClass jc = dbLayer.getJobClass(config.getId());
                if (jc != null) {
                    item.setConfiguration(jc.getContent());
                }
                break;
            case AGENTCLUSTER:
                DBItemInventoryAgentCluster ac = dbLayer.getAgentCluster(config.getId());
                if (ac != null) {
                    item.setConfiguration(ac.getContent());
                }
                break;
            case LOCK:
                DBItemInventoryLock l = dbLayer.getLock(config.getId());
                if (l != null) {
                    item.setConfiguration(l.getContent());
                }
                break;
            case JUNCTION:
                DBItemInventoryJunction j = dbLayer.getJunction(config.getId());
                if (j != null) {
                    item.setConfiguration(j.getContent());
                }
                break;
            case ORDER:
                DBItemInventoryWorkflowOrder wo = dbLayer.getWorkflowOrder(config.getId());
                if (wo != null) {
                    item.setConfiguration(wo.getContent());
                }
                break;
            case CALENDAR:
                DBItemInventoryCalendar c = dbLayer.getCalendar(config.getId());
                if (c != null) {
                    item.setConfiguration(c.getContent());
                }
                break;
            default:
                break;
            }
            session.commit();

            return item;
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, final Filter in) throws Exception {
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
