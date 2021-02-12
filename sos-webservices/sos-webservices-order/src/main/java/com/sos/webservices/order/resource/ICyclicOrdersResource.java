
package com.sos.webservices.order.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
   
public interface ICyclicOrdersResource {

    @POST
    @Path("cyclic_orders")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postCyclicOrders(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes)  ;
}
