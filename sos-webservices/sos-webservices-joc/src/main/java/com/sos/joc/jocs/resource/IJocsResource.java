package com.sos.joc.jocs.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IJocsResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postJocs(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);

}
