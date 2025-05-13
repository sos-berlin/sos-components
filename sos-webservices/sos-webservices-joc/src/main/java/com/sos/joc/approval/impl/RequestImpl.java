package com.sos.joc.approval.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.joc.Globals;
import com.sos.joc.approval.resource.IRequestResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.security.foureyes.FourEyesRequest;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("approval")
public class RequestImpl extends JOCResourceImpl implements IRequestResource {

    private static final String API_CALL = "./approval/request";

    @Override
    public JOCDefaultResponse postRequest(String accessToken, byte[] filterBytes) {
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, FourEyesRequest.class);
            FourEyesRequest filter = Globals.objectMapper.readValue(filterBytes, FourEyesRequest.class);
            JOCDefaultResponse response = initPermissions(null, true);
            if (response != null) {
                return response;
            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}