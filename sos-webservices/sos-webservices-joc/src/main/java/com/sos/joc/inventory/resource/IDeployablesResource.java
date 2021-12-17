package com.sos.joc.inventory.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
    public JOCDefaultResponse deployables(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);
    
    @POST
    @Path(PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse deployablesTree(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);

}
