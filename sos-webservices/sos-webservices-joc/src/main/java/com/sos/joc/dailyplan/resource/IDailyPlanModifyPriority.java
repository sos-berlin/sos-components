
package com.sos.joc.dailyplan.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;

public interface IDailyPlanModifyPriority {

    public static final String PATH = "orders/modify/priority";
    public static final String IMPL_PATH = WebservicePaths.getResourceImplPath(WebservicePaths.DAILYPLAN, PATH);

    @POST
    @Path(PATH)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postModifyPriority(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);

}
