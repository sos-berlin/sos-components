
package com.sos.joc.dailyplan.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;

public interface IDailyPlanSubmissionsResource {

    public static final String PATH_MAIN = "submissions";
    public static final String IMPL_PATH_MAIN = WebservicePaths.getResourceImplPath(WebservicePaths.DAILYPLAN, PATH_MAIN);

    public static final String PATH_DELETE = "submissions/delete";
    public static final String IMPL_PATH_DELETE = WebservicePaths.getResourceImplPath(WebservicePaths.DAILYPLAN, PATH_DELETE);

    @POST
    @Path(PATH_MAIN)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postDailyPlanSubmissions(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);

    @POST
    @Path(PATH_DELETE)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postDeleteDailyPlanSubmissions(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);
}
