
package com.sos.joc.controller.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

public interface IControllerLogResource {

    @POST
    @Path("log")
    @Consumes("application/json")
    public JOCDefaultResponse getLog(@HeaderParam("X-Access-Token") String xAccessToken, @HeaderParam("Accept-Encoding") String acceptEncoding,
            byte[] filterBytes);

    @POST
    @Path("log/download")
    @Consumes("application/json")
    public JOCDefaultResponse postDownloadLog(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
