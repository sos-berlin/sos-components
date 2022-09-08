package com.sos.joc.agents.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IAgentsOrdering {

    @POST
    @Path("inventory/ordering")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse standaloneOrdering(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    
    @POST
    @Path("inventory/cluster/ordering")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse clusterOrdering(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
}
