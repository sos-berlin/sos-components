package com.sos.joc.jobscheduler.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.model.jobscheduler.UrlParameter;

public interface IJobSchedulerResourceModifyJobSchedulerCluster {

    @POST
    @Path("cluster/switchover")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse postJobschedulerSwitchOver(@HeaderParam("X-Access-Token") String xAccessToken, UrlParameter urlParameter);

}
