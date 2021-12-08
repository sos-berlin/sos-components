package com.sos.joc.agent.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface ISubAgentStoreResource {

    @POST
    @Path("subagents/store")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse store(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
}
