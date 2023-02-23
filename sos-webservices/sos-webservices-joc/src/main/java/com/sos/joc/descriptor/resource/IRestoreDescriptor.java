package com.sos.joc.descriptor.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IRestoreDescriptor {

    public static final String PATH_RESTORE = "descriptor/trash/restore";
    public static final String IMPL_PATH_RESTORE = "./descriptor/trash/restore";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postRestoreFromTrash(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);

}
