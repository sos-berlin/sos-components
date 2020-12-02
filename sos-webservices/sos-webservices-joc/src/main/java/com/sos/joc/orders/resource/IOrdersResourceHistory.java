
package com.sos.joc.orders.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
