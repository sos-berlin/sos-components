
package com.sos.joc.schedules.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;

public interface ISchedulesResource {

    public static final String IMPL_PATH = WebservicePaths.getResourceImplPath(WebservicePaths.SCHEDULES);

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postSchedules(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);
}
