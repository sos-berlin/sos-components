package com.sos.joc.workflows.impl;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.quicksearch.QuickSearchStore;
import com.sos.joc.classes.tag.GroupedTag;
import com.sos.joc.inventory.resource.ITagSearchResource;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.DeployedObjectQuickSearchFilter;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.search.ResponseQuickSearch;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("workflows")
public class WorkflowTagSearchImpl extends JOCResourceImpl implements ITagSearchResource {
    
    private static final String API_CALL = "./workflows/tag/search";

    @Override
    public JOCDefaultResponse postTagSearch(final String accessToken, byte[] inBytes) {
        try {
            inBytes = initLogging(API_CALL, inBytes, accessToken, CategoryType.INVENTORY);
            JsonValidator.validateFailFast(inBytes, DeployedObjectQuickSearchFilter.class);
            DeployedObjectQuickSearchFilter in = Globals.objectMapper.readValue(inBytes, DeployedObjectQuickSearchFilter.class);

            String controllerId = in.getControllerId();
            JOCDefaultResponse response = initPermissions(controllerId, getBasicControllerPermissions(controllerId, accessToken).getWorkflows()
                    .getView());
            if (response != null) {
                return response;
            }
            
            in.setSearch(new GroupedTag(in.getSearch()).getTag());
            ResponseQuickSearch answer = QuickSearchStore.getTagsAnswer(in, ConfigurationType.WORKFLOW, accessToken, folderPermissions);
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }
}
