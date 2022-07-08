
package com.sos.joc.security.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
