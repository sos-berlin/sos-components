package com.sos.joc.inventory.changes.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IChangesStore {

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("store")
    public JOCDefaultResponse postChangesStore(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filter);

}
