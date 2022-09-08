package com.sos.joc.publish.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface ISetVersion {

    @POST
    @Path("set_version")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postSetVersion(@HeaderParam("X-Access-Token") String xAccessToken, byte[] setVersionFilter) throws Exception;
}
