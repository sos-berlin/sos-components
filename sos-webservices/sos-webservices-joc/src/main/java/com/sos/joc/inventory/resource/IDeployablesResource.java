package com.sos.joc.inventory.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.inventory.JocInventory;

public interface IDeployablesResource {

    public static final String PATH = "deployables";
    public static final String IMPL_PATH = JocInventory.getResourceImplPath(PATH);
    public static final String PATH_OLD = "deployables/old";
    public static final String IMPL_PATH_OLD = JocInventory.getResourceImplPath(PATH_OLD);

    @POST
    @Path(PATH_OLD)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse deployables(@HeaderParam("X-Access-Token") final String accessToken, byte[] body);
    
    @POST
    @Path(PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse deployablesTree(@HeaderParam("X-Access-Token") final String accessToken, byte[] body);

}
