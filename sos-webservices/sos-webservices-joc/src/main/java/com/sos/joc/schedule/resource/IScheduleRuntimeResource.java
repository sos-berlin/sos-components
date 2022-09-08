
package com.sos.joc.schedule.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IScheduleRuntimeResource {

    @Path("runtime")
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postScheduleRuntime(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
