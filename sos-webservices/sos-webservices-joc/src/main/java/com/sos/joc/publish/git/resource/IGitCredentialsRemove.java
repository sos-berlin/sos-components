package com.sos.joc.publish.git.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IGitCredentialsRemove {

    @POST
    @Path("credentials/remove")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postRemoveCredentials(@HeaderParam("X-Access-Token") String xAccessToken, byte[] removeCredentialsFilter) throws Exception;
}
