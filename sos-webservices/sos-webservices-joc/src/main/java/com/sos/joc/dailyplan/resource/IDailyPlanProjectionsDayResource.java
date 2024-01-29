
package com.sos.joc.dailyplan.resource;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IDailyPlanProjectionsDayResource {

    public static final String PATH = "projections/day";
    public static final String IMPL_PATH = WebservicePaths.getResourceImplPath(WebservicePaths.DAILYPLAN, PATH);

    @POST
    @Path(PATH)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse dayProjection(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    //alias
    @POST
    @Path("projections/date")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse dateProjection(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
