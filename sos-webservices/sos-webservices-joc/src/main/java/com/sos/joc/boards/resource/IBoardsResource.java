
package com.sos.joc.boards.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IBoardsResource {

    @POST
    @Path("boards")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postBoards(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
