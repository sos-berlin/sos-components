
package com.sos.webservices.order.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.model.dailyplan.DailyPlanModifyOrder;

public interface IDailyPlanModifyOrder {

    @POST
    @Path("orders/modify")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postModifyOrder(@HeaderParam("X-Access-Token") String accessToken, DailyPlanModifyOrder dailyPlanModifyOrder)
            throws Exception;
 

}
