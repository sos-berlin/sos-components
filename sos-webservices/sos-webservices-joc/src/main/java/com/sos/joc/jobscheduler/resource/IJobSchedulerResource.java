
package com.sos.joc.jobscheduler.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.model.jobscheduler.UrlParameter;

public interface IJobSchedulerResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postJobscheduler(@HeaderParam("X-Access-Token") String accessToken, UrlParameter jobSchedulerFilter);

    @POST
    @Path("p")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postJobschedulerP(@HeaderParam("X-Access-Token") String xAccessToken, UrlParameter jobSchedulerFilter);
}
