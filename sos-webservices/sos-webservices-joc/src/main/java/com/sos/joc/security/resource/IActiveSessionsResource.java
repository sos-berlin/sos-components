
package com.sos.joc.security.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IActiveSessionsResource {

    @POST
    @Path("sessions")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postSessions(@HeaderParam("X-Access-Token") String accessToken, byte[] body);
    
    @POST
    @Path("sessions/delete")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postSessionsDelete(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

  
}
