package com.sos.joc.documentation.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IDocumentationUsedResource {

    @Path("used")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public JOCDefaultResponse postDocumentationUsed(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);
    
}
