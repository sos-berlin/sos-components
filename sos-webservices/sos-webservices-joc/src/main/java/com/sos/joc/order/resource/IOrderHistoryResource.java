
package com.sos.joc.order.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;

public interface IOrderHistoryResource {

    public static final String PATH = "history";
    public static final String IMPL_PATH = WebservicePaths.getResourceImplPath(WebservicePaths.ORDER, PATH);

    @POST
    @Path(PATH)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postOrderHistory(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);
}
