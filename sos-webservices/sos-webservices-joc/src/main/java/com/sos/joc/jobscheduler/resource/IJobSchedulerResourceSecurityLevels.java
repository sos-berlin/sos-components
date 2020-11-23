
package com.sos.joc.jobscheduler.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IJobSchedulerResourceSecurityLevels {

    @POST
    @Path("security_levels")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postControllerIdsWithSecurityLevel(@HeaderParam("X-Access-Token") String xAccessToken);
    
    @POST
    @Path("security_levels/take_over")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse takeOverSecurityLevel(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
}
