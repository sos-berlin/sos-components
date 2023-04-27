
package com.sos.joc.security.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IFido2RegistrationResource {

    @POST
    @Path("fido2registration")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postFido2RegistrationRead(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("fido2registration/requestregistration")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postFido2RequestRegistration(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("fido2registration/verifyregistration")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postFido2VerifyRegistration(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("fido2registration/delete")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postFido2RegistrationDelete(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("fido2registration/approve")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postFido2RegistrationApprove(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("fido2registration/reject")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postFido2RegistrationReject(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("fido2registration/confirm")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postFido2RegistrationConfirm(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("fido2registrations")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postFido2Registrations(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

}