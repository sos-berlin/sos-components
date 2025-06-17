package com.sos.joc.boards.impl;

import com.sos.joc.Globals;
import com.sos.joc.boards.resource.IQuickSearchResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.quicksearch.QuickSearchStore;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.DeployedObjectQuickSearchFilter;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.search.ResponseQuickSearch;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("notice")
public class BoardQuickSearchImpl extends JOCResourceImpl implements IQuickSearchResource {
    
    private static final String API_CALL = "./notice/boards/quick/search";

    @Override
    public JOCDefaultResponse postSearch(final String accessToken, byte[] inBytes) {
        try {
            inBytes = initLogging(API_CALL, inBytes, accessToken, CategoryType.CONTROLLER);
            JsonValidator.validateFailFast(inBytes, DeployedObjectQuickSearchFilter.class);
            DeployedObjectQuickSearchFilter in = Globals.objectMapper.readValue(inBytes, DeployedObjectQuickSearchFilter.class);

            String controllerId = in.getControllerId();
            JOCDefaultResponse response = initPermissions(controllerId, getBasicControllerPermissions(controllerId, accessToken).getNoticeBoards()
                    .getView());
            if (response != null) {
                return response;
            }
            
            ResponseQuickSearch answer = QuickSearchStore.getAnswer(in, ConfigurationType.NOTICEBOARD, accessToken, folderPermissions);
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }
}
