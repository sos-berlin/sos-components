package com.sos.joc.calendars.impl;

import java.util.Collections;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.quicksearch.QuickSearchStore;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IQuickSearchResource;
import com.sos.joc.model.inventory.search.RequestQuickSearchFilter;
import com.sos.joc.model.inventory.search.RequestSearchReturnType;
import com.sos.joc.model.inventory.search.ResponseQuickSearch;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("calendars")
public class CalendarQuickSearchImpl extends JOCResourceImpl implements IQuickSearchResource {
    
    private static final String API_CALL = "./calendars/quick/search";

    @Override
    public JOCDefaultResponse postSearch(final String accessToken, byte[] inBytes) {
        try {
            inBytes = initLogging(API_CALL, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, RequestQuickSearchFilter.class);
            RequestQuickSearchFilter in = Globals.objectMapper.readValue(inBytes, RequestQuickSearchFilter.class);

            JOCDefaultResponse response = initPermissions(null, getBasicJocPermissions(accessToken).getCalendars().getView());
            if (response != null) {
                return response;
            }
            
            in.setReturnTypes(Collections.singletonList(RequestSearchReturnType.CALENDAR));
            ResponseQuickSearch answer = QuickSearchStore.getAnswer(in, accessToken, folderPermissions, false);
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Throwable e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
}
