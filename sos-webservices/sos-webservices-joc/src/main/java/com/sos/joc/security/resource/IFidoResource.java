
package com.sos.joc.security.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IFidoResource {

    @POST
    @Path("fidoregistration")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postFidoRegistrationRead(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("fidoregistration/request_registration")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postFidoRequestRegistration(byte[] body);

    @POST
    @Path("fidoregistration/request_registration_start")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postFidoRequestRegistrationStart(byte[] body);

    @POST
    @Path("fido/request_authentication")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postFidoRequestAuthentication(byte[] body);

    @POST
    @Path("fido/add_device")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postFidoAddDevice(byte[] body);

    @POST
    @Path("fido/remove_devices")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postFidoRemoveDevices(byte[] body);

    @POST
    @Path("fidoregistration/delete")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postFidoRegistrationDelete(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("fidoregistration/deferr")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postFidoRegistrationDeferr(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("fidoregistration/approve")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postFidoRegistrationApprove(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("fidoregistration/confirm")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postFidoRegistrationConfirm(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("fidoregistrations")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postFidoRegistrations(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("identity_fido_client")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postIdentityFidoclient(byte[] body);
    
    @POST
    @Path("fido/configuration")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postReadFidoConfiguration(@HeaderParam("X-Access-Token") String xAccessToken, byte[] configuration);

    
}
