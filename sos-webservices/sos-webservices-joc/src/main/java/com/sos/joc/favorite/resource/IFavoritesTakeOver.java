package com.sos.joc.favorite.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IFavoritesTakeOver {

    @POST
    @Path("take_over")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse takeOverFavorites(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);
}
