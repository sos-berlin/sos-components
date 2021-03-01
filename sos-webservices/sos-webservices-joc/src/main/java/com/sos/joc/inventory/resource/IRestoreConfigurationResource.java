package com.sos.joc.inventory.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.inventory.JocInventory;

public interface IRestoreConfigurationResource {

    public static final String TRASH_PATH = "trash/restore";
    public static final String TRASH_IMPL_PATH = JocInventory.getResourceImplPath(TRASH_PATH);

    @POST
    @Path(TRASH_PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse restore(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);

}
