package com.sos.joc.favorite.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IFavoritesShare {

    @POST
    @Path("share")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse shareFavorites(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);
    
    @POST
    @Path("make_private")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse unshareFavorites(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);
}
