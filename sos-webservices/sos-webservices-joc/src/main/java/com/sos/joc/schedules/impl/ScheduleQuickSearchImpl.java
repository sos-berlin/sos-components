package com.sos.joc.schedules.impl;

import java.util.Collections;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.quicksearch.QuickSearchStore;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IQuickSearchResource;
import com.sos.joc.model.common.DeployedObjectQuickSearchFilter;
import com.sos.joc.model.inventory.search.RequestQuickSearchFilter;
import com.sos.joc.model.inventory.search.RequestSearchReturnType;
import com.sos.joc.model.inventory.search.ResponseQuickSearch;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("schedules")
public class ScheduleQuickSearchImpl extends JOCResourceImpl implements IQuickSearchResource {
    
    private static final String API_CALL = "./schedules/quick/search";

    @Override
    public JOCDefaultResponse postSearch(final String accessToken, final byte[] inBytes) {
        try {
            initLogging(API_CALL, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, RequestQuickSearchFilter.class);
            DeployedObjectQuickSearchFilter in = Globals.objectMapper.readValue(inBytes, DeployedObjectQuickSearchFilter.class);

            String controllerId = in.getControllerId();
            JOCDefaultResponse response = initPermissions(controllerId, getControllerPermissions(controllerId, accessToken).getOrders().getView());
            if (response != null) {
                return response;
            }
            
            RequestQuickSearchFilter filter = new RequestQuickSearchFilter();
            filter.setReturnTypes(Collections.singletonList(RequestSearchReturnType.SCHEDULE));
            filter.setQuit(in.getQuit());
            filter.setSearch(in.getSearch());
            filter.setToken(in.getToken());
            
            ResponseQuickSearch answer = QuickSearchStore.getAnswer(filter, accessToken, folderPermissions, false, controllerId);
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Throwable e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
}
