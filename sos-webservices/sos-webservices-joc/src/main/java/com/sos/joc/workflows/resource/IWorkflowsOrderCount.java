
package com.sos.joc.workflows.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IWorkflowsOrderCount {

    @POST
    @Path("order_count")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postOrderCount(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
