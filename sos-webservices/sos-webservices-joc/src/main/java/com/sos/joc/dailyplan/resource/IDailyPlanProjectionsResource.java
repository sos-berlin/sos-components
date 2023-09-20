
package com.sos.joc.dailyplan.resource;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IDailyPlanProjectionsResource {

    public static final String PATH = "projections";
    public static final String PATH_CALENDAR = "projections/calendar";
    public static final String PATH_SCHEDULES = "projections/schedules";
    public static final String PATH_SCHEDULE = "projections/schedule";
    public static final String PATH_RECREATE = "projections/recreate";

    public static final String IMPL_PATH = WebservicePaths.getResourceImplPath(WebservicePaths.DAILYPLAN, PATH);
    public static final String IMPL_PATH_CALENDAR = WebservicePaths.getResourceImplPath(WebservicePaths.DAILYPLAN, PATH_CALENDAR);
    public static final String IMPL_PATH_SCHEDULES = WebservicePaths.getResourceImplPath(WebservicePaths.DAILYPLAN, PATH_SCHEDULES);
    public static final String IMPL_PATH_SCHEDULE = WebservicePaths.getResourceImplPath(WebservicePaths.DAILYPLAN, PATH_SCHEDULE);
    public static final String IMPL_PATH_RECREATE = WebservicePaths.getResourceImplPath(WebservicePaths.DAILYPLAN, PATH_RECREATE);

    @POST
    @Path(PATH_CALENDAR)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse calendarProjections(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

    @POST
    @Path(PATH_SCHEDULES)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse schedulesProjections(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

    @POST
    @Path(PATH_SCHEDULE)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse scheduleProjections(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

    @POST
    @Path(PATH_RECREATE)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse recreate(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
