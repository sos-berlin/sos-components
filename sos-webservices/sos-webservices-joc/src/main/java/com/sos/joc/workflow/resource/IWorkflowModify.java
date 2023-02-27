
package com.sos.joc.workflow.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IWorkflowModify {

    @POST
    @Path("transition")
    @Produces({ "application/json" })
    public JOCDefaultResponse transition(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("transfer")
    @Produces({ "application/json" })
    public JOCDefaultResponse transfer(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
}
