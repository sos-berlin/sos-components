package com.sos.joc.favorite.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IFavoritesOrdering {

    @POST
    @Path("ordering")
    @Consumes("application/json")
    @Produces({ "application/json" })
    public JOCDefaultResponse ordering(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
}
