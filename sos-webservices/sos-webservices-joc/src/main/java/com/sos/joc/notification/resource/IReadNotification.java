package com.sos.joc.notification.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IReadNotification {

    @POST
//    @Path("")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postReadNotification(@HeaderParam("X-Access-Token") String xAccessToken, byte[] readNotificationFilter);
}
