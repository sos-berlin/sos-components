
package com.sos.joc.approval.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IRequestResource {

    @POST
    @Path("request")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postRequest(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
