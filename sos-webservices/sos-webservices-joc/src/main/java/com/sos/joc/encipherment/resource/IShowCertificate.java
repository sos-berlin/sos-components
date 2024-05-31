package com.sos.joc.encipherment.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IShowCertificate {

    @POST
//    @Path("encipherment/certificate")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postShowCertificate(@HeaderParam("X-Access-Token") String xAccessToken, byte[] showCertificateFilter) throws Exception;
}
