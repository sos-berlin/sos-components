
package com.sos.joc.jobscheduler.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IJobSchedulerResourceDb {

	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	public JOCDefaultResponse postJobschedulerDb(@HeaderParam("X-Access-Token") String accessToken);
}
