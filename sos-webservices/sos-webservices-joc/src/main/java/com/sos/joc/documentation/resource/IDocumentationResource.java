package com.sos.joc.documentation.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IDocumentationResource {

    @GET
    // @Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.TEXT_PLAIN })
    @Path("{accessToken}/{path : .+}")
    public JOCDefaultResponse postDocumentation(@PathParam("accessToken") String accessToken, @PathParam("path") String path);
}
