
package com.sos.joc.approval.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IRequestsShowResource {

    @POST
    @Path("requests")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postRequests(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);

}
