package com.sos.joc.documentations.impl;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.quicksearch.QuickSearchStore;
import com.sos.joc.inventory.resource.IQuickSearchResource;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.inventory.search.RequestQuickSearchFilter;
import com.sos.joc.model.inventory.search.ResponseQuickSearch;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("documentations")
public class DocumentationQuickSearchImpl extends JOCResourceImpl implements IQuickSearchResource {
    
    private static final String API_CALL = "./documentations/quick/search";

    @Override
    public JOCDefaultResponse postSearch(final String accessToken, byte[] inBytes) {
        try {
            inBytes = initLogging(API_CALL, inBytes, accessToken, CategoryType.DOCUMENTATIONS);
            JsonValidator.validateFailFast(inBytes, RequestQuickSearchFilter.class);
            RequestQuickSearchFilter in = Globals.objectMapper.readValue(inBytes, RequestQuickSearchFilter.class);

            JOCDefaultResponse response = initPermissions(null, getBasicJocPermissions(accessToken).getDocumentations().getView());
            if (response != null) {
                return response;
            }
            in.setReturnTypes(null);
            ResponseQuickSearch answer = QuickSearchStore.getAnswer(in, accessToken, folderPermissions, false);
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }
}
