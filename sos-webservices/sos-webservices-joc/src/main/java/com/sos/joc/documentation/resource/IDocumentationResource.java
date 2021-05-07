package com.sos.joc.documentation.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IDocumentationResource {

    @GET
    // @Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.TEXT_PLAIN })
    @Path("{controllerId}/{accessToken}/{name : .+}")
    public JOCDefaultResponse postDocumentation(@PathParam("accessToken") String accessToken,
            @PathParam("controllerId") String jobschedulerId, @PathParam("name") String name) throws Exception;
}
