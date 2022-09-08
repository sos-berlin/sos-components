
package com.sos.joc.order.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import com.sos.joc.annotation.CompressedAlready;
import com.sos.joc.classes.JOCDefaultResponse;

public interface IOrderLogResource {

    @POST
    @Path("log")
    @CompressedAlready
    @Consumes("application/json")
    public JOCDefaultResponse postOrderLog(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

    @POST
    @Path("log/running")
    @CompressedAlready
    @Consumes("application/json")
    public JOCDefaultResponse postRollingOrderLog(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @GET
    @Path("log/download")
    @CompressedAlready
    public JOCDefaultResponse downloadOrderLog(@HeaderParam("X-Access-Token") String xAccessToken, @QueryParam("accessToken") String queryAccessToken,
            @QueryParam("controllerId") String controllerId, @QueryParam("historyId") Long historyId);

    @POST
    @Path("log/download")
    @CompressedAlready
    @Consumes("application/json")
    public JOCDefaultResponse downloadOrderLog(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
}
