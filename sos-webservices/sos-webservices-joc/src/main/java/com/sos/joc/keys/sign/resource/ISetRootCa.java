package com.sos.joc.keys.sign.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface ISetRootCa {

    @POST
    @Path("store")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postSetRootCa(@HeaderParam("X-Access-Token") String xAccessToken, byte[] setKeyFilter) throws Exception;
}
