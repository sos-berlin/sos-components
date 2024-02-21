package com.sos.joc.reporting.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.reporting.LoadData;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.reporting.LoadFilter;
import com.sos.joc.reporting.resource.ILoadDataResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path(WebservicePaths.REPORTING)
public class LoadDataImpl extends JOCResourceImpl implements ILoadDataResource {
    
    @Override
    public JOCDefaultResponse create2(String accessToken, byte[] filterBytes) {
        return create(accessToken, filterBytes);
    }
    
    @Override
    public JOCDefaultResponse create(String accessToken, byte[] filterBytes) {
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, LoadFilter.class);
            LoadFilter in = Globals.objectMapper.readValue(filterBytes, LoadFilter.class);
            
            boolean permitted = true;

            // TODO: PERMISSIONS, maybe a new permission

            JOCDefaultResponse response = initPermissions(null, permitted);
            if (response != null) {
                return response;
            }
            
            // TODO: event when ready without exception
            LoadData.writeCSVFiles(in.getDateFrom()).thenAccept(e -> ProblemHelper.postExceptionEventIfExist(e, accessToken, getJocError(), null));
            
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}