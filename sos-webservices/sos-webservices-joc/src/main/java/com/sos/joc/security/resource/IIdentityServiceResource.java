
package com.sos.joc.security.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
