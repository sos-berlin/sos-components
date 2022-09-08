package com.sos.joc.publish.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IRedeploy {

    @POST
    @Path("redeploy")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postRedeploy(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filter);
    
    @POST
    @Path("synchronize")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postSync(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filter);
}
