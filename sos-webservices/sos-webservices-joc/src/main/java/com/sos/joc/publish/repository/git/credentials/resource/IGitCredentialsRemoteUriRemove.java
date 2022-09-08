package com.sos.joc.publish.repository.git.credentials.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IGitCredentialsRemoteUriRemove {

    @POST
    @Path("credentials/uri/remove")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postAddRemoteUri(@HeaderParam("X-Access-Token") String xAccessToken, byte[] removeRemoteUriFilter) throws Exception;
}
