package com.sos.joc.publish.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IRevoke {

    @POST
    @Path("revoke")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postRevoke(@HeaderParam("X-Access-Token") String xAccessToken, byte[] revokeFilter) throws Exception;
}
