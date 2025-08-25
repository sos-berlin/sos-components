
package com.sos.joc.task.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import com.sos.joc.annotation.CompressedAlready;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;

public interface ITaskLogResource {

    public static final String PATH = "log";
    public static final String PATH_RUNNING = PATH + "/running";
    public static final String PATH_DOWNLOAD = PATH + "/download";
    public static final String PATH_UNSUBSCRIBE = PATH + "/unsubscribe";

    public static final String IMPL_PATH_LOG = WebservicePaths.getResourceImplPath(WebservicePaths.TASK, PATH);
    public static final String IMPL_PATH_LOG_RUNNING = WebservicePaths.getResourceImplPath(WebservicePaths.TASK, PATH_RUNNING);
    public static final String IMPL_PATH_LOG_DOWNLOAD = WebservicePaths.getResourceImplPath(WebservicePaths.TASK, PATH_DOWNLOAD);
    public static final String IMPL_PATH_LOG_UNSUBSCRIBE = WebservicePaths.getResourceImplPath(WebservicePaths.TASK, PATH_UNSUBSCRIBE);

    @POST
    @Path(PATH)
    @CompressedAlready
    @Consumes("application/json")
    // @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postTaskLog(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

    @POST
    @Path(PATH_RUNNING)
    @CompressedAlready
    @Consumes("application/json")
    // @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postRollingTaskLog(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

    @GET
    @Path(PATH_DOWNLOAD)
    @CompressedAlready
    // @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public JOCDefaultResponse downloadTaskLog(@HeaderParam("X-Access-Token") String xAccessToken, @QueryParam("accessToken") String queryAccessToken,
            @QueryParam("controllerId") String controllerId, @QueryParam("taskId") Long taskId);

    @POST
    @Path(PATH_DOWNLOAD)
    @CompressedAlready
    @Consumes("application/json")
    // @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public JOCDefaultResponse downloadTaskLog(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

    @POST
    @Path(PATH_UNSUBSCRIBE)
    @CompressedAlready
    @Consumes("application/json")
    // @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public JOCDefaultResponse unsubscribeTaskLog(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
}
