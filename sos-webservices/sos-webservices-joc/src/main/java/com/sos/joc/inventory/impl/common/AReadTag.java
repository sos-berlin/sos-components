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
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.common.SyncStateHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.tag.GroupedTag;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationFilter;
import com.sos.joc.db.inventory.IDBItemTag;
import com.sos.joc.db.inventory.common.ATagDBLayer;
import com.sos.joc.db.inventory.items.InventoryTreeFolderItem;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.RequestTag;
import com.sos.joc.model.inventory.common.ResponseFolderItem;
import com.sos.joc.model.inventory.common.ResponseTag;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.data_for_java.controller.JControllerState;

public abstract class AReadTag extends JOCResourceImpl {
    
    public JOCDefaultResponse readTag(final String action, boolean forTrash, ATagDBLayer<? extends IDBItemTag> dbLayer, final String accessToken,
            byte[] inBytes) {
        try {
            inBytes = initLogging(action, inBytes, accessToken, CategoryType.INVENTORY);
            JsonValidator.validateFailFast(inBytes, RequestTag.class);
            RequestTag in = Globals.objectMapper.readValue(inBytes, RequestTag.class);

            JOCDefaultResponse response = initPermissions(null, getBasicJocPermissions(accessToken).getInventory().getView());
            if (response == null) {
                ResponseTag tag = readTag(in, action, forTrash, dbLayer);
                response = responseStatus200(Globals.objectMapper.writeValueAsBytes(tag));
            }
            return response;
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    private ResponseTag readTag(RequestTag in, String action, boolean forTrash, ATagDBLayer<? extends IDBItemTag> dbLayer) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(action);
            dbLayer.setSession(session);
            
            Set<Integer> types = Collections.singleton(ConfigurationType.WORKFLOW.intValue());
            GroupedTag groupedTag = new GroupedTag(in.getTag());
            List<InventoryTreeFolderItem> items = dbLayer.getConfigurationsByTag(groupedTag.getTag(), types, in.getOnlyValidObjects(), forTrash);
            
            ResponseTag tag = new ResponseTag();
            tag.setDeliveryDate(Date.from(Instant.now()));
            tag.setTag(groupedTag.toString());

            boolean withSync = !forTrash && in.getControllerId() != null && !in.getControllerId().isEmpty();
            JControllerState currentstate = null;
            Map<Integer, Map<Long, String>> deloyedNames = Collections.emptyMap();
            if (withSync && items != null && !items.isEmpty()) {
                try {
                    DeployedConfigurationDBLayer deployedDbLayer = new DeployedConfigurationDBLayer(session);
                    DeployedConfigurationFilter deployedFilter = new DeployedConfigurationFilter();
                    deployedFilter.setControllerId(in.getControllerId());
                    deployedFilter.setObjectTypes(types);
                    deployedFilter.setTags(Collections.singleton(in.getTag()));
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
                    config.setWorkflowNames(null);
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
