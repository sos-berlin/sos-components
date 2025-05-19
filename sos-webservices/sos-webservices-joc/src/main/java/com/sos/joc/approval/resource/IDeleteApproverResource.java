
package com.sos.joc.approval.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IDeleteApproverResource {

    @POST
    @Path("approver/delete")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postDelete(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
