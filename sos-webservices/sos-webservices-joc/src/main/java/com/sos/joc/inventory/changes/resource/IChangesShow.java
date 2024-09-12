package com.sos.joc.inventory.changes.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IChangesShow {

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postShowChanges(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filter);
}
