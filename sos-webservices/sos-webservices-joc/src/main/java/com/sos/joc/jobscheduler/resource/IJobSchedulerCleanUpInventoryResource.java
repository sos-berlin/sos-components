package com.sos.joc.jobscheduler.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.model.jobscheduler.HostPortParameter;
import com.sos.joc.model.jobscheduler.UriParameter;

public interface IJobSchedulerCleanUpInventoryResource {
    
    @Deprecated
	@POST
    @Path("cleanup")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse oldPostJobschedulerCleanupInventory(
            @HeaderParam("X-Access-Token") String accessToken, HostPortParameter hostPortParameter) throws Exception;
    
    @POST
    @Path("cleanup2")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse postJobschedulerCleanupInventory(
            @HeaderParam("X-Access-Token") String accessToken, UriParameter UriParameter) throws Exception;

}
