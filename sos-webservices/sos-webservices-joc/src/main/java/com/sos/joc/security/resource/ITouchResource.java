
package com.sos.joc.security.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface ITouchResource {

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postTouch(@HeaderParam("X-Access-Token") String xAccessToken,@HeaderParam("access_token") String accessToken);
    
    @POST
    @Path("log4j")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postTouchLog4j(@HeaderParam("X-Access-Token") String xAccessToken);
}
