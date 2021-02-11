
package com.sos.joc.workflows.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IWorkflowsResource {

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postWorkflowsPermanent(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("v")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postWorkflowsVolatile(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
