package com.sos.joc.keys.sign.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IImportKey {

    @Path("import")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON) 
    public JOCDefaultResponse postImportKey(
            @HeaderParam("X-Access-Token") String xAccessToken, 
            @FormDataParam("file") FormDataBodyPart body,
            @FormDataParam("timeSpent") String timeSpent,
            @FormDataParam("ticketLink") String ticketLink,
            @FormDataParam("comment") String comment,
            @FormDataParam("importKeyFilter") String importKeyFilter) throws Exception;
}
