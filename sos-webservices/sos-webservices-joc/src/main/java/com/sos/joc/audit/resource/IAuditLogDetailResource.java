
package com.sos.joc.audit.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IAuditLogDetailResource {

    @POST
    @Path("detail")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postAuditLogDetail(@HeaderParam("X-Access-Token") String xAccessToken, byte[] bytes);
    
    @POST
    @Path("details")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postAuditLogDetails(@HeaderParam("X-Access-Token") String xAccessToken, byte[] bytes);

}
