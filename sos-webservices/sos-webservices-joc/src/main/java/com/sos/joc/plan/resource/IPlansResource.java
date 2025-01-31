
package com.sos.joc.plan.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IPlansResource {

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postPlans(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
