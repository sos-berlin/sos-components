package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.meta.CalendarType;
import com.sos.joc.db.inventory.meta.ConfigurationType;
import com.sos.joc.db.inventory.items.InventoryTreeFolderItem;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IFolderResource;
import com.sos.joc.model.inventory.common.RequestFolder;
import com.sos.joc.model.inventory.common.ResponseFolder;
import com.sos.joc.model.inventory.common.ResponseFolderItem;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class FolderResourceImpl extends JOCResourceImpl implements IFolderResource {

    @Override
    public JOCDefaultResponse readFolder(final String accessToken, final byte[] inBytes) {
        try {
            JsonValidator.validateFailFast(inBytes, RequestFolder.class);
            RequestFolder in = Globals.objectMapper.readValue(inBytes, RequestFolder.class);

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

    private ResponseFolder readFolder(RequestFolder in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            Integer configType = null;
            Integer calendarType = null;
            if (in.getObjectType() != null) {
                try {
                    configType = ConfigurationType.fromValue(in.getObjectType().value()).intValue();
                } catch (Throwable e) {
                    try {
                        calendarType = CalendarType.fromValue(in.getObjectType().value()).intValue();
                        configType = ConfigurationType.CALENDAR.intValue();
                    } catch (Throwable ex) {
                    }
                }
            }
            session.beginTransaction();
            List<InventoryTreeFolderItem> items = dbLayer.getConfigurationsByFolder(in.getPath(), false, configType, calendarType);
            session.commit();

            ResponseFolder folder = new ResponseFolder();
            folder.setDeliveryDate(Date.from(Instant.now()));
            folder.setPath(in.getPath());

            if (items != null && items.size() > 0) {
                for (InventoryTreeFolderItem config : items) {
                    ConfigurationType type = JocInventory.getType(config.getType());
                    if (type != null) {
                        ResponseFolderItem item = new ResponseFolderItem();
                        item.setId(config.getId());
                        item.setName(config.getName());
                        item.setTitle(config.getTitle());
                        item.setValide(config.getValide());
                        item.setDeleted(config.getDeleted());
                        item.setDeployed(config.getDeployed());
                        item.setHasDeployments(JocInventory.long2boolean(config.getCountDeployments()));

                        switch (type) {
                        case WORKFLOW:
                            folder.getWorkflows().add(item);
                            break;
                        // case JOB:
                        // folder.getJobs().add(item);
                        // break;
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
                folder.setWorkflows(sort(folder.getWorkflows()));
                folder.setJobClasses(sort(folder.getJobClasses()));
                folder.setAgentClusters(sort(folder.getAgentClusters()));
                folder.setLocks(sort(folder.getLocks()));
                folder.setJunctions(sort(folder.getJunctions()));
                folder.setOrders(sort(folder.getOrders()));
                folder.setCalendars(sort(folder.getCalendars()));
            }
            return folder;
        } catch (Throwable e) {
            if (session != null && session.isTransactionOpened()) {
                Globals.rollback(session);
            }
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private Set<ResponseFolderItem> sort(Set<ResponseFolderItem> set) {
        if (set == null || set.size() == 0) {
            return set;
        }
        return set.stream().sorted(Comparator.comparing(ResponseFolderItem::getName)).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, final RequestFolder in) throws Exception {
        SOSPermissionJocCockpit permissions = getPermissonsJocCockpit("", accessToken);
        boolean permission = permissions.getInventory().getConfigurations().isEdit();

        JOCDefaultResponse response = init(IMPL_PATH, in, accessToken, "", permission);
        if (response == null) {
            if (JocInventory.ROOT_FOLDER.equals(in.getPath())) {
                if (!folderPermissions.isPermittedForFolder(in.getPath())) {
                    ResponseFolder entity = new ResponseFolder();
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
