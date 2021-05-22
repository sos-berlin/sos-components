package com.sos.joc.documentation.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IDocumentationEditResource {

    @Path("edit")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public JOCDefaultResponse postDocumentationEdit(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);
    
}
