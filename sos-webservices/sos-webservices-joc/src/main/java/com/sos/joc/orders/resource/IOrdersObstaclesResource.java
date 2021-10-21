
package com.sos.joc.orders.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IOrdersObstaclesResource {

    @POST
    @Path("obstacles")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postOrders(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
