
package com.sos.joc.security.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IIdentityServiceResource {

    @POST
    @Path("identityservice")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postIdentityServiceRead(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("identityservice/rename")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postIdentityServiceRename(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("identityservice/store")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postIdentityServiceStore(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("identityservice/delete")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postIdentityServiceDelete(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("identityservices")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postIdentityServices(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("identityservices/reorder")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postIdentityServicesReorder(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

}
