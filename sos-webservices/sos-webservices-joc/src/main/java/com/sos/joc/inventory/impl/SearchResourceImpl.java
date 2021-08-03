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

            // TODO releasedOrDeployed
            ConfigurationType objectType = ConfigurationType.valueOf(in.getReturnType().value());
            List<InventorySearchItem> items = dbLayer.getInventoryConfigurations(objectType, in.getSearch(), in.getFolders());
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

    private List<ResponseSearchItem> getAdvancedSearch(final RequestSearchFilter in) {
        List<ResponseSearchItem> r = new ArrayList<>();
        switch (in.getReturnType()) {
        case WORKFLOW:
            r = getWorkflows(in);
            break;
        case FILEORDERSOURCE:
            r = getFileOrderSources(in);
            break;
        case JOBRESOURCE:
            r = getJobResources(in);
            break;
        case BOARD:
            r = getBoards(in);
            break;
        case LOCK:
            r = getLocks(in);
            break;
        case SCHEDULE:
            r = getSchedules(in);
            break;
        }
        return r;
    }

    private List<ResponseSearchItem> getWorkflows(final RequestSearchFilter in) {
        List<ResponseSearchItem> r = new ArrayList<>();

        return r;
    }

    private List<ResponseSearchItem> getFileOrderSources(final RequestSearchFilter in) {
        List<ResponseSearchItem> r = new ArrayList<>();

        return r;
    }

    private List<ResponseSearchItem> getJobResources(final RequestSearchFilter in) {
        List<ResponseSearchItem> r = new ArrayList<>();

        return r;
    }

    private List<ResponseSearchItem> getBoards(final RequestSearchFilter in) {
        List<ResponseSearchItem> r = new ArrayList<>();

        return r;
    }

    private List<ResponseSearchItem> getLocks(final RequestSearchFilter in) {
        List<ResponseSearchItem> r = new ArrayList<>();

        return r;
    }

    private List<ResponseSearchItem> getSchedules(final RequestSearchFilter in) {
        List<ResponseSearchItem> r = new ArrayList<>();

        return r;
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
