package com.sos.joc.inventory.impl;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.ISearchResource;
import com.sos.joc.model.common.Folder;
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

            ResponseSearch answer = createAnswer();

            switch (in.getReturnType()) {
            case WORKFLOW:
                answer.setWorkflows(new HashSet<ResponseSearchItem>());
                break;
            case FILEORDERSOURCE:
                answer.setFileOrderSources(new HashSet<ResponseSearchItem>());
                break;
            case JOBRESOURCE:
                answer.setJobResources(new HashSet<ResponseSearchItem>());
                break;
            case BOARD:
                answer.setBoards(new HashSet<ResponseSearchItem>());
                break;
            case LOCK:
                answer.setLocks(new HashSet<ResponseSearchItem>());
                break;
            case SCHEDULE:
                answer.setSchedules(new HashSet<ResponseSearchItem>());
                break;
            }

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private ResponseSearch createAnswer() {
        ResponseSearch answer = new ResponseSearch();
        answer.setDeliveryDate(new Date());
        answer.setWorkflows(null);
        answer.setFileOrderSources(null);
        answer.setJobResources(null);
        answer.setBoards(null);
        answer.setLocks(null);
        answer.setSchedules(null);
        return answer;
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, final RequestSearchFilter in, boolean permission) throws Exception {
        JOCDefaultResponse response = initPermissions(in.getControllerId(), getPermitted(accessToken, in));
        if (response == null) {
            if (in.getFolders() != null) {
                for (Folder folder : in.getFolders()) {
                    if (!folderPermissions.isPermittedForFolder(folder.getFolder())) {
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
