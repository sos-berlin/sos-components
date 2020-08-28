
package com.sos.joc.orders.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IOrdersResourceAdd {

    @POST
    @Path("add")
    @Produces({ "application/json" })
    public JOCDefaultResponse postOrdersAdd(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
}
