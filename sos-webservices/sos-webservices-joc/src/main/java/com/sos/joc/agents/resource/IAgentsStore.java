package com.sos.joc.agents.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IAgentsStore {

    @POST
    @Path("store")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse store(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("inventory/store")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse inventoryStore(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("inventory/cluster/store")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse clusterInventoryStore(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
}
