
package com.sos.joc.utilities.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
