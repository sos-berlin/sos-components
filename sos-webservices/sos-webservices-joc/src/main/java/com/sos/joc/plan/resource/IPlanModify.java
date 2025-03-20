
package com.sos.joc.plan.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IPlanModify {

    @POST
    @Path("open")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse openPlan(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("close")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse closePlan(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("delete")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse deletePlan(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
