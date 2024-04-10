package com.sos.joc.joc.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IUriResource {

    @Path("url")
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse setUrl(@HeaderParam("X-Access-Token") String accessToken, byte[] body);
    
    @Path("uri")
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse setUri(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

}
