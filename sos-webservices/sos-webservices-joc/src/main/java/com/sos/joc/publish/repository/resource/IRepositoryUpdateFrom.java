package com.sos.joc.publish.repository.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IRepositoryUpdateFrom {

    @POST
    @Path("update")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postUpdate(@HeaderParam("X-Access-Token") String xAccessToken, byte[] updateFromFilter) throws Exception;
}
