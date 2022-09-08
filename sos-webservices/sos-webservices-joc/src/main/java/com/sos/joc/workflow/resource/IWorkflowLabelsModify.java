
package com.sos.joc.workflow.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IWorkflowLabelsModify {

    @POST
    @Path("skip")
    @Produces({ "application/json" })
    public JOCDefaultResponse skipWorkflows(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("unskip")
    @Produces({ "application/json" })
    public JOCDefaultResponse unskipWorkflows(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
}
