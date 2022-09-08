package com.sos.joc.keys.auth.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IOnetimeToken {

    @POST
    @Path("create")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postCreateToken(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filter);

    @POST
    @Path("show")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postShowToken(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filter);
}
