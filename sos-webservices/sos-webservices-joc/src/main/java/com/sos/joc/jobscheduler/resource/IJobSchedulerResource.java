
package com.sos.joc.jobscheduler.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.model.jobscheduler.HostPortParameter;
import com.sos.joc.model.jobscheduler.UriParameter;

public interface IJobSchedulerResource {

	@Deprecated
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({ MediaType.APPLICATION_JSON })
	public JOCDefaultResponse oldPostJobscheduler(@HeaderParam("X-Access-Token") String accessToken,
			HostPortParameter jobSchedulerFilter) throws Exception;

	@POST
	@Path("2")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({ MediaType.APPLICATION_JSON })
	public JOCDefaultResponse postJobscheduler(@HeaderParam("X-Access-Token") String accessToken,
			UriParameter jobSchedulerFilter) throws Exception;
}
