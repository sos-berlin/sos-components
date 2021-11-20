package com.sos.joc.joc.resource;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IStateResource {

    @Path("is_active")
    @GET
    @Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getIsActive(@HeaderParam("X-Access-Token") String xAccessToken, @QueryParam("accessToken") String accessToken);
    
    @Path("is_active")
    @POST
    @Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postIsActive(@HeaderParam("X-Access-Token") String accessToken);

}
