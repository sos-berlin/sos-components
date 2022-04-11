package com.sos.joc.agents.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

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
