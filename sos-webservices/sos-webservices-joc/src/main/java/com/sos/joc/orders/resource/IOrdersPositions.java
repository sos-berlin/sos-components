
package com.sos.joc.orders.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IOrdersPositions {

    @POST
    @Path("resume/positions")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse resumeOrderPositions(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("add/positions")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse addOrderPositions(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
