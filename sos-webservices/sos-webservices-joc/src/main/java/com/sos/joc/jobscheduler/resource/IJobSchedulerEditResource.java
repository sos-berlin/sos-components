
package com.sos.joc.jobscheduler.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.model.jobscheduler.RegisterParameters;
import com.sos.joc.model.jobscheduler.UrlParameter;

public interface IJobSchedulerEditResource {

	@POST
	@Path("register")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({ MediaType.APPLICATION_JSON })
	public JOCDefaultResponse storeJobscheduler(@HeaderParam("X-Access-Token") String accessToken, RegisterParameters jobSchedulerFilter);
	
	@POST
    @Path("test")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse testConnectionJobscheduler(@HeaderParam("X-Access-Token") String xAccessToken, UrlParameter jobSchedulerFilter);

}
