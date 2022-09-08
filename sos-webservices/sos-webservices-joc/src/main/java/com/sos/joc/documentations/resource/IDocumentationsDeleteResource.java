package com.sos.joc.documentations.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IDocumentationsDeleteResource {

    @POST
    @Path("delete")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse deleteDocumentations(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);
}
