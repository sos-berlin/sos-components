
package com.sos.joc.reporting.resource;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IOrderStepsResource {

    public static final String PATH_ORDER_STEPS = "order_steps";

    public static final String IMPL_PATH_ORDER_STEPS = WebservicePaths.getResourceImplPath(WebservicePaths.REPORTING, PATH_ORDER_STEPS);

    @POST
    @Path(PATH_ORDER_STEPS)
    @Produces({ MediaType.TEXT_PLAIN })
    public JOCDefaultResponse orderSteps(@HeaderParam("X-Access-Token") String xAccessToken, @HeaderParam("Accept-Encoding") String acceptEncoding,
            byte[] filterBytes);

}
