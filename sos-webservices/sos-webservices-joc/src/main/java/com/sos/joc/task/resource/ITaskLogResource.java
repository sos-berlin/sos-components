
package com.sos.joc.task.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import com.sos.joc.annotation.CompressedAlready;
import com.sos.joc.classes.JOCDefaultResponse;

public interface ITaskLogResource {

    @POST
    @Path("log")
    @CompressedAlready
    @Consumes("application/json")
    // @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postTaskLog(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

    @POST
    @Path("log/running")
    @CompressedAlready
    @Consumes("application/json")
    // @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postRollingTaskLog(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

    @GET
    @Path("log/download")
    @CompressedAlready
    // @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public JOCDefaultResponse downloadTaskLog(@HeaderParam("X-Access-Token") String xAccessToken, @QueryParam("accessToken") String queryAccessToken,
            @QueryParam("controllerId") String controllerId, @QueryParam("taskId") Long taskId);

    @POST
    @Path("log/download")
    @CompressedAlready
    @Consumes("application/json")
    // @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public JOCDefaultResponse downloadTaskLog(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
}
