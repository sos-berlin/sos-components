
package com.sos.webservices.order.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.model.plan.PlannedOrdersFilter;

public interface ICalculatePlansResource {

    @POST
    @Path("calculate_plans")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postCalculatePlans(@HeaderParam("X-Access-Token") String accessToken,
            PlannedOrdersFilter planOrdersFilter) throws Exception;
}
