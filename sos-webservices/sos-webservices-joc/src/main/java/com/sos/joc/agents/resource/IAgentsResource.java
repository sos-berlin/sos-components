package com.sos.joc.agents.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IAgentsResource {

    //old
    @POST
    @Path("p")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse post2(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("inventory")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse post(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("names")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse postNames(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
}
