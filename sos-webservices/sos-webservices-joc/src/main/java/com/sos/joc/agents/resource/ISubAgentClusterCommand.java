package com.sos.joc.agents.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface ISubAgentClusterCommand {

    @POST
    @Path("cluster/delete")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse delete(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("cluster/revoke")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse revoke(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
}
