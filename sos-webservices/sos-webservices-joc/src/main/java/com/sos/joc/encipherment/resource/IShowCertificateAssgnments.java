package com.sos.joc.encipherment.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IShowCertificateAssgnments {

    @POST
//    @Path("assignment")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postShowCertificateAssignment(@HeaderParam("X-Access-Token") String xAccessToken, byte[] showAssignmentFilter) throws Exception;
}
