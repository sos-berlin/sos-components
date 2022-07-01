package com.sos.joc.profiles.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
