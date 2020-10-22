
package com.sos.joc.orders.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IOrdersResourceModify {

    @POST
    @Path("cancel")
    @Produces({ "application/json" })
    public JOCDefaultResponse postOrdersCancel(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
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
}
