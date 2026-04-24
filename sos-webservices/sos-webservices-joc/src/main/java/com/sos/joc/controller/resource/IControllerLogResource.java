
package com.sos.joc.controller.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

public interface IControllerLogResource {

    @POST
    @Path("log")
    //@CompressedAlready
    @Consumes("application/json")
    public JOCDefaultResponse getLog(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    
    @POST
    @Path("log/download")
    //@CompressedAlready
    @Consumes("application/json")
    //@Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getDownloadLog(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
