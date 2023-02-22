package com.sos.joc.descriptor.resource;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.inventory.JocInventory;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface ICopyDescriptor {

    public static final String PATH_COPY = "descriptor/copy";
    public static final String IMPL_PATH_COPY = JocInventory.getResourceImplPath(PATH_COPY);

    @POST
    @Path(PATH_COPY)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse copy(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);

}
