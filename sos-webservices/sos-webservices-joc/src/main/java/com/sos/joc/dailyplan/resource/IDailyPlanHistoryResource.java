
package com.sos.joc.dailyplan.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;

public interface IDailyPlanHistoryResource {

    public static final String PATH_MAIN = "history";
    public static final String IMPL_PATH_MAIN = WebservicePaths.getResourceImplPath(WebservicePaths.DAILYPLAN, PATH_MAIN);

    public static final String PATH_SUBMISSIONS = PATH_MAIN + "/submissions";
    public static final String IMPL_PATH_SUBMISSIONS = WebservicePaths.getResourceImplPath(WebservicePaths.DAILYPLAN, PATH_SUBMISSIONS);

    public static final String PATH_SUBMISSIONS_ORDERS = PATH_SUBMISSIONS + "/orders";
    public static final String IMPL_PATH_SUBMISSIONS_ORDERS = WebservicePaths.getResourceImplPath(WebservicePaths.DAILYPLAN, PATH_SUBMISSIONS_ORDERS);

    @POST
    @Path(PATH_MAIN)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postDates(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);

    @POST
    @Path(PATH_SUBMISSIONS)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postSubmissions(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);

    @POST
    @Path(PATH_SUBMISSIONS_ORDERS)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postSubmissionsOrders(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);
}
