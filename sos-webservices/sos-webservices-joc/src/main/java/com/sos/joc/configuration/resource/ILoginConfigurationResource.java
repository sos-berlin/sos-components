package com.sos.joc.configuration.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

 
public interface ILoginConfigurationResource{

    @POST
    @Path("login")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postLoginConfiguration();

    @GET
    @Path("login")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getLoginConfiguration();
}
