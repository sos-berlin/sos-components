package com.sos.joc.inventory.impl;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.quicksearch.QuickSearchStore;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IQuickSearchResource;
import com.sos.joc.model.inventory.search.RequestQuickSearchFilter;
import com.sos.joc.model.inventory.search.ResponseQuickSearch;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(JocInventory.APPLICATION_PATH)
public class QuickSearchResourceImpl extends JOCResourceImpl implements IQuickSearchResource {

    @Override
    public JOCDefaultResponse postSearch(final String accessToken, byte[] inBytes) {
        try {
            inBytes = initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, RequestQuickSearchFilter.class);
            RequestQuickSearchFilter in = Globals.objectMapper.readValue(inBytes, RequestQuickSearchFilter.class);

            JOCDefaultResponse response = initPermissions(null, getBasicJocPermissions(accessToken).getInventory().getView());
            if (response != null) {
                return response;
            }
            
            ResponseQuickSearch answer = QuickSearchStore.getAnswer(in, accessToken, folderPermissions, true);
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Throwable e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
}
