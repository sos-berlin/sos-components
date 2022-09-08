package com.sos.joc.publish.repository.git.commands.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IGitCommandCommit {

    @POST
    @Path("commit")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postCommandCommit(@HeaderParam("X-Access-Token") String xAccessToken, byte[] commitFilter) throws Exception;
}
