package com.sos.joc.publish.repository.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IRepositoryUpdateFrom {

    @POST
    @Path("update")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postUpdate(@HeaderParam("X-Access-Token") String xAccessToken, byte[] updateFromFilter) throws Exception;
}
