
package com.sos.joc.workflows.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

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
    
    @POST
    @Path("skip")
    @Produces({ "application/json" })
    public JOCDefaultResponse skipWorkflows(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
}
