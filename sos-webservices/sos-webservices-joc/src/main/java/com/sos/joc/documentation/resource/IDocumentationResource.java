package com.sos.joc.documentation.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IDocumentationResource {

    @GET
    // @Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.TEXT_PLAIN })
    @Path("{path : .+}")
    public JOCDefaultResponse postDocumentation(@HeaderParam("X-Access-Token") String xAccessToken, @HeaderParam("Referer") String referer, @PathParam("path") String path);
}
