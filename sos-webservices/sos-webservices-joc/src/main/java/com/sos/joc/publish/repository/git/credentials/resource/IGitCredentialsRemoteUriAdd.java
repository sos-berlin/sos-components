package com.sos.joc.publish.repository.git.credentials.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IGitCredentialsRemoteUriAdd {

    @POST
    @Path("credentials/uri/add")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postAddRemoteUri(@HeaderParam("X-Access-Token") String xAccessToken, byte[] addRemoteUriFilter) throws Exception;
}
