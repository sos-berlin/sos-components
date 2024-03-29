package com.sos.joc.documentation.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IDocumentationShowResource {

//    @GET
//    @Path("show")
//    public JOCDefaultResponse show(@HeaderParam("X-Access-Token") String xAccessToken, @QueryParam("accessToken") String accessToken,
//            @QueryParam("path") String path, @QueryParam("objectType") String type);
//
//    @POST
//    @Path("show")
//    @Consumes("application/json")
//    public JOCDefaultResponse show(@HeaderParam("X-Access-Token") String xAccessToken, byte[] inFilter);

    // only an alias for show
    @GET
    @Path("preview")
    public JOCDefaultResponse preview(@HeaderParam("X-Access-Token") String xAccessToken, @QueryParam("accessToken") String accessToken,
            @QueryParam("documentation") String path);
    
    @GET
    @Path("show")
    public JOCDefaultResponse show(@HeaderParam("X-Access-Token") String xAccessToken, @QueryParam("accessToken") String accessToken,
            @QueryParam("documentation") String path);
    
//    @POST
//    @Path("show")
//    @Consumes("application/json")
//    public JOCDefaultResponse preview(@HeaderParam("X-Access-Token") String xAccessToken, byte[] inFilter);

//    @POST
//    @Path("url")
//    @Consumes("application/json")
//    public JOCDefaultResponse postUrl(@HeaderParam("X-Access-Token") String xAccessToken, byte[] inFilter);
}
