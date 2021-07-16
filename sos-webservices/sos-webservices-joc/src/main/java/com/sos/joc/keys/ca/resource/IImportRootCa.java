package com.sos.joc.keys.ca.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IImportRootCa {

    @Path("import")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON) 
    public JOCDefaultResponse postImportRootCa (
            @HeaderParam("X-Access-Token") String xAccessToken, 
            @FormDataParam("file") FormDataBodyPart body,
            @FormDataParam("timeSpent") String timeSpent,
            @FormDataParam("ticketLink") String ticketLink,
            @FormDataParam("comment") String comment) throws Exception;
    }
