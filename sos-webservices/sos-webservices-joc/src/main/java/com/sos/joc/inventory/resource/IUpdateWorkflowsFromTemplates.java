package com.sos.joc.inventory.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.inventory.JocInventory;

public interface IUpdateWorkflowsFromTemplates {

    public static final String PATH = "workflows/update";
    public static final String IMPL_PATH = JocInventory.getResourceImplPath(PATH);

    @POST
    @Path(PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse update(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);

}