
package com.sos.joc.controller.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

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
