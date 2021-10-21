
package com.sos.joc.order.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IOrderObstaclesResource {

    @POST
    @Path("obstacles")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postOrder(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
