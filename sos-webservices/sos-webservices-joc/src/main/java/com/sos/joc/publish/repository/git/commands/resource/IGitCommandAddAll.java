package com.sos.joc.publish.repository.git.commands.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IGitCommandAddAll {

    @POST
    @Path("add")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postCommandAdd(@HeaderParam("X-Access-Token") String xAccessToken, byte[] commonFilter) throws Exception;
}
