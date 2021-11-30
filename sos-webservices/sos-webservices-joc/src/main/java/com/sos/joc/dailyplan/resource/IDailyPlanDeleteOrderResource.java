
package com.sos.joc.dailyplan.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IDailyPlanDeleteOrderResource {

    @POST
    @Path("orders/delete")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postDeleteOrders(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes) throws Exception;

}
