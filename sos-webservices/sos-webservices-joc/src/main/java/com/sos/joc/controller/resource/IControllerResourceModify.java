package com.sos.joc.controller.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IControllerResourceModify {

    @POST
    @Path("terminate")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse postJobschedulerTerminate(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

    @POST
    @Path("restart")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse postJobschedulerRestartTerminate(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

    @POST
    @Path("abort")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse postJobschedulerAbort(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

    @POST
    @Path("abort_and_restart")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse postJobschedulerRestartAbort(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
