
package com.sos.joc.task.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

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
    @Path("log/rolling")
    @CompressedAlready
    @Consumes("application/json")
    // @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postRollingTaskLog(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

    @GET
    @Path("log/download")
    @CompressedAlready
    // @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public JOCDefaultResponse downloadTaskLog(@HeaderParam("X-Access-Token") String xAccessToken, @QueryParam("accessToken") String queryAccessToken,
            @QueryParam("jobschedulerId") String jobschedulerId, @QueryParam("taskId") Long taskId);

    @POST
    @Path("log/download")
    @CompressedAlready
    @Consumes("application/json")
    // @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public JOCDefaultResponse downloadTaskLog(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
}
