
package com.sos.webservices.order.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.model.dailyplan.DailyChangeStartTime;

public interface IDailyPlanChangeStartTimeResource {

    @POST
    @Path("orders/starttime")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postChangeStartime(@HeaderParam("X-Access-Token") String accessToken, DailyChangeStartTime dailyChangeStartTime)
            throws Exception;

}
