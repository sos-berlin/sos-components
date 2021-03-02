package com.sos.joc.inventory.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
