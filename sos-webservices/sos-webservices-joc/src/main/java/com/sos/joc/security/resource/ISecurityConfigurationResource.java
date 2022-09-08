
package com.sos.joc.security.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface ISecurityConfigurationResource {

    @POST
    @Path("auth")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postAuthRead(@HeaderParam("X-Access-Token") String xAccessToken, byte[] body);

    @POST
    @Path("auth/store")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postAuthStore(@HeaderParam("X-Access-Token") String xAccessToken, byte[] body);
 
 
}
