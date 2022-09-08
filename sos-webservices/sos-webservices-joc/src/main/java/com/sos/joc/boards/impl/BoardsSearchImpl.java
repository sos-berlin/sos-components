package com.sos.joc.boards.impl;

import java.time.Instant;
import java.util.Date;

import jakarta.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.inventory.impl.SearchResourceImpl;
import com.sos.joc.boards.resource.ISearchResource;
import com.sos.joc.model.inventory.search.RequestDeployedSearchFilter;
import com.sos.joc.model.inventory.search.RequestSearchFilter;
import com.sos.joc.model.inventory.search.RequestSearchReturnType;
import com.sos.joc.model.inventory.search.ResponseSearch;
import com.sos.schema.JsonValidator;

@Path("notice")
public class BoardsSearchImpl extends JOCResourceImpl implements ISearchResource {

    private static final String API_CALL = "./notice/boards/search";

    @Override
    public JOCDefaultResponse postSearch(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, RequestDeployedSearchFilter.class);
            RequestSearchFilter in = Globals.objectMapper.readValue(filterBytes, RequestSearchFilter.class);
            in.setReturnType(RequestSearchReturnType.NOTICEBOARD);
            in.setDeployedOrReleased(true);

            boolean permission = getControllerPermissions(in.getControllerId(), accessToken).getNoticeBoards().getView();
            JOCDefaultResponse response = initPermissions(in.getControllerId(), permission);
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
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}