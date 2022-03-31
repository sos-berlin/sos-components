package com.sos.joc.publish.git.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IGitCredentialsGet {

    @POST
    @Path("credentials")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postGetCredentials(@HeaderParam("X-Access-Token") String xAccessToken, byte[] getCredentialsFilter) throws Exception;
}
