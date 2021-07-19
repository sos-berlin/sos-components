package com.sos.joc.keys.sign.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IGenerateKey {

    @POST
    @Path("generate")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postGenerateKey(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filter) throws Exception;
}
