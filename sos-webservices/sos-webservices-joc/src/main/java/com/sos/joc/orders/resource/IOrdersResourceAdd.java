
package com.sos.joc.orders.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IOrdersResourceAdd {

    @POST
    @Path("add")
    @Produces({ "application/json" })
    public JOCDefaultResponse postOrdersAdd(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
}
