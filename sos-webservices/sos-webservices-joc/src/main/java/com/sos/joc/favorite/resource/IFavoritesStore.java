package com.sos.joc.favorite.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IFavoritesStore {

    @POST
    @Path("store")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse storeFavorites(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);
    
    @POST
    @Path("rename")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse renameFavorites(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);
}
