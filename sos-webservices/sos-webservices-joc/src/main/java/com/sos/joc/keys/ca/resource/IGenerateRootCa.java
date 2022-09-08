package com.sos.joc.keys.ca.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IGenerateRootCa {

    @POST
    @Path("generate")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postGenerateRootCa(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filter) throws Exception;

}
