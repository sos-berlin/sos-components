
package com.sos.webservices.order.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.webservices.order.initiator.model.OrderTemplate;
  
public interface IAddOrderResource {

    @POST
    @Path("add_orders")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postAddOrders(@HeaderParam("X-Access-Token") String accessToken,
            OrderTemplate orderTemplate)  ;
}
