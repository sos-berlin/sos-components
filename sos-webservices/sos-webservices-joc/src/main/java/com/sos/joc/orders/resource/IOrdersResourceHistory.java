
package com.sos.joc.orders.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;

public interface IOrdersResourceHistory {

    public static final String PATH = "history";
    public static final String IMPL_PATH = WebservicePaths.getResourceImplPath(WebservicePaths.ORDERS, PATH);

    @POST
    @Path(PATH)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postOrdersHistory(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
}
