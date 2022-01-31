
package com.sos.joc.security.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface ISecurityConfigurationResource {

    @POST
    @Path("auth")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postAuthRead(@HeaderParam("X-Access-Token") String xAccessToken, byte[] body);

    @POST
    @Path("auth/store")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postAuthStore(@HeaderParam("X-Access-Token") String xAccessToken, byte[] body);

    @POST
    @Path("auth/accounts/delete")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postAuthAcountsDelete(@HeaderParam("X-Access-Token") String xAccessToken, byte[] body);

    @POST
    @Path("auth/account/rename")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postAuthAcountRename(@HeaderParam("X-Access-Token") String xAccessToken, byte[] body);

    @POST
    @Path("auth/roles/delete")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postAuthRolesDelete(@HeaderParam("X-Access-Token") String xAccessToken, byte[] body);

    @POST
    @Path("auth/changepassword")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse changePassword(@HeaderParam("X-Access-Token") String xAccessToken, byte[] body);

    @POST
    @Path("auth/forcepasswordchange")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse forcePasswordChange(@HeaderParam("X-Access-Token") String xAccessToken, byte[] body);

    @POST
    @Path("auth/resetpassword")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse resetPassword(@HeaderParam("X-Access-Token") String xAccessToken, byte[] body);

}
