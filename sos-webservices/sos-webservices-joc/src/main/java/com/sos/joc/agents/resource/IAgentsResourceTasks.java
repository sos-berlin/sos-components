package com.sos.joc.agents.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IAgentsResourceTasks {

    @POST
    @Path("tasks")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse getTasks(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
}
