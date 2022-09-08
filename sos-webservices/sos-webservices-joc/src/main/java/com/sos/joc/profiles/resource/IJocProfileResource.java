package com.sos.joc.profiles.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

 
public interface IJocProfileResource{

  
    @POST
    @Path("store")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postProfileStore(@HeaderParam("X-Access-Token") String xAccessToken, byte[] body);
  
    
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postProfile(@HeaderParam("X-Access-Token") String xAccessToken, byte[] body);
 
}
