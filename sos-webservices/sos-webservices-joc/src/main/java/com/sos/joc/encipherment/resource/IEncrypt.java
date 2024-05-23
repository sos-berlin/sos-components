package com.sos.joc.encipherment.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IEncrypt {

    @POST
    @Path("encrypt")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postDeploy(@HeaderParam("X-Access-Token") String xAccessToken, byte[] encryptFilter) throws Exception;
}
