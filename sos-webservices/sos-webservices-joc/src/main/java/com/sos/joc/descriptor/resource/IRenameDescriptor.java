package com.sos.joc.descriptor.resource;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.inventory.JocInventory;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IRenameDescriptor {

    public static final String PATH_RENAME = "descriptor/rename";
    public static final String IMPL_PATH_RENAME = JocInventory.getResourceImplPath(PATH_RENAME);

    @POST
    @Path(PATH_RENAME)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse rename(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);

}
