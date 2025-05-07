package com.sos.joc.orders.impl;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.quicksearch.QuickSearchStore;
import com.sos.joc.classes.tag.GroupedTag;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.ITagSearchResource;
import com.sos.joc.model.common.DeployedObjectQuickSearchFilter;
import com.sos.joc.model.inventory.search.ResponseQuickSearch;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("orders")
public class OrdersTagSearchImpl extends JOCResourceImpl implements ITagSearchResource {
    
    private static final String API_CALL = "./orders/tag/search";

    @Override
    public JOCDefaultResponse postTagSearch(final String accessToken, byte[] inBytes) {
        try {
            inBytes = initLogging(API_CALL, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, DeployedObjectQuickSearchFilter.class);
            DeployedObjectQuickSearchFilter in = Globals.objectMapper.readValue(inBytes, DeployedObjectQuickSearchFilter.class);

            String controllerId = in.getControllerId();
            JOCDefaultResponse response = initPermissions(controllerId, getBasicControllerPermissions(controllerId, accessToken).getOrders()
                    .getView());
            if (response != null) {
                return response;
            }
            
            in.setSearch(new GroupedTag(in.getSearch()).getTag());
            ResponseQuickSearch answer = QuickSearchStore.getOrderTagsAnswer(in, accessToken);
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Throwable e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
}
