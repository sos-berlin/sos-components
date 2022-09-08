package com.sos.joc.publish.repository.git.commands.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IGitCommandClone {

    @POST
    @Path("clone")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postCommandClone(@HeaderParam("X-Access-Token") String xAccessToken, byte[] cloneFilter) throws Exception;
}
