package com.sos.joc.agents.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface ISubAgentClusterOrdering {

    @POST
    @Path("cluster/ordering")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse ordering(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
}
