
package com.sos.joc.reporting.resource;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IDeleteReportsResource {

    public static final String PATH = "reports/delete";
    public static final String IMPL_PATH = WebservicePaths.getResourceImplPath(WebservicePaths.REPORTING, PATH);

    @POST
    @Path(PATH)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse delete(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
}
