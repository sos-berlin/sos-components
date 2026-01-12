package com.sos.joc.help.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IHelpResource {

    @GET
    @Path("{path : .+}")
    public JOCDefaultResponse postHelpFile(@HeaderParam("X-Access-Token") String xAccessToken, @HeaderParam("Referer") String referer, @PathParam("path") String path);
}
