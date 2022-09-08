
package com.sos.joc.security.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IAccountResource {

    @POST
    @Path("account")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postAccountRead(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("account/rename")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postAccountRename(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("account/permissions")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postAccountPermissions(@HeaderParam("X-Access-Token") String accessToken, byte[] body);
    
    @POST
    @Path("account/store")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postAccountStore(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("accounts/delete")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postAccountsDelete(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("accounts")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postAccounts(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("account/changepassword")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse changePassword(@HeaderParam("X-Access-Token") String xAccessToken, byte[] body);

    @POST
    @Path("accounts/forcepasswordchange")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse forcePasswordChange(@HeaderParam("X-Access-Token") String xAccessToken, byte[] body);

    @POST
    @Path("accounts/resetpassword")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse resetPassword(@HeaderParam("X-Access-Token") String xAccessToken, byte[] body);

    @POST
    @Path("accounts/enable")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse enable(@HeaderParam("X-Access-Token") String xAccessToken, byte[] body);

    @POST
    @Path("accounts/disable")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse disable(@HeaderParam("X-Access-Token") String xAccessToken, byte[] body);

}
