package com.sos.joc.publish.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IExportFolderResource {

    @Path("export/folder")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public JOCDefaultResponse postExportFolder(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filter) throws Exception;

    @Path("export/folder")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public JOCDefaultResponse getExportFolder(@HeaderParam("X-Access-Token") String xAccessToken,
            @QueryParam("accessToken") String accessToken, @QueryParam("exportFilter") String exportFilter) throws Exception;
}
