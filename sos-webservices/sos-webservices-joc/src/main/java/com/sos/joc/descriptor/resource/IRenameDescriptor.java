package com.sos.joc.descriptor.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IRenameDescriptor {

    public static final String PATH_RENAME = "rename";
    public static final String IMPL_PATH_RENAME = "./descriptor/rename";

    @POST
    @Path(PATH_RENAME)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postRename(@HeaderParam("X-Access-Token") final String accessToken, byte[] body);

}
