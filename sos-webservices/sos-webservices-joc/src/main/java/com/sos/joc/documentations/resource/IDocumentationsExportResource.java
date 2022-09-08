package com.sos.joc.documentations.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IDocumentationsExportResource {

    @Path("export")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public JOCDefaultResponse getExportDocumentations(@HeaderParam("X-Access-Token") String accessToken,
            @QueryParam("filename") String filename);

    @Path("export")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public JOCDefaultResponse postExportDocumentations(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);

    @Path("export/info")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public JOCDefaultResponse postExportInfo(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);
}
