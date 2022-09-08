
package com.sos.joc.security.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IActiveSessionsResource {

    @POST
    @Path("sessions")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postSessions(@HeaderParam("X-Access-Token") String accessToken, byte[] body);
    
    @POST
    @Path("sessions/cancel")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postSessionsCancel(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

  
}
