package com.sos.joc.agent.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

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
