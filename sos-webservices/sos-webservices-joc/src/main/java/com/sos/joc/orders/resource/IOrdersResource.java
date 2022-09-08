
package com.sos.joc.orders.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;


 
public interface IOrdersResource {

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postOrders(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
