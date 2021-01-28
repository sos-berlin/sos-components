
package com.sos.webservices.order.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.model.dailyplan.DailyPlanSubmissionsFilter;
   
public interface IDailyPlanSubmissionsResource {

    @POST
    @Path("submissions")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postDailyPlanSubmissions(@HeaderParam("X-Access-Token") String accessToken,
            DailyPlanSubmissionsFilter dailyPlanSubmissionsFilter) throws Exception;
}
