
package com.sos.webservices.order.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.webservices.order.initiator.model.ScheduleFilter;
 
public interface ISchedulesResource {

    @POST
    @Path("list")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postSchedules(@HeaderParam("X-Access-Token") String accessToken, ScheduleFilter scheduleFilter);
}
