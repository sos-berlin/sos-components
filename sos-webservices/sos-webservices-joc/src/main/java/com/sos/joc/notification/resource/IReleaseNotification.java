package com.sos.joc.notification.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IReleaseNotification {

    @POST
    @Path("release")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postReleaseNotification(@HeaderParam("X-Access-Token") String xAccessToken, byte[] deleteNotificationFilter);

}
