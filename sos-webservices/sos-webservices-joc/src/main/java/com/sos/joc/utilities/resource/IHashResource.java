package com.sos.joc.utilities.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
