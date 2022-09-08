
package com.sos.joc.controller.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IControllerEditResource {

	@POST
	@Path("register")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({ MediaType.APPLICATION_JSON })
	public JOCDefaultResponse registerController(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);
	
	@POST
    @Path("cleanup")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse deleteController(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);
    
	@POST
    @Path("unregister")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse unregisterController(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);
    
    @POST
    @Path("test")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse testControllerConnection(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
