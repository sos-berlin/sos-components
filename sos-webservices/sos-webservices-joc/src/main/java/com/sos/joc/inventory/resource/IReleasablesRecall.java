package com.sos.joc.inventory.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IReleasablesRecall {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("releasables/recall")
    public JOCDefaultResponse postRecall(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filter);
}
