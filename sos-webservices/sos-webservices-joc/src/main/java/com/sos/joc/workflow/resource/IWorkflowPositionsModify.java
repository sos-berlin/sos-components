
package com.sos.joc.workflow.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IWorkflowPositionsModify {

    @POST
    @Path("stop")
    @Produces({ "application/json" })
    public JOCDefaultResponse stopWorkflows(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("unstop")
    @Produces({ "application/json" })
    public JOCDefaultResponse unstopWorkflows(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
}
