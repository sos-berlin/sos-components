
package com.sos.joc.controller.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IControllerLogResource {

    @GET
    @Path("log")
    //@CompressedAlready
    //@Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON })
//    public JOCDefaultResponse getDebugLog(@HeaderParam("X-Access-Token") String xAccessToken,
//            @QueryParam("accessToken") String queryAccessToken, @QueryParam("controllerId") String controllerId, @QueryParam("url") String url,
//            @QueryParam("filename") String filename);
    public JOCDefaultResponse getDebugLog(@HeaderParam("X-Access-Token") String xAccessToken,
            @QueryParam("accessToken") String queryAccessToken, @QueryParam("controllerId") String controllerId, @QueryParam("url") String url);

    @POST
    @Path("log")
    //@CompressedAlready
    @Consumes("application/json")
    //@Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getDebugLog(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    
//    @POST
//    @Path("log/info")
//    @Consumes("application/json")
//    @Produces({ MediaType.APPLICATION_JSON })
//    public JOCDefaultResponse getDebugLogInfo(@HeaderParam("X-Access-Token") String xAccessTokenn, UrlParameter urlParamSchema);

}
