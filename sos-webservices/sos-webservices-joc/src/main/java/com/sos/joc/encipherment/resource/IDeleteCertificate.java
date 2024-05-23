package com.sos.joc.encipherment.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IDeleteCertificate {

    @POST
    @Path("delete")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postDeleteCertificate(@HeaderParam("X-Access-Token") String xAccessToken, byte[] deleteCertificateFilter) throws Exception;
}
