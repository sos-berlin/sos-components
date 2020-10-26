package com.sos.joc.publish.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IExportResource {

    @Path("export")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public JOCDefaultResponse postExportConfiguration(@HeaderParam("X-Access-Token") String xAccessToken, @HeaderParam("filename") 
            String filename, byte[] filter) throws Exception;

    @Path("export")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public JOCDefaultResponse getExportConfiguration(@HeaderParam("X-Access-Token") String xAccessToken,
            @QueryParam("accessToken") String accessToken, @QueryParam("filename") String filename, 
            @QueryParam("configurations") String configurations, @QueryParam("deployments") String deployments) throws Exception;
}
