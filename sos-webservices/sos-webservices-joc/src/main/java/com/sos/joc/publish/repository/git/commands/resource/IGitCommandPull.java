package com.sos.joc.publish.repository.git.commands.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IGitCommandPull {

    @POST
    @Path("pull")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postCommandPull(@HeaderParam("X-Access-Token") String xAccessToken, byte[] commonFilter) throws Exception;
}
