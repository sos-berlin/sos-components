
package com.sos.joc.orders.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

 
public interface IOrdersResourceOverviewSnapshot {

    @POST
    @Path("overview/snapshot")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postOrdersOverviewSnapshot(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
}
