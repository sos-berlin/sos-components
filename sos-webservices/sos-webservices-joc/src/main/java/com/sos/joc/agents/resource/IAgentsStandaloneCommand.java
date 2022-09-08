package com.sos.joc.agents.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IAgentsStandaloneCommand {

    @POST
    @Path("inventory/revoke")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse postRevoke(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("inventory/enable")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse postEnable(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("inventory/disable")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse postDisable(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
}
