
package com.sos.joc.controller.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
    @Path("test")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse testControllerConnection(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
