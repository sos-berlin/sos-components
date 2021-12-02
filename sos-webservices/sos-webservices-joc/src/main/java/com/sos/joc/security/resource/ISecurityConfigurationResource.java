
package com.sos.joc.security.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
