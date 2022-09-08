package com.sos.joc.publish.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IDeploy {

    @POST
    @Path("deploy")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postDeploy(@HeaderParam("X-Access-Token") String xAccessToken, byte[] deployFilter) throws Exception;
}
