package com.sos.joc.inventory.dependencies.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IUpdateDependencies {

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("dependencies/update")
    public JOCDefaultResponse postUpdateDependencies(@HeaderParam("X-Access-Token") String xAccessToken);
    
}
