package com.sos.joc.inventory.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.inventory.JocInventory;

public interface IValidateResource {

    public static final String PATH = "validate";
    public static final String IMPL_PATH = JocInventory.getResourceImplPath(PATH);

    @POST
    @Path("{objectType}/" + PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse validate(@HeaderParam("X-Access-Token") final String accessToken, @PathParam("objectType") final String objectType,
            final byte[] requestBody);
}
