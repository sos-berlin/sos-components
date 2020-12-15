
package com.sos.joc.security.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.model.security.SecurityConfiguration;

public interface ISecurityConfigurationResource
{

    @POST
    @Path("shiro")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postShiroRead(@HeaderParam("X-Access-Token") String xAccessToken,@HeaderParam("access_token") String accessToken) throws Exception;
    
    @POST
    @Path("shiro/store")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postShiroStore(@HeaderParam("X-Access-Token") String xAccessToken,@HeaderParam("access_token") String accessToken, SecurityConfiguration securityConfiguration) throws Exception;

}
