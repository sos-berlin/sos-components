package com.sos.joc.descriptor.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IReadDescriptor {

    public static final String PATH_READ = "read";
    public static final String IMPL_PATH_READ = "./descriptor/read";

    public static final String PATH_TRASH_READ = "trash/read";
    public static final String IMPL_PATH_TRASH_READ = "./descriptor/trash/read";

    @POST
    @Path(PATH_READ)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse read(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);

    @POST
    @Path(PATH_TRASH_READ)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse readTrash(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);

}
