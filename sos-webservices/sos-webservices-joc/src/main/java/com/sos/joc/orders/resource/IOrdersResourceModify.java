
package com.sos.joc.orders.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IOrdersResourceModify {

    @POST
    @Path("cancel")
    @Produces({ "application/json" })
    public JOCDefaultResponse postOrdersCancel(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("daily_plan/cancel")
    @Produces({ "application/json" })
    public JOCDefaultResponse postOrdersDailyPlanCancel(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

    
    @POST
    @Path("suspend")
    @Produces({ "application/json" })
    public JOCDefaultResponse postOrdersSuspend(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("resume")
    @Produces({ "application/json" })
    public JOCDefaultResponse postOrdersResume(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("remove_when_terminated")
    @Produces({ "application/json" })
    public JOCDefaultResponse postOrdersRemoveWhenTerminated(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("confirm")
    @Produces({ "application/json" })
    public JOCDefaultResponse postOrdersConfirm(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
}
