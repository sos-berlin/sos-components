package com.sos.joc.notification.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IStoreNotification {

    @POST
    @Path("store")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postStoreNotification(@HeaderParam("X-Access-Token") String xAccessToken, byte[] storeNotificationFilter);
}
