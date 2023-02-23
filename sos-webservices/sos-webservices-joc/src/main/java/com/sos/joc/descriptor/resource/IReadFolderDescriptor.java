package com.sos.joc.descriptor.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IReadFolderDescriptor {

    public static final String PATH_READ_FOLDER = "read/folder";
    public static final String IMPL_PATH_READ_FOLDER = "./descriptor/read/folder";

    public static final String PATH_TRASH_READ_FOLDER = "trash/read/folder";
    public static final String IMPL_PATH_TRASH_READ_FOLDER = "./descriptor/trash/read/folder";

    @POST
    @Path(PATH_READ_FOLDER)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postReadFolder(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);

    @POST
    @Path(PATH_TRASH_READ_FOLDER)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postReadTrashFolder(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);

}
