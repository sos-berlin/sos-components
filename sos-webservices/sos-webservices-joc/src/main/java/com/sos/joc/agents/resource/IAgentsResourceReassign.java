package com.sos.joc.agents.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IAgentsResourceReassign {

    //old
    @POST
    @Path("reassign")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse reAssign2(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("inventory/reassign")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse reAssign(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
}
