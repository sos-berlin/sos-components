package com.sos.joc.keys.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IShowKey {

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postShowKey(@HeaderParam("X-Access-Token") String xAccessToken) throws Exception;
}
