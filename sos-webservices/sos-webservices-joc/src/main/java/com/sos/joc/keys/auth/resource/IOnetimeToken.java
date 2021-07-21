package com.sos.joc.keys.auth.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IOnetimeToken {

    @POST
    @Path("create")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postCreateToken(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filter) throws Exception;

    @POST
    @Path("show")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postShowToken(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filter) throws Exception;
}
