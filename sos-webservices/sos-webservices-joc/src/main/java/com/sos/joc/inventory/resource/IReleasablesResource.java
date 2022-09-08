package com.sos.joc.inventory.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.inventory.JocInventory;

public interface IReleasablesResource {

    public static final String PATH = "releasables";
    public static final String IMPL_PATH = JocInventory.getResourceImplPath(PATH);
    public static final String PATH_OLD = "releasables/old";
    public static final String IMPL_PATH_OLD = JocInventory.getResourceImplPath(PATH_OLD);

    @POST
    @Path(PATH_OLD)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse releasables(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);
    
    @POST
    @Path(PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse releasablesTree(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);


}
