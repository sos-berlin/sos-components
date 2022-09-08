package com.sos.joc.publish.repository.git.credentials.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IGitCredentialsRemoteUriAdd {

    @POST
    @Path("credentials/uri/add")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postAddRemoteUri(@HeaderParam("X-Access-Token") String xAccessToken, byte[] addRemoteUriFilter) throws Exception;
}
