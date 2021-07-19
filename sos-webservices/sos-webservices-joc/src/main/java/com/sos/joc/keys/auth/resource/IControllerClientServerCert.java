package com.sos.joc.keys.auth.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IControllerClientServerCert {

    @POST
    @Path("certificate")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postCreateControllerClientServerCert(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filter) throws Exception;

}
