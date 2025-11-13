package com.sos.joc.note.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IAddPost {

    @POST
    @Path("post/add")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse add(@HeaderParam("X-Access-Token") String accessToken, byte[] body);
}
