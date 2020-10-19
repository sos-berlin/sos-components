package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
import com.sos.joc.db.inventory.items.InventoryTreeFolderItem;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IFolderResource;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.RequestFolder;
import com.sos.joc.model.inventory.common.ResponseFolder;
import com.sos.joc.model.inventory.common.ResponseFolderItem;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class FolderResourceImpl extends JOCResourceImpl implements IFolderResource {

    @Override
    public JOCDefaultResponse readFolder(final String accessToken, final byte[] inBytes) {
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, RequestFolder.class);
            RequestFolder in = Globals.objectMapper.readValue(inBytes, RequestFolder.class);

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

            Set<Integer> configTypes = null;
            if (in.getObjectTypes() != null && !in.getObjectTypes().isEmpty()) {
                configTypes = in.getObjectTypes().stream().map(ConfigurationType::intValue).collect(Collectors.toSet());
            }
            
            session.beginTransaction();
            List<InventoryTreeFolderItem> items = dbLayer.getConfigurationsByFolder(in.getPath(), in.getRecursive() == Boolean.TRUE, configTypes, in
                    .getOnlyValidObjects());
            session.commit();

            ResponseFolder folder = new ResponseFolder();
            folder.setDeliveryDate(Date.from(Instant.now()));
            folder.setPath(in.getPath());
            
            Map<String, InventoryTreeFolderItem> orders = new HashMap<>();
            Set<ResponseFolderItem> workflows = new HashSet<>();

            if (items != null && !items.isEmpty()) {
                for (InventoryTreeFolderItem config : items) {
                    ConfigurationType type = config.getObjectType();
                    if (type != null) {
                        switch (type) {
                        case WORKFLOW:
                            workflows.add(config);
                            //folder.getWorkflows().add(config);
                            break;
                        case JOB:
                            folder.getJobs().add(config);
                            break;
                        case JOBCLASS:
                            folder.getJobClasses().add(config);
                            break;
                        case AGENTCLUSTER:
                            folder.getAgentClusters().add(config);
                            break;
                        case LOCK:
                            folder.getLocks().add(config);
                            break;
                        case JUNCTION:
                            folder.getJunctions().add(config);
                            break;
                        case ORDER:
                            if (config.getWorkflowPath() != null) {
                                orders.put(config.getWorkflowPath(), config);
                            }
                            folder.getOrders().add(config);
                            break;
                        case WORKINGDAYSCALENDAR:
                        case NONWORKINGDAYSCALENDAR:
                            folder.getCalendars().add(config);
                            break;
                        default:
                            break;
                        }
                    }
                }
                
                // put OrderTemplate to Workflow
                workflows.stream().map(item -> {
                    item.setOrder(orders.remove(item.getPath()));
                    return item;
                }).collect(Collectors.toSet());
                
                folder.setWorkflows(sort(workflows));
                folder.setJobs(sort(folder.getJobs()));
                folder.setJobClasses(sort(folder.getJobClasses()));
                folder.setAgentClusters(sort(folder.getAgentClusters()));
                folder.setLocks(sort(folder.getLocks()));
                folder.setJunctions(sort(folder.getJunctions()));
                folder.setOrders(sort(folder.getOrders()));
                folder.setCalendars(sort(folder.getCalendars()));
            }
            return folder;
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private Set<ResponseFolderItem> sort(Set<ResponseFolderItem> set) {
        if (set == null || set.size() == 0) {
            return set;
        }
        return set.stream().sorted(Comparator.comparing(ResponseFolderItem::getPath)).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, final RequestFolder in) throws Exception {
        SOSPermissionJocCockpit permissions = getPermissonsJocCockpit("", accessToken);
        boolean permission = permissions.getInventory().getConfigurations().isEdit();

        JOCDefaultResponse response = initPermissions(null, permission);
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
