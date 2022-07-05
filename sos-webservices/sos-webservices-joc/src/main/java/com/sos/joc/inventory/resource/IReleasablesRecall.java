package com.sos.joc.inventory.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IReleasablesRecall {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("releasables/recall")
    public JOCDefaultResponse postRecall(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filter);
}
