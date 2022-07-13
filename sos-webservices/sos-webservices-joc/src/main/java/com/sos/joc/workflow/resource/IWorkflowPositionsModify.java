
package com.sos.joc.workflow.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

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
