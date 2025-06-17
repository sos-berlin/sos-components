package com.sos.joc.inventory.impl.common;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.common.SyncStateHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationFilter;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.items.InventoryTreeFolderItem;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.RequestFolder;
import com.sos.joc.model.inventory.common.ResponseFolder;
import com.sos.joc.model.inventory.common.ResponseFolderItem;

import io.vavr.control.Either;
import js7.data_for_java.controller.JControllerState;

public abstract class AReadFolder extends JOCResourceImpl {

    private static final String INVENTORY_IMPL_PATH = JocInventory.getResourceImplPath("read/folder");
    private static final String INVENTORY_TRASH_IMPL_PATH = JocInventory.getResourceImplPath("trash/read/folder");
    private static final String DESCRIPTOR_TRASH_IMPL_PATH = "./descriptor/trash/read/folder";

    public ResponseFolder readFolder(RequestFolder in, String action) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(action);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            Set<Integer> configTypes = null;
            if (in.getObjectTypes() != null && !in.getObjectTypes().isEmpty()) {
                configTypes = in.getObjectTypes().stream().map(ConfigurationType::intValue).collect(Collectors.toSet());
            }

            boolean forTrash = INVENTORY_TRASH_IMPL_PATH.equals(action) || DESCRIPTOR_TRASH_IMPL_PATH.equals(action);
            List<InventoryTreeFolderItem> items = dbLayer.getConfigurationsByFolder(in.getPath(), in.getRecursive() == Boolean.TRUE, configTypes, in
                    .getOnlyValidObjects(), forTrash);

            ResponseFolder folder = new ResponseFolder();
            folder.setDeliveryDate(Date.from(Instant.now()));
            folder.setPath(in.getPath());

            boolean withSync = action.equals(INVENTORY_IMPL_PATH) && in.getControllerId() != null && !in.getControllerId().isEmpty();
            JControllerState currentstate = null;
            Map<Integer, Map<Long, String>> deloyedNames = Collections.emptyMap();
            if (withSync) {
                try {
                    DeployedConfigurationDBLayer deployedDbLayer = new DeployedConfigurationDBLayer(session);
                    DeployedConfigurationFilter deployedFilter = new DeployedConfigurationFilter();
                    deployedFilter.setControllerId(in.getControllerId());
                    Folder fld = new Folder();
                    fld.setFolder(in.getPath());
                    fld.setRecursive(in.getRecursive());
                    deployedFilter.setFolders(Collections.singleton(fld));
                    deployedFilter.setObjectTypes(configTypes);
                    currentstate = Proxy.of(in.getControllerId()).currentState();
                    deloyedNames = deployedDbLayer.getDeployedNames(deployedFilter);
                } catch (Exception e) {
                    ProblemHelper.postExceptionEventIfExist(Either.left(e), null, getJocError(), null);
                }
            }

            Set<Folder> permittedFolders = folderPermissions.getListOfFolders();

            if (items != null && !items.isEmpty()) {
                for (InventoryTreeFolderItem item : items) {
                    ResponseFolderItem config = item.toResponseFolderItem();
                    if (config == null) {// e.g. unknown type
                        continue;
                    }
                    // for in.getRecursive() == false: the folder permissions are already checked earlier
                    if (in.getRecursive() == Boolean.TRUE && !canAdd(config.getPath(), permittedFolders)) {
                        continue;
                    }
                    ConfigurationType type = config.getObjectType();
                    if (withSync) {
                        config.setSyncState(SyncStateHelper.getState(currentstate, config.getId(), type, deloyedNames.get(type.intValue())));
                    }
                    if (type != null) {
                        switch (type) {
                        case WORKFLOW:
                            folder.getWorkflows().add(config);
                            break;
                        case JOBTEMPLATE:
                            folder.getJobTemplates().add(config);
                            break;
                        case JOBCLASS:
                            folder.getJobClasses().add(config);
                            break;
                        case JOBRESOURCE:
                            folder.getJobResources().add(config);
                            break;
                        case LOCK:
                            folder.getLocks().add(config);
                            break;
                        case NOTICEBOARD:
                            folder.getNoticeBoards().add(config);
                            break;
                        case FILEORDERSOURCE:
                            folder.getFileOrderSources().add(config);
                            break;
                        case SCHEDULE:
                            config.setWorkflowNames(getWorkflowNames(dbLayer, config.getId(), forTrash));
                            folder.getSchedules().add(config);
                            break;
                        case INCLUDESCRIPT:
                            folder.getIncludeScripts().add(config);
                            break;
                        case WORKINGDAYSCALENDAR:
                        case NONWORKINGDAYSCALENDAR:
                            folder.getCalendars().add(config);
                            break;
                        case REPORT:
                            folder.getReports().add(config);
                            break;
                        case DEPLOYMENTDESCRIPTOR:
                             folder.getDeploymentDescriptors().add(config);
                            break;
                        case FOLDER:
                            // folder.getFolders().add(config);
                            break;
                        default:
                            break;
                        }
                    }
                }

                folder.setWorkflows(sort(folder.getWorkflows()));
                folder.setJobTemplates(sort(folder.getJobTemplates()));
                folder.setJobClasses(sort(folder.getJobClasses()));
                folder.setJobResources(sort(folder.getJobResources()));
                folder.setLocks(sort(folder.getLocks()));
                folder.setNoticeBoards(sort(folder.getNoticeBoards()));
                folder.setFileOrderSources(sort(folder.getFileOrderSources()));
                folder.setSchedules(sort(folder.getSchedules()));
                folder.setIncludeScripts(sort(folder.getIncludeScripts()));
                folder.setCalendars(sort(folder.getCalendars()));
                folder.setReports(sort(folder.getReports()));
                folder.setDeploymentDescriptors(sort(folder.getDeploymentDescriptors()));
                // folder.setFolders(sort(folder.getFolders()));
            }
            return folder;
        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private List<String> getWorkflowNames(InventoryDBLayer dbLayer, Long id, boolean forTrash) throws Exception {
        List<String> workflows = null;
        String json = dbLayer.getConfigurationContent(id, forTrash);
        if (!SOSString.isEmpty(json)) {
            Schedule schedule = Globals.objectMapper.readValue(json, Schedule.class);
            workflows = schedule.getWorkflowNames();
            if (workflows == null && schedule.getWorkflowName() != null) {
                workflows = Collections.singletonList(schedule.getWorkflowName());
            }
        }
        return workflows;
    }

    private Set<ResponseFolderItem> sort(Set<ResponseFolderItem> set) {
        if (set == null || set.isEmpty()) {
            return set;
        }
        return set.stream().sorted(Comparator.comparing(ResponseFolderItem::getPath)).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public JOCDefaultResponse checkPermissions(final String accessToken, final RequestFolder in, boolean permission) throws Exception {
        JOCDefaultResponse response = initPermissions(null, permission);
        if (response == null) {
            // for in.getRecursive() == TRUE: folder permissions are checked later
            if (JocInventory.ROOT_FOLDER.equals(in.getPath())) {
                if (in.getRecursive() != Boolean.TRUE && !folderPermissions.isPermittedForFolder(in.getPath())) {
                    ResponseFolder entity = new ResponseFolder();
                    entity.setDeliveryDate(Date.from(Instant.now()));
                    entity.setPath(in.getPath());
                    response = responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
                }

            } else {
                if (in.getRecursive() != Boolean.TRUE && !folderPermissions.isPermittedForFolder(in.getPath())) {
                    response = accessDeniedResponse();
                }
            }
        }
        return response;
    }
}
