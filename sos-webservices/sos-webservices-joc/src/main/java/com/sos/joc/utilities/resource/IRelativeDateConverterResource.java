
package com.sos.joc.utilities.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;

public interface IRelativeDateConverterResource {

    public static final String PATH = "convert_relative_dates";
    public static final String IMPL_PATH = WebservicePaths.getResourceImplPath(WebservicePaths.UTILITIES, PATH);

    @POST
    @Path(PATH)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postConvertRelativeDates(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);
}
