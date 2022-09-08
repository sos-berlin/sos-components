
package com.sos.joc.workflows.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IWorkflowsModify {

    @POST
    @Path("suspend")
    @Produces({ "application/json" })
    public JOCDefaultResponse suspendWorkflows(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("resume")
    @Produces({ "application/json" })
    public JOCDefaultResponse resumeWorkflows(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
}
