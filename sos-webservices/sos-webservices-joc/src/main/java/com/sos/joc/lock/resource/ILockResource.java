
package com.sos.joc.lock.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface ILockResource {

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postPermanent(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
