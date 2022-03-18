package com.sos.joc.agents.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

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
