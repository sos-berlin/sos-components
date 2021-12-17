package com.sos.joc.publish.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IRevoke {

    @POST
    @Path("revoke")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postRevoke(@HeaderParam("X-Access-Token") String xAccessToken, byte[] revokeFilter) throws Exception;
}
