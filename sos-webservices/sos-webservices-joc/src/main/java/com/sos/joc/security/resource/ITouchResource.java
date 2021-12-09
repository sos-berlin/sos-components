
package com.sos.joc.security.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface ITouchResource {

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postTouch(@HeaderParam("X-Access-Token") String xAccessToken,@HeaderParam("access_token") String accessToken) throws Exception;
    
    @POST
    @Path("log4j")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postTouchLog4j(@HeaderParam("X-Access-Token") String xAccessToken) throws Exception;
}
