
package com.sos.joc.workflow.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IWorkflowOrderTemplates {

    @Path("order_templates")
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postOrderTemplates(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
