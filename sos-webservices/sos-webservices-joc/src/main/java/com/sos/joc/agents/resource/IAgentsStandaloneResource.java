package com.sos.joc.agents.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IAgentsStandaloneResource {

    //old
    @POST
    @Path("p")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse post2(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("inventory")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse post(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
}
