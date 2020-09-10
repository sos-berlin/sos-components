
package com.sos.webservices.order.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilter;

public interface IRemoveOrderResource {

    @POST
    @Path("remove_orders")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postRemoveOrders(@HeaderParam("X-Access-Token") String accessToken,
            DailyPlanOrderFilter dailyPlanOrderFilter) throws Exception;
}
