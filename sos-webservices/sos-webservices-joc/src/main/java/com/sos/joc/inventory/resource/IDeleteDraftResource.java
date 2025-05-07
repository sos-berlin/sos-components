package com.sos.joc.inventory.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.inventory.JocInventory;

public interface IDeleteDraftResource {

    public static final String PATH = "delete_draft";
    public static final String IMPL_PATH = JocInventory.getResourceImplPath(PATH);
    public static final String PATH_FOLDER = "delete_draft/folder";
    public static final String IMPL_PATH_FOLDER = JocInventory.getResourceImplPath(PATH_FOLDER);

    @POST
    @Path(PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse delete(@HeaderParam("X-Access-Token") final String accessToken, byte[] body);
    
    @POST
    @Path(PATH_FOLDER)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse deleteFolder(@HeaderParam("X-Access-Token") final String accessToken, byte[] body);

}
