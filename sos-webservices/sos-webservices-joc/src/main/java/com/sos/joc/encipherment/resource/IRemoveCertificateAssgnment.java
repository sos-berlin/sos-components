package com.sos.joc.encipherment.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IRemoveCertificateAssgnment {

    @POST
    @Path("remove")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postRemoveCertificateAssgnment(@HeaderParam("X-Access-Token") String xAccessToken, byte[] agentAssignmentFilter) throws Exception;
}
