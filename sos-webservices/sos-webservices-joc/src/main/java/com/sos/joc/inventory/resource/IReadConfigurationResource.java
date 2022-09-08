package com.sos.joc.inventory.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.inventory.JocInventory;

public interface IReadConfigurationResource {

    public static final String PATH = "read/configuration";
    public static final String IMPL_PATH = JocInventory.getResourceImplPath(PATH);
    public static final String TRASH_PATH = "trash/read/configuration";
    public static final String TRASH_IMPL_PATH = JocInventory.getResourceImplPath(TRASH_PATH);

    @POST
    @Path(PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse read(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);
    
    @POST
    @Path(TRASH_PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse readTrash(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);

}
