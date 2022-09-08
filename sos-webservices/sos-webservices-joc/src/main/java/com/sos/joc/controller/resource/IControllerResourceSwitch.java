
package com.sos.joc.controller.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IControllerResourceSwitch {

    @POST
    @Path("switch")
    @Produces({ "application/json" })
    public JOCDefaultResponse postJobschedulerSwitch(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBody);

}
