package com.sos.joc.keys.sign.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IDeleteKey {

    @POST
    @Path("delete")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postDeleteKey(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filter) throws Exception;
}
