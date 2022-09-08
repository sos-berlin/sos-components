package com.sos.joc.documentation.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IDocumentationEditResource {

    @Path("edit")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public JOCDefaultResponse postDocumentationEdit(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);
    
}
