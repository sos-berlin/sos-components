
package com.sos.joc.approval.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IModifyStateResource {

    @POST
    @Path("approve")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postApprove(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);
    
    @POST
    @Path("reject")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postReject(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);

}
