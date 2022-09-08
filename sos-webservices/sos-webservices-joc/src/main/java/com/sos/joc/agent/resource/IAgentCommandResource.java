package com.sos.joc.agent.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IAgentCommandResource {

    @POST
    @Path("reset")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse reset(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    //old
    @POST
    @Path("remove")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse remove(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("delete")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse delete(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
}
