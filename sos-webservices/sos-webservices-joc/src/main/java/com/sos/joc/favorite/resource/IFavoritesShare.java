package com.sos.joc.favorite.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
