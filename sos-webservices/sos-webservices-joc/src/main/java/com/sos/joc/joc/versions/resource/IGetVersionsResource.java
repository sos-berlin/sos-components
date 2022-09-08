package com.sos.joc.joc.versions.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IGetVersionsResource {

    @Path("versions")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public JOCDefaultResponse postGetVersions(@HeaderParam("X-Access-Token") String xAccessToken, byte[] versionsFilter);
}
