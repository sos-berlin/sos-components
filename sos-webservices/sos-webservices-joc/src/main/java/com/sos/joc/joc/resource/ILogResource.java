package com.sos.joc.joc.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.annotation.Compress;
import com.sos.joc.classes.JOCDefaultResponse;

public interface ILogResource {

    @Path("log")
    @POST
    @Compress
    //@Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postLog(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @Path("log")
    @GET
    @Compress
    //@Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getLog(@HeaderParam("X-Access-Token") String accessToken, @QueryParam("accessToken") String queryAccessToken, @QueryParam("filename") String filename);
    
    @Path("logs")
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postLogs(@HeaderParam("X-Access-Token") String accessToken);
}
