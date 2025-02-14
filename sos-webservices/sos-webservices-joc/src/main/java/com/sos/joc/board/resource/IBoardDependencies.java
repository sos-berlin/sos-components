
package com.sos.joc.board.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IBoardDependencies {

    @POST
    @Path("board/dependencies")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postBoardDeps(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
