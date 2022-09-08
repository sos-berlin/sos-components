package com.sos.joc.controller.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IControllerResourceModifyCluster {

    @POST
    @Path("cluster/switchover")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse postJobschedulerSwitchOver(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("cluster/appoint_nodes")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse postJobschedulerAppointNodes(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
