package com.sos.joc.yade.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IYADEFileResource {

    @POST
    @Path("file")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postYADEFile(@HeaderParam("X-Access-Token") String accessToken, byte[] body);
}
