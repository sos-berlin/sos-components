
package com.sos.joc.order.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import com.sos.joc.annotation.CompressedAlready;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;

public interface IOrderLogResource {

    public static final String PATH = "log";
    public static final String PATH_RUNNING = PATH + "/running";
    public static final String PATH_DOWNLOAD = PATH + "/download";
    public static final String PATH_UNSUBSCRIBE = PATH + "/unsubscribe";

    public static final String IMPL_PATH_LOG = WebservicePaths.getResourceImplPath(WebservicePaths.ORDER, PATH);
    public static final String IMPL_PATH_LOG_RUNNING = WebservicePaths.getResourceImplPath(WebservicePaths.ORDER, PATH_RUNNING);
    public static final String IMPL_PATH_LOG_DOWNLOAD = WebservicePaths.getResourceImplPath(WebservicePaths.ORDER, PATH_DOWNLOAD);
    public static final String IMPL_PATH_LOG_UNSUBSCRIBE = WebservicePaths.getResourceImplPath(WebservicePaths.ORDER, PATH_UNSUBSCRIBE);

    @POST
    @Path(PATH)
    @CompressedAlready
    @Consumes("application/json")
    public JOCDefaultResponse postOrderLog(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

    @POST
    @Path(PATH_RUNNING)
    @CompressedAlready
    @Consumes("application/json")
    public JOCDefaultResponse postRollingOrderLog(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

    @GET
    @Path(PATH_DOWNLOAD)
    @CompressedAlready
    public JOCDefaultResponse downloadOrderLog(@HeaderParam("X-Access-Token") String xAccessToken, @QueryParam("accessToken") String queryAccessToken,
            @QueryParam("controllerId") String controllerId, @QueryParam("historyId") Long historyId);

    @POST
    @Path(PATH_DOWNLOAD)
    @CompressedAlready
    @Consumes("application/json")
    public JOCDefaultResponse downloadOrderLog(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

    @POST
    @Path(PATH_UNSUBSCRIBE)
    @CompressedAlready
    @Consumes("application/json")
    public JOCDefaultResponse unsubscribeOrderLog(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
}
