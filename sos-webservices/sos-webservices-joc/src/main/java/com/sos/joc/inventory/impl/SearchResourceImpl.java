package com.sos.joc.inventory.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSReflection;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.inventory.InventorySearchDBLayer;
import com.sos.joc.db.inventory.items.InventorySearchItem;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.ISearchResource;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.search.RequestSearchAdvancedItem;
import com.sos.joc.model.inventory.search.RequestSearchFilter;
import com.sos.joc.model.inventory.search.ResponseSearch;
import com.sos.joc.model.inventory.search.ResponseSearchItem;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class SearchResourceImpl extends JOCResourceImpl implements ISearchResource {

    @Override
    public JOCDefaultResponse post(final String accessToken, final byte[] inBytes) {
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, RequestSearchFilter.class);
            RequestSearchFilter in = Globals.objectMapper.readValue(inBytes, RequestSearchFilter.class);

            boolean permission = getJocPermissions(accessToken).getInventory().getView();
            JOCDefaultResponse response = checkPermissions(accessToken, in, permission);
            if (response != null) {
                return response;
            }

            ResponseSearch answer = new ResponseSearch();
            answer.setResults(SOSReflection.isEmpty(in.getAdvanced()) ? getBasicSearch(in) : getAdvancedSearch(in));
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private List<ResponseSearchItem> getBasicSearch(final RequestSearchFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventorySearchDBLayer dbLayer = new InventorySearchDBLayer(session);

            ConfigurationType objectType = ConfigurationType.valueOf(in.getReturnType().value());
            List<InventorySearchItem> items = null;
            if (in.getDeployedOrReleased() != null && in.getDeployedOrReleased().booleanValue()) {
                items = dbLayer.getBasicSearchDeployedOrReleasedConfigurations(objectType, in.getSearch(), in.getFolders(), in.getControllerId());
            } else {
                items = dbLayer.getBasicSearchInventoryConfigurations(objectType, in.getSearch(), in.getFolders());
            }

            List<ResponseSearchItem> r = new ArrayList<>();
            if (items != null) {
                List<InventorySearchItem> sorted = items.stream().sorted(Comparator.comparing(InventorySearchItem::getPath)).collect(Collectors
                        .toList());
                for (InventorySearchItem item : sorted) {
                    ResponseSearchItem ri = new ResponseSearchItem();
                    ri.setId(item.getId());
                    ri.setPath(item.getPath());
                    ri.setName(item.getName());
                    ri.setObjectType(objectType);
                    ri.setTitle(item.getTitle());
                    ri.setControllerId(item.getControllerId());
                    ri.setValid(item.isValid());
                    ri.setDeleted(item.isDeleted());
                    ri.setDeployed(item.isDeployed());
                    ri.setReleased(item.isReleased());
                    ri.setHasDeployments(item.getCountDeployed().intValue() > 0);
                    ri.setHasReleases(item.getCountReleased().intValue() > 0);
                    r.add(ri);
                }
            }
            return r;
        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private List<ResponseSearchItem> getAdvancedSearch(final RequestSearchFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            adjustAdvanced(in);

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventorySearchDBLayer dbLayer = new InventorySearchDBLayer(session);

            ConfigurationType objectType = ConfigurationType.valueOf(in.getReturnType().value());
            List<InventorySearchItem> items = null;
            boolean deployedOrReleased = in.getDeployedOrReleased() != null && in.getDeployedOrReleased().booleanValue();
            if (deployedOrReleased) {
                items = dbLayer.getAdvancedSearchDeployedOrReleasedConfigurations(objectType, in.getSearch(), in.getFolders(), in.getAdvanced(), in
                        .getControllerId());
            } else {
                items = dbLayer.getAdvancedSearchInventoryConfigurations(objectType, in.getSearch(), in.getFolders(), in.getAdvanced());
            }

            List<ResponseSearchItem> r = new ArrayList<>();
            if (items != null) {
                List<InventorySearchItem> sorted = items.stream().sorted(Comparator.comparing(InventorySearchItem::getPath)).collect(Collectors
                        .toList());
                RequestSearchAdvancedItem workflowAdvanced = cloneAdvanced4WorkflowSearch(in);
                for (InventorySearchItem item : sorted) {
                    boolean checkWorkflow = false;
                    switch (objectType) {
                    case JOBRESOURCE:
                        workflowAdvanced.setJobResources(item.getName());
                        checkWorkflow = true;
                        break;
                    case BOARD:
                        workflowAdvanced.setBoards(item.getName());
                        checkWorkflow = true;
                        break;
                    case LOCK:
                        workflowAdvanced.setLock(item.getName());
                        checkWorkflow = true;
                        break;
                    default:
                        break;
                    }
                    if (checkWorkflow) {
                        List<InventorySearchItem> wi = null;
                        if (deployedOrReleased) {
                            wi = dbLayer.getAdvancedSearchDeployedOrReleasedConfigurations(ConfigurationType.WORKFLOW, in.getAdvanced().getWorkflow(),
                                    null, workflowAdvanced, in.getControllerId());
                        } else {
                            wi = dbLayer.getAdvancedSearchInventoryConfigurations(ConfigurationType.WORKFLOW, in.getAdvanced().getWorkflow(), null,
                                    workflowAdvanced);
                        }
                        if (wi == null || wi.size() == 0) {
                            continue;

                        }
                    }

                    ResponseSearchItem ri = new ResponseSearchItem();
                    ri.setId(item.getId());
                    ri.setPath(item.getPath());
                    ri.setName(item.getName());
                    ri.setObjectType(objectType);
                    ri.setTitle(item.getTitle());
                    ri.setControllerId(item.getControllerId());
                    ri.setValid(item.isValid());
                    ri.setDeleted(item.isDeleted());
                    ri.setDeployed(item.isDeployed());
                    ri.setReleased(item.isReleased());
                    ri.setHasDeployments(item.getCountDeployed().intValue() > 0);
                    ri.setHasReleases(item.getCountReleased().intValue() > 0);
                    r.add(ri);
                }
            }
            return r;
        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private void adjustAdvanced(final RequestSearchFilter in) {
        if (in.getAdvanced() == null) {
            return;
        }
        switch (in.getReturnType()) {
        case WORKFLOW:
            in.getAdvanced().setWorkflow(null);
            break;
        case FILEORDERSOURCE:
            in.getAdvanced().setFileOrderSource(null);
            break;
        case JOBRESOURCE:
            in.getAdvanced().setJobResources(null);
            break;
        case BOARD:
            in.getAdvanced().setBoards(null);
            break;
        case LOCK:
            in.getAdvanced().setLock(null);
            break;
        case SCHEDULE:
            in.getAdvanced().setSchedule(null);
            break;
        }
    }

    private RequestSearchAdvancedItem cloneAdvanced4WorkflowSearch(final RequestSearchFilter in) {
        if (in.getAdvanced() == null) {
            return null;
        }
        RequestSearchAdvancedItem item = new RequestSearchAdvancedItem();
        item.setAgentName(in.getAdvanced().getAgentName());
        item.setArgumentName(in.getAdvanced().getArgumentName());
        item.setArgumentValue(in.getAdvanced().getArgumentValue());
        item.setBoards(in.getAdvanced().getBoards());
        item.setFileOrderSource(in.getAdvanced().getFileOrderSource());
        item.setJobCountFrom(in.getAdvanced().getJobCountFrom());
        item.setJobCountTo(in.getAdvanced().getJobCountTo());
        item.setJobCriticality(in.getAdvanced().getJobCriticality());
        item.setJobName(in.getAdvanced().getJobName());
        item.setJobResources(in.getAdvanced().getJobResources());
        item.setLock(in.getAdvanced().getLock());
        item.setSchedule(in.getAdvanced().getSchedule());
        item.setWorkflow(null);
        return item;
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, final RequestSearchFilter in, boolean permission) throws Exception {
        JOCDefaultResponse response = initPermissions(in.getControllerId(), getPermitted(accessToken, in));
        if (response == null) {
            if (in.getFolders() != null) {
                for (String folder : in.getFolders()) {
                    if (!folderPermissions.isPermittedForFolder(folder)) {
                        return accessDeniedResponse();
                    }
                }
            }
        }
        return response;
    }

    private boolean getPermitted(String accessToken, RequestSearchFilter in) {
        String controllerId = in.getControllerId();
        Set<String> allowedControllers = Collections.emptySet();
        boolean permitted = false;
        if (controllerId == null || controllerId.isEmpty()) {
            controllerId = "";
            allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(availableController -> getControllerPermissions(
                    availableController, accessToken).getOrders().getView()).collect(Collectors.toSet());
            permitted = !allowedControllers.isEmpty();
            if (allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
                allowedControllers = Collections.emptySet();
            }
        } else {
            allowedControllers = Collections.singleton(controllerId);
            permitted = getControllerPermissions(controllerId, accessToken).getOrders().getView();
        }
        return permitted;
    }
}
