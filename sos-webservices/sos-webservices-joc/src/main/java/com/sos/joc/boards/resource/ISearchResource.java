package com.sos.joc.boards.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.inventory.JocInventory;

public interface ISearchResource {

    public static final String PATH = "search";
    public static final String IMPL_PATH = JocInventory.getResourceImplPath(PATH);

    @POST
    @Path("boards/" + PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postSearch(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);

}
