package com.sos.joc.configurations.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

 
public interface IJocConfigurationsResource{

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postConfigurations(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("delete")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postConfigurationsDelete(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("profiles")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postConfigurationsProfiles(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
 
}
