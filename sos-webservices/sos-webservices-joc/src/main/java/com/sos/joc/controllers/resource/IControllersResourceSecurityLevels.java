
package com.sos.joc.controllers.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IControllersResourceSecurityLevels {

    @POST
    @Path("security_level")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postControllerIdsWithSecurityLevel(@HeaderParam("X-Access-Token") String xAccessToken);
    
    @POST
    @Path("security_level/take_over")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse takeOverSecurityLevel(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
}
