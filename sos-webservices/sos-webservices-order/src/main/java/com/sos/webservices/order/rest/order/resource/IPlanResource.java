
package com.sos.webservices.order.rest.order.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.model.plan.PlanFilter;

public interface IPlanResource {

    @POST
    @Path("plan")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postPlan(@HeaderParam("X-Access-Token") String xAccessToken, @HeaderParam("access_token") String accessToken,
            PlanFilter planFilter) throws Exception;
}
