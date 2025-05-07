package com.sos.joc.boards.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IQuickSearchResource {

    @POST
    @Path("boards/quick/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postSearch(@HeaderParam("X-Access-Token") final String accessToken, byte[] body);

}
