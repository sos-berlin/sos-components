package com.sos.joc.inventory.resource;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.inventory.JocInventory;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IRevalidateResource {

    public static final String PATH = "revalidate/folder";
    public static final String IMPL_PATH = JocInventory.getResourceImplPath(PATH);

    @POST
    @Path(PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse revalidate(@HeaderParam("X-Access-Token") final String xAccessToken, final byte[] body);
}
