package com.sos.joc.inventory.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.inventory.JocInventory;

public interface IDeleteConfigurationResource {

    public static final String PATH_DELETE = "remove";
    public static final String IMPL_PATH_DELETE = JocInventory.getResourceImplPath(PATH_DELETE);
    public static final String PATH_FOLDER_DELETE = "remove/folder";
    public static final String IMPL_PATH_FOLDER_DELETE = JocInventory.getResourceImplPath(PATH_FOLDER_DELETE);
    public static final String PATH_TRASH_DELETE = "trash/delete";
    public static final String IMPL_PATH_TRASH_DELETE = JocInventory.getResourceImplPath(PATH_TRASH_DELETE);
    public static final String PATH_TRASH_FOLDER_DELETE = "trash/delete/folder";
    public static final String IMPL_PATH_TRASH_FOLDER_DELETE = JocInventory.getResourceImplPath(PATH_TRASH_FOLDER_DELETE);

    @POST
    @Path(PATH_DELETE)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse remove(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);
    
    @POST
    @Path(PATH_FOLDER_DELETE)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse removeFolder(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);
    
    @POST
    @Path(PATH_TRASH_DELETE)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse delete(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);
    
    @POST
    @Path(PATH_TRASH_FOLDER_DELETE)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse deleteFolder(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);

}
