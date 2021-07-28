package com.sos.joc.keys.auth.resource;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface ICreateClientServerCert {

    @POST
    @Path("certificate/create")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postCreateClientServerCert(@Context HttpServletRequest request, @HeaderParam("X-Onetime-Token") String token, byte[] filter)
            throws Exception;

}
