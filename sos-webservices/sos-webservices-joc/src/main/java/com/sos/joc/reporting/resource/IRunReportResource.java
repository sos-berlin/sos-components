
package com.sos.joc.reporting.resource;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IRunReportResource {

    public static final String PATH_SINGLE_RUN = "report/run";
    public static final String IMPL_SINGLE_RUN_PATH = WebservicePaths.getResourceImplPath(WebservicePaths.REPORTING, PATH_SINGLE_RUN);
    
    public static final String PATH = "reports/run";
    public static final String IMPL_PATH = WebservicePaths.getResourceImplPath(WebservicePaths.REPORTING, PATH);

    @POST
    @Path(PATH_SINGLE_RUN)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse runReport(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path(PATH)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse runReports(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
