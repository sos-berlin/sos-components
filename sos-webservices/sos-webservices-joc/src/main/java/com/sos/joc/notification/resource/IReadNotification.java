package com.sos.joc.notification.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IReadNotification {

    @POST
//    @Path("")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postReadNotification(@HeaderParam("X-Access-Token") String xAccessToken, byte[] readNotificationFilter);
}
