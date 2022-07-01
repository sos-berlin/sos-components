package com.sos.joc.publish.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

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
