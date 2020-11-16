package com.sos.joc.publish.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IRedeploy {

    @POST
    @Path("redeploy")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postDeploy(@HeaderParam("X-Access-Token") String xAccessToken, byte[] redeployFilter) throws Exception;
}
