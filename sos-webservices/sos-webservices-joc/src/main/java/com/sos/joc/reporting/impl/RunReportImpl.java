package com.sos.joc.reporting.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.reporting.RunReport;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.reporting.RunFilter;
import com.sos.joc.reporting.resource.IRunReportResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path(WebservicePaths.REPORTING)
public class RunReportImpl extends JOCResourceImpl implements IRunReportResource {
    
    @Override
    public JOCDefaultResponse run(String accessToken, byte[] filterBytes) {
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, RunFilter.class);
            RunFilter in = Globals.objectMapper.readValue(filterBytes, RunFilter.class);
            
            boolean permitted = true;

            // TODO: PERMISSIONS, maybe a new permission

            JOCDefaultResponse response = initPermissions(null, permitted);
            if (response != null) {
                return response;
            }
            // TODO: event when ready without exception
            RunReport.run(in).thenAccept(e -> ProblemHelper.postExceptionEventIfExist(e, accessToken, getJocError(), null));
            
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
