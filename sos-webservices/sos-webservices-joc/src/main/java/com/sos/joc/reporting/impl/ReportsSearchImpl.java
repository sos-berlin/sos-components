package com.sos.joc.reporting.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.inventory.impl.SearchResourceImpl;
import com.sos.joc.inventory.resource.ISearchResource;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.inventory.search.RequestDeployedSearchFilter;
import com.sos.joc.model.inventory.search.RequestSearchFilter;
import com.sos.joc.model.inventory.search.RequestSearchReturnType;
import com.sos.joc.model.inventory.search.ResponseSearch;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(WebservicePaths.REPORTING)
public class ReportsSearchImpl extends JOCResourceImpl implements ISearchResource {

    private static final String API_CALL = "./reporting/search";

    @Override
    public JOCDefaultResponse postSearch(String accessToken, byte[] filterBytes) {
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken, CategoryType.CONTROLLER);
            JsonValidator.validateFailFast(filterBytes, RequestDeployedSearchFilter.class);
            RequestSearchFilter in = Globals.objectMapper.readValue(filterBytes, RequestSearchFilter.class);
            in.setReturnType(RequestSearchReturnType.REPORT);
            in.setDeployedOrReleased(true);

            JOCDefaultResponse response = initPermissions(null, getBasicJocPermissions(accessToken).getReports().getView());
            if (response != null) {
                return response;
            }
            if (in.getFolders() != null) {
                for (String folder : in.getFolders()) {
                    if (!folderPermissions.isPermittedForFolder(folder)) {
                        throw new JocFolderPermissionsException(folder);
                    }
                }
            }

            ResponseSearch answer = new ResponseSearch();
            answer.setResults(SearchResourceImpl.getSearchResult(in, folderPermissions));
            answer.setDeliveryDate(Date.from(Instant.now()));
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

}