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
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.inventory.InventorySearchDBLayer;
import com.sos.joc.db.inventory.items.InventorySearchItem;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.ISearchResource;
import com.sos.joc.model.inventory.search.RequestSearchAdvancedItem;
import com.sos.joc.model.inventory.search.RequestSearchFilter;
import com.sos.joc.model.inventory.search.RequestSearchReturnType;
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

            boolean controllerPermissions = false;
            if (!SOSString.isEmpty(in.getControllerId())) {
                controllerPermissions = getControllerPermissions(in.getControllerId(), accessToken).getWorkflows().getView();
            }
            boolean permission = getJocPermissions(accessToken).getInventory().getView() || controllerPermissions;
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

            List<InventorySearchItem> items = null;
            if (in.getDeployedOrReleased() != null && in.getDeployedOrReleased().booleanValue()) {
                items = dbLayer.getBasicSearchDeployedOrReleasedConfigurations(in.getReturnType(), in.getSearch(), in.getFolders(), in
                        .getControllerId());
            } else {
                items = dbLayer.getBasicSearchInventoryConfigurations(in.getReturnType(), in.getSearch(), in.getFolders());
            }

            List<ResponseSearchItem> r = Collections.emptyList();
            if (items != null) {
                r = items.stream().map(item -> toResponseSearchItem(item)).sorted(Comparator.comparing(ResponseSearchItem::getPath)).collect(
                        Collectors.toList());
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

            List<InventorySearchItem> items = null;
            boolean deployedOrReleased = in.getDeployedOrReleased() != null && in.getDeployedOrReleased().booleanValue();
            if (deployedOrReleased) {
                items = dbLayer.getAdvancedSearchDeployedOrReleasedConfigurations(in.getReturnType(), in.getSearch(), in.getFolders(), in
                        .getAdvanced(), in.getControllerId());
            } else {
                items = dbLayer.getAdvancedSearchInventoryConfigurations(in.getReturnType(), in.getSearch(), in.getFolders(), in.getAdvanced());
            }

            List<ResponseSearchItem> r = new ArrayList<>();
            if (items != null) {
                List<InventorySearchItem> sorted = items.stream().sorted(Comparator.comparing(InventorySearchItem::getPath)).collect(Collectors
                        .toList());
                RequestSearchAdvancedItem workflowAdvanced = cloneAdvanced4WorkflowSearch(in);
                for (InventorySearchItem item : sorted) {
                    boolean checkWorkflow = false;
                    // TODO consider SCRIPT objects
                    switch (in.getReturnType()) {
                    case JOBRESOURCE:
                        workflowAdvanced.setJobResources(item.getName());
                        checkWorkflow = true;
                        break;
                    case NOTICEBOARD:
                        workflowAdvanced.setNoticeBoards(item.getName());
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
                            wi = dbLayer.getAdvancedSearchDeployedOrReleasedConfigurations(RequestSearchReturnType.WORKFLOW, in.getAdvanced()
                                    .getWorkflow(), null, workflowAdvanced, in.getControllerId());
                        } else {
                            wi = dbLayer.getAdvancedSearchInventoryConfigurations(RequestSearchReturnType.WORKFLOW, in.getAdvanced().getWorkflow(),
                                    null, workflowAdvanced);
                        }
                        if (wi == null || wi.size() == 0) {
                            continue;
                        }
                    }

                    ResponseSearchItem ri = toResponseSearchItem(item);
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
        case NOTICEBOARD:
            in.getAdvanced().setNoticeBoards(null);
            break;
        case LOCK:
            in.getAdvanced().setLock(null);
            break;
        case SCHEDULE:
            in.getAdvanced().setSchedule(null);
            break;
        case INCLUDESCRIPT:
            in.getAdvanced().setIncludeScript(null);
            break;
        case CALENDAR:
            in.getAdvanced().setCalendar(null);
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
        item.setNoticeBoards(in.getAdvanced().getNoticeBoards());
        item.setFileOrderSource(in.getAdvanced().getFileOrderSource());
        item.setJobCountFrom(in.getAdvanced().getJobCountFrom());
        item.setJobCountTo(in.getAdvanced().getJobCountTo());
        item.setJobCriticality(in.getAdvanced().getJobCriticality());
        item.setJobName(in.getAdvanced().getJobName());
        item.setJobNameExactMatch(in.getAdvanced().getJobNameExactMatch());
        item.setJobResources(in.getAdvanced().getJobResources());
        item.setLock(in.getAdvanced().getLock());
        item.setSchedule(in.getAdvanced().getSchedule());
        item.setIncludeScript(in.getAdvanced().getIncludeScript());
        item.setCalendar(in.getAdvanced().getCalendar());
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
    
    private ResponseSearchItem toResponseSearchItem(InventorySearchItem item) {
        ResponseSearchItem ri = new ResponseSearchItem();
        ri.setId(item.getId());
        ri.setPath(item.getPath());
        ri.setName(item.getName());
        ri.setObjectType(item.getTypeAsEnum());
        ri.setTitle(item.getTitle());
        ri.setControllerId(item.getControllerId());
        ri.setValid(item.isValid());
        ri.setDeleted(item.isDeleted());
        ri.setDeployed(item.isDeployed());
        ri.setReleased(item.isReleased());
        ri.setHasDeployments(item.getCountDeployed().intValue() > 0);
        ri.setHasReleases(item.getCountReleased().intValue() > 0);
        ri.setPermitted(folderPermissions.isPermittedForFolder(item.getFolder()));
        return ri;
    }
}
