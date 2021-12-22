package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
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
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IFolderResource;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.RequestFolder;
import com.sos.joc.model.inventory.common.ResponseFolder;
import com.sos.joc.model.inventory.common.ResponseFolderItem;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.data_for_java.controller.JControllerState;

@Path(JocInventory.APPLICATION_PATH)
public class FolderResourceImpl extends JOCResourceImpl implements IFolderResource {

    @Override
    public JOCDefaultResponse readFolder(final String accessToken, final byte[] inBytes) {
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, RequestFolder.class);
            RequestFolder in = Globals.objectMapper.readValue(inBytes, RequestFolder.class);

            in.setPath(normalizeFolder(in.getPath()));
            boolean permission = getJocPermissions(accessToken).getInventory().getView();
            JOCDefaultResponse response = checkPermissions(accessToken, in, permission);
            if (response == null) {
                response = JOCDefaultResponse.responseStatus200(readFolder(in, IMPL_PATH));
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse readTrashFolder(final String accessToken, final byte[] inBytes) {
        try {
            initLogging(TRASH_IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, RequestFolder.class);
            RequestFolder in = Globals.objectMapper.readValue(inBytes, RequestFolder.class);

            in.setPath(normalizeFolder(in.getPath()));
            boolean permission = getJocPermissions(accessToken).getInventory().getView();
            JOCDefaultResponse response = checkPermissions(accessToken, in, permission);
            if (response == null) {
                response = JOCDefaultResponse.responseStatus200(readFolder(in, TRASH_IMPL_PATH));
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private ResponseFolder readFolder(RequestFolder in, String action) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(action);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            Set<Integer> configTypes = null;
            if (in.getObjectTypes() != null && !in.getObjectTypes().isEmpty()) {
                configTypes = in.getObjectTypes().stream().map(ConfigurationType::intValue).collect(Collectors.toSet());
            }

            List<InventoryTreeFolderItem> items = dbLayer.getConfigurationsByFolder(in.getPath(), in.getRecursive() == Boolean.TRUE, configTypes, in
                    .getOnlyValidObjects(), TRASH_IMPL_PATH.equals(action));

            ResponseFolder folder = new ResponseFolder();
            folder.setDeliveryDate(Date.from(Instant.now()));
            folder.setPath(in.getPath());
            
            boolean withSync = action.equals(IMPL_PATH) && in.getControllerId() != null && !in.getControllerId().isEmpty();
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

            if (items != null && !items.isEmpty()) {
                for (InventoryTreeFolderItem item : items) {
                    ResponseFolderItem config = item.toResponseFolderItem();
                    if (config == null) {// e.g. unknown type
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
                        case JOB:
                            folder.getJobs().add(config);
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
                            folder.getSchedules().add(config);
                            break;
                        case INCLUDESCRIPT:
                            folder.getIncludeScripts().add(config);
                            break;
                        case WORKINGDAYSCALENDAR:
                        case NONWORKINGDAYSCALENDAR:
                            folder.getCalendars().add(config);
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
                folder.setJobs(sort(folder.getJobs()));
                folder.setJobClasses(sort(folder.getJobClasses()));
                folder.setJobResources(sort(folder.getJobResources()));
                folder.setLocks(sort(folder.getLocks()));
                folder.setNoticeBoards(sort(folder.getNoticeBoards()));
                folder.setFileOrderSources(sort(folder.getFileOrderSources()));
                folder.setSchedules(sort(folder.getSchedules()));
                folder.setIncludeScripts(sort(folder.getIncludeScripts()));
                folder.setCalendars(sort(folder.getCalendars()));
                // folder.setFolders(sort(folder.getFolders()));
            }
            return folder;
        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private Set<ResponseFolderItem> sort(Set<ResponseFolderItem> set) {
        if (set == null || !set.isEmpty()) {
            return set;
        }
        return set.stream().sorted(Comparator.comparing(ResponseFolderItem::getPath)).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, final RequestFolder in, boolean permission) throws Exception {
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
