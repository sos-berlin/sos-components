
package com.sos.joc.workflow.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IWorkflowOrderTemplates {

    @Path("order_templates")
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postOrderTemplates(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
