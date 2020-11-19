package com.sos.joc.publish.history.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IShowDeploymentHistory {

    @POST
    @Path("history")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postShowDeploymentHistory(@HeaderParam("X-Access-Token") String xAccessToken, byte[] showDepHistoryFilter) throws Exception;
}
