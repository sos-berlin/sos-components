package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.InventoryMeta.ConfigurationType;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IFolderResource;
import com.sos.joc.model.inventory.common.Filter;
import com.sos.joc.model.inventory.common.Folder;
import com.sos.joc.model.inventory.common.FolderItem;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class FolderResourceImpl extends JOCResourceImpl implements IFolderResource {

    @Override
    public JOCDefaultResponse readFolder(final String accessToken, final byte[] inBytes) {
        try {
            JsonValidator.validateFailFast(inBytes, Filter.class);
            Filter in = Globals.objectMapper.readValue(inBytes, Filter.class);

            checkRequiredParameter("path", in.getPath());
            in.setPath(normalizeFolder(in.getPath()));

            JOCDefaultResponse response = checkPermissions(accessToken, in);
            if (response == null) {
                response = JOCDefaultResponse.responseStatus200(readFolder(in));
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private Folder readFolder(Filter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            session.beginTransaction();
            List<DBItemInventoryConfiguration> result = dbLayer.getConfigurationsByFolder(in.getPath(), false, JocInventory.getType(in
                    .getObjectType()));
            session.commit();

            Folder folder = new Folder();
            folder.setDeliveryDate(Date.from(Instant.now()));
            folder.setPath(in.getPath());

            if (result != null && result.size() > 0) {
                for (DBItemInventoryConfiguration configuration : result) {
                    ConfigurationType type = JocInventory.getType(configuration.getType());
                    if (type != null) {
                        FolderItem item = new FolderItem();
                        item.setId(configuration.getId());
                        item.setName(configuration.getName());
                        item.setTitle(configuration.getTitle());
                        switch (type) {
                        case WORKFLOW:
                            folder.getWorkflows().add(item);
                            break;
                        case JOB:
                            folder.getJobs().add(item);
                            break;
                        case JOBCLASS:
                            folder.getJobClasses().add(item);
                            break;
                        case AGENTCLUSTER:
                            folder.getAgentClusters().add(item);
                            break;
                        case LOCK:
                            folder.getLocks().add(item);
                            break;
                        case JUNCTION:
                            folder.getJunctions().add(item);
                            break;
                        case ORDER:
                            folder.getOrders().add(item);
                            break;
                        case CALENDAR:
                            folder.getCalendars().add(item);
                            break;
                        default:
                            break;
                        }
                    }
                }
            }
            return folder;
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
            if (JocInventory.ROOT_FOLDER.equals(in.getPath())) {
                if (!folderPermissions.isPermittedForFolder(in.getPath())) {
                    Folder entity = new Folder();
                    entity.setDeliveryDate(Date.from(Instant.now()));
                    entity.setPath(in.getPath());
                    response = JOCDefaultResponse.responseStatus200(entity);
                }

            } else {
                if (!folderPermissions.isPermittedForFolder(in.getPath())) {
                    response = accessDeniedResponse();
                }
            }
        }
        return response;
    }
}
