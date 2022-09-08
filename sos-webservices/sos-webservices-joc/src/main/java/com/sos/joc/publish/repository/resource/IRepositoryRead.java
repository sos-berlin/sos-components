package com.sos.joc.publish.repository.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IRepositoryRead {

    @POST
    @Path("read")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postRead(@HeaderParam("X-Access-Token") String xAccessToken, byte[] readFromFilter) throws Exception;
}
