package com.sos.joc.documentations.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IDocumentationsImportResource {

    @Path("import")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON) 
    public JOCDefaultResponse postImportDocumentations(
            @HeaderParam("X-Access-Token") String xAccessToken,
            @FormDataParam("accessToken") String accessToken, 
            @FormDataParam("folder") String folder, 
            @FormDataParam("file") FormDataBodyPart body,
            @FormDataParam("timeSpent") String timeSpent,
            @FormDataParam("ticketLink") String ticketLink,
            @FormDataParam("comment") String comment);
}
