
package com.sos.joc.jobscheduler.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IJobSchedulerResourceComponents {

    @POST
    @Path("components")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postComponents(@Context UriInfo uriInfo, @HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
}
