package com.sos.joc.agents.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

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
