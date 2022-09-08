package com.sos.joc.publish.repository.git.credentials.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IGitCredentialsRemove {

    @POST
    @Path("credentials/remove")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postRemoveCredentials(@HeaderParam("X-Access-Token") String xAccessToken, byte[] removeCredentialsFilter) throws Exception;
}
