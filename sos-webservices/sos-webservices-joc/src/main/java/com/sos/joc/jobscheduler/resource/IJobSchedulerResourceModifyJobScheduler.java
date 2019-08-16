package com.sos.joc.jobscheduler.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.model.jobscheduler.UrlParameter;

public interface IJobSchedulerResourceModifyJobScheduler {

    @POST
    @Path("terminate")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse postJobschedulerTerminate(@HeaderParam("X-Access-Token") String xAccessToken, UrlParameter urlParameter);

    @POST
    @Path("restart")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse postJobschedulerRestartTerminate(@HeaderParam("X-Access-Token") String xAccessToken, UrlParameter urlParameter);

    @POST
    @Path("abort")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse postJobschedulerAbort(@HeaderParam("X-Access-Token") String xAccessToken, UrlParameter urlParameter);

    @POST
    @Path("abort_and_restart")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse postJobschedulerRestartAbort(@HeaderParam("X-Access-Token") String xAccessToken, UrlParameter urlParameter);

}
