
package com.sos.joc.audit.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface ICommentsResource {

    @POST
    @Path("comments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postComments(@HeaderParam("X-Access-Token") String xAccessToken, @HeaderParam("access_token") String accessToken) throws Exception;

}
