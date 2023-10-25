package com.sos.joc.inventory.impl.common;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.common.SyncStateHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationFilter;
import com.sos.joc.db.inventory.InventoryTagDBLayer;
import com.sos.joc.db.inventory.items.InventoryTreeFolderItem;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.RequestTag;
import com.sos.joc.model.inventory.common.ResponseFolderItem;
import com.sos.joc.model.inventory.common.ResponseTag;

import io.vavr.control.Either;
import js7.data_for_java.controller.JControllerState;

public abstract class AReadTag extends JOCResourceImpl {

    private static final String INVENTORY_IMPL_PATH = JocInventory.getResourceImplPath("read/tag");
    private static final String INVENTORY_TRASH_IMPL_PATH = JocInventory.getResourceImplPath("trash/read/tag");
    //private static final String DESCRIPTOR_TRASH_IMPL_PATH = "./descriptor/trash/read/tag";

    public ResponseTag readTag(RequestTag in, String action) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(action);
            InventoryTagDBLayer dbTagLayer = new InventoryTagDBLayer(session);
            
            boolean forTrash = INVENTORY_TRASH_IMPL_PATH.equals(action);
            
            Set<Integer> types = Collections.singleton(ConfigurationType.WORKFLOW.intValue());
            List<InventoryTreeFolderItem> items = dbTagLayer.getConfigurationsByTag(in.getTag(), types, in.getOnlyValidObjects(), forTrash);
            
            ResponseTag tag = new ResponseTag();
            tag.setDeliveryDate(Date.from(Instant.now()));
            tag.setTag(in.getTag());

            boolean withSync = action.equals(INVENTORY_IMPL_PATH) && in.getControllerId() != null && !in.getControllerId().isEmpty();
            JControllerState currentstate = null;
            Map<Integer, Map<Long, String>> deloyedNames = Collections.emptyMap();
            if (withSync && items != null && !items.isEmpty()) {
                try {
                    DeployedConfigurationDBLayer deployedDbLayer = new DeployedConfigurationDBLayer(session);
                    DeployedConfigurationFilter deployedFilter = new DeployedConfigurationFilter();
                    deployedFilter.setControllerId(in.getControllerId());
                    deployedFilter.setObjectTypes(types);
                    deployedFilter.setNames(items.stream().map(InventoryTreeFolderItem::getName).filter(Objects::nonNull).collect(Collectors.toSet()));
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
                    if (!canAdd(config.getPath(), permittedFolders)) {
                        continue;
                    }
                    ConfigurationType type = config.getObjectType();
                    if (withSync) {
                        config.setSyncState(SyncStateHelper.getState(currentstate, config.getId(), type, deloyedNames.get(type.intValue())));
                    }
                    if (type != null) {
                        switch (type) {
                        case WORKFLOW:
                            tag.getWorkflows().add(config);
                            break;
//                        case JOBTEMPLATE:
//                            tag.getJobTemplates().add(config);
//                            break;
//                        case JOBCLASS:
//                            tag.getJobClasses().add(config);
//                            break;
//                        case JOBRESOURCE:
//                            tag.getJobResources().add(config);
//                            break;
//                        case LOCK:
//                            tag.getLocks().add(config);
//                            break;
//                        case NOTICEBOARD:
//                            tag.getNoticeBoards().add(config);
//                            break;
//                        case FILEORDERSOURCE:
//                            tag.getFileOrderSources().add(config);
//                            break;
//                        case SCHEDULE:
//                            config.setWorkflowNames(getWorkflowNames(dbLayer, config.getId(), forTrash));
//                            tag.getSchedules().add(config);
//                            break;
//                        case INCLUDESCRIPT:
//                            tag.getIncludeScripts().add(config);
//                            break;
//                        case WORKINGDAYSCALENDAR:
//                        case NONWORKINGDAYSCALENDAR:
//                            tag.getCalendars().add(config);
//                            break;
//                        case DEPLOYMENTDESCRIPTOR:
//                            tag.getDeploymentDescriptors().add(config);
//                            break;
                        default:
                            break;
                        }
                    }
                }

                tag.setWorkflows(sort(tag.getWorkflows()));
//                tag.setJobTemplates(sort(folder.getJobTemplates()));
//                tag.setJobClasses(sort(folder.getJobClasses()));
//                tag.setJobResources(sort(folder.getJobResources()));
//                tag.setLocks(sort(folder.getLocks()));
//                tag.setNoticeBoards(sort(folder.getNoticeBoards()));
//                tag.setFileOrderSources(sort(folder.getFileOrderSources()));
//                tag.setSchedules(sort(folder.getSchedules()));
//                tag.setIncludeScripts(sort(folder.getIncludeScripts()));
//                tag.setCalendars(sort(folder.getCalendars()));
//                tag.setDeploymentDescriptors(sort(folder.getDeploymentDescriptors()));
            }
            return tag;
        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

//    private List<String> getWorkflowNames(InventoryDBLayer dbLayer, Long id, boolean forTrash) throws Exception {
//        List<String> workflows = null;
//        String json = dbLayer.getConfigurationContent(id, forTrash);
//        if (!SOSString.isEmpty(json)) {
//            Schedule schedule = Globals.objectMapper.readValue(json, Schedule.class);
//            workflows = schedule.getWorkflowNames();
//            if (workflows == null && schedule.getWorkflowName() != null) {
//                workflows = Collections.singletonList(schedule.getWorkflowName());
//            }
//        }
//        return workflows;
//    }

    private Set<ResponseFolderItem> sort(Set<ResponseFolderItem> set) {
        if (set == null || set.isEmpty()) {
            return set;
        }
        return set.stream().sorted(Comparator.comparing(ResponseFolderItem::getPath)).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
