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

    public static final String PATH_DELETE = "delete";
    public static final String IMPL_PATH_DELETE = JocInventory.getResourceImplPath(PATH_DELETE);
    public static final String PATH_UNDELETE = "undelete";
    public static final String IMPL_PATH_UNDELETE = JocInventory.getResourceImplPath(PATH_UNDELETE);

    @POST
    @Path(PATH_DELETE)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse delete(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);
    
    @POST
    @Path(PATH_UNDELETE)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse undelete(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);

}
