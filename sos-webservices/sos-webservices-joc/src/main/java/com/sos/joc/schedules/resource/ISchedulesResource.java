
package com.sos.joc.schedules.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;

public interface ISchedulesResource {

    public static final String IMPL_PATH = WebservicePaths.getResourceImplPath(WebservicePaths.SCHEDULES);

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postSchedules(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);
}
