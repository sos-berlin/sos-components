package com.sos.joc.profiles.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IJocProfilesResource {

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postProfiles(@HeaderParam("X-Access-Token") String xAccessToken, byte[] body);

    @POST
    @Path("delete")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postProfilesDelete(@HeaderParam("X-Access-Token") String xAccessToken, byte[] body);

}
