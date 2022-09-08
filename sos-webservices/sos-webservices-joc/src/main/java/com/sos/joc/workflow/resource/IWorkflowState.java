
package com.sos.joc.workflow.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IWorkflowState {

    @POST
    @Path("state")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postState(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("status")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postStatus(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
