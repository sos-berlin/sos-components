
package com.sos.joc.monitoring.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;

public interface INotifications {

    public static final String PATH = "notifications";
    public static final String IMPL_PATH = WebservicePaths.getResourceImplPath(WebservicePaths.MONITORING, PATH);

    @POST
    @Path(PATH)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse post(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
}
