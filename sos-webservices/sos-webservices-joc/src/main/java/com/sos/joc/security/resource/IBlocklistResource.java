
package com.sos.joc.security.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IBlocklistResource {

    @POST
    @Path("blockedAccounts")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postBlockedAccounts(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

       
    @POST
    @Path("blockedAccount/store")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postBlockedAccountStore(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("blockedAccounts/delete")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postBlockedAccountsDelete(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

   
}
