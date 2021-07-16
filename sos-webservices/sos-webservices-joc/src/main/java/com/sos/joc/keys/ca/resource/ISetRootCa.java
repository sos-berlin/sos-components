package com.sos.joc.keys.ca.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface ISetRootCa {

    @POST
    @Path("store")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postSetRootCa(@HeaderParam("X-Access-Token") String xAccessToken, byte[] setKeyFilter) throws Exception;
}
