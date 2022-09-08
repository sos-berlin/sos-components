package com.sos.joc.utilities.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;

public interface IHashResource {

    public static final String PATH = "hash";
    public static final String IMPL_PATH = WebservicePaths.getResourceImplPath(WebservicePaths.UTILITIES, PATH);

    @Path(PATH)
    @POST
    @Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postHash(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

}
