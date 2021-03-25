
package com.sos.joc.controller.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IControllerResourceSwitch {

    @POST
    @Path("switch")
    @Produces({ "application/json" })
    public JOCDefaultResponse postJobschedulerSwitch(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBody);

}
