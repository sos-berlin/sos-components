package com.sos.joc.publish.repository.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IRepositoryStore {

    @POST
    @Path("store")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postStore(@HeaderParam("X-Access-Token") String xAccessToken, byte[] copyToFilter) throws Exception;
}
