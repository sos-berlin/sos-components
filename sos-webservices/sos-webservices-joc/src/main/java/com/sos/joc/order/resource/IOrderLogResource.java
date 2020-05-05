
package com.sos.joc.order.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.sos.joc.annotation.CompressedAlready;
import com.sos.joc.classes.JOCDefaultResponse;

public interface IOrderLogResource {

    @POST
    @Path("log")
    @CompressedAlready
    @Consumes("application/json")
    // @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postOrderLog(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

    @GET
    @Path("log/download")
    @CompressedAlready
    // @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public JOCDefaultResponse downloadOrderLog(@HeaderParam("X-Access-Token") String xAccessToken, @QueryParam("accessToken") String queryAccessToken,
            @QueryParam("jobschedulerId") String jobschedulerId, @QueryParam("historyId") Long historyId);

    @POST
    @Path("log/download")
    @CompressedAlready
    @Consumes("application/json")
    // @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public JOCDefaultResponse downloadOrderLog(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
}
