
package com.sos.joc.jobscheduler.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.sos.joc.annotation.CompressedAlready;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.model.jobscheduler.UrlParameter;

public interface IJobSchedulerLogResource {

    @GET
    @Path("log")
    @CompressedAlready
    //@Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getMainLog(@HeaderParam("X-Access-Token") String xAccessToken,
            @QueryParam("accessToken") String queryAccessToken, @QueryParam("jobschedulerId") String jobschedulerId, @QueryParam("url") String url,
            @QueryParam("filename") String filename) throws Exception;

    @POST
    @Path("log")
    @CompressedAlready
    @Consumes("application/json")
    //@Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getMainLog(@HeaderParam("X-Access-Token") String xAccessToken, UrlParameter urlParamSchema) throws Exception;

    @POST
    @Path("log/info")
    @Consumes("application/json")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getLogInfo(@HeaderParam("X-Access-Token") String xAccessToken, UrlParameter urlParamSchema) throws Exception;
    
    @GET
    @Path("debuglog")
    //@CompressedAlready
    //@Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getDebugLog(@HeaderParam("X-Access-Token") String xAccessToken,
            @QueryParam("accessToken") String queryAccessToken, @QueryParam("jobschedulerId") String jobschedulerId, @QueryParam("url") String url,
            @QueryParam("filename") String filename) throws Exception;

    @POST
    @Path("debuglog")
    //@CompressedAlready
    @Consumes("application/json")
    //@Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getDebugLog(@HeaderParam("X-Access-Token") String xAccessToken, UrlParameter urlParamSchema) throws Exception;

    @POST
    @Path("debuglog/info")
    @Consumes("application/json")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getDebugLogInfo(@HeaderParam("X-Access-Token") String xAccessTokenn, UrlParameter urlParamSchema) throws Exception;

}
