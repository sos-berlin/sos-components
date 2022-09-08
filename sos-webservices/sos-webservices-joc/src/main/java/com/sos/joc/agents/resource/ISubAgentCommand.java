package com.sos.joc.agents.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface ISubAgentCommand {

    @POST
    @Path("inventory/cluster/subagents/delete")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse delete(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("inventory/cluster/subagents/revoke")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse revoke(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("inventory/cluster/subagents/enable")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse enable(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("inventory/cluster/subagents/disable")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse disable(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("inventory/cluster/subagent/reset")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse reset(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
}
