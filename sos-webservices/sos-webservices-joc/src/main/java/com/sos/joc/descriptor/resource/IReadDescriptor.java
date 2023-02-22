package com.sos.joc.descriptor.resource;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.inventory.JocInventory;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IReadDescriptor {

    public static final String PATH_READ = "descriptor/read";
    public static final String IMPL_PATH_READ = JocInventory.getResourceImplPath(PATH_READ);

    public static final String PATH_READ_FOLDER = "descriptor/read/folder";
    public static final String IMPL_PATH_READ_FOLDER = JocInventory.getResourceImplPath(PATH_READ_FOLDER);

    public static final String PATH_TRASH_READ = "descriptor/trash/read";
    public static final String IMPL_PATH_TRASH_READ = JocInventory.getResourceImplPath(PATH_TRASH_READ);

    public static final String PATH_TRASH_READ_FOLDER = "descriptor/trash/read/folder";
    public static final String IMPL_PATH_TRASH_READ_FOLDER = JocInventory.getResourceImplPath(PATH_TRASH_READ_FOLDER);

    @POST
    @Path(PATH_READ)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse read(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);

    @POST
    @Path(PATH_READ_FOLDER)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse readFolder(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);

    @POST
    @Path(PATH_TRASH_READ)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse readTrash(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);

    @POST
    @Path(PATH_TRASH_READ_FOLDER)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse readTrashFolder(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);

}
