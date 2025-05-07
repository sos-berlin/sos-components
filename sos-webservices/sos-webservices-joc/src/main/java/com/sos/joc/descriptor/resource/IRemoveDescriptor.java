package com.sos.joc.descriptor.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IRemoveDescriptor {

    public static final String PATH_REMOVE = "remove";
    public static final String IMPL_PATH_REMOVE = "./descriptor/remove";

    public static final String PATH_REMOVE_FOLDER = "remove/folder";
    public static final String IMPL_PATH_REMOVE_FOLDER = "./descriptor/remove/folder";

    public static final String PATH_TRASH_DELETE = "trash/delete";
    public static final String IMPL_PATH_TRASH_DELETE = "./descriptor/trash/delete";

    public static final String PATH_TRASH_DELETE_FOLDER = "trash/delete/folder";
    public static final String IMPL_PATH_TRASH_DELETE_FOLDER = "./descriptor/trash/delete/folder";

    @POST
    @Path(PATH_REMOVE)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse remove(@HeaderParam("X-Access-Token") final String accessToken, byte[] body);

    @POST
    @Path(PATH_REMOVE_FOLDER)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse removeFolder(@HeaderParam("X-Access-Token") final String accessToken, byte[] body);

    @POST
    @Path(IRemoveDescriptor.PATH_TRASH_DELETE)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse deleteFromTrash(@HeaderParam("X-Access-Token") final String accessToken, byte[] body);

    @POST
    @Path(PATH_TRASH_DELETE_FOLDER)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse deleteFolderFromTrash(@HeaderParam("X-Access-Token") final String accessToken, byte[] body);

}
