
package com.sos.joc.workflows.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IWorkflowsSnapshot {

    @POST
    @Path("overview/snapshot")
    @Produces({ "application/json" })
    public JOCDefaultResponse snapshot(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
}
