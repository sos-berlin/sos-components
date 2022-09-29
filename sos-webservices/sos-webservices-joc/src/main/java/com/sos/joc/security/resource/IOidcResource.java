
package com.sos.joc.security.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IOidcResource {

    @POST
    @Path("identityproviders")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postIdentityproviders();
 
    @POST
    @Path("identityclients")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postIdentityclient2(byte[] body);
 
    @POST
    @Path("identityclient")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postIdentityclient(byte[] body);
 
    @POST
    @Path("import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public JOCDefaultResponse postImportDocumentations(
            @HeaderParam("X-Access-Token") String xAccessToken,
            @FormDataParam("identityServiceName") String identityServiceName,
            @FormDataParam("file") FormDataBodyPart file,
            @FormDataParam("timeSpent") String timeSpent,
            @FormDataParam("ticketLink") String ticketLink,
            @FormDataParam("comment") String comment);
 
    @GET
    @Path("icon/{identityServiceName : .+}")
    public JOCDefaultResponse getIcon(@PathParam("identityServiceName") String identityServiceName);
 
}

