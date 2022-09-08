package com.sos.joc.yade.resource.xmleditor;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IProfilesResource {

    public static final String PATH = "profiles";
    public static final String IMPL_PATH = "./yade/" + PATH;

    @POST
    @Path(PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getProfiles(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);

}
