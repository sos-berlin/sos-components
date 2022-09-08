package com.sos.joc.publish.history.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IShowDeploymentHistory {

    @POST
    @Path("history")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postShowDeploymentHistory(@HeaderParam("X-Access-Token") String xAccessToken, byte[] showDepHistoryFilter) throws Exception;
}
