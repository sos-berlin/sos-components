
package com.sos.joc.order.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IOrderVariablesResource {

    @POST
    @Path("variables")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postVariables(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
