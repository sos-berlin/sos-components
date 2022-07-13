package com.sos.joc.settings.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IReadSettings {

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postReadSettings(@HeaderParam("X-Access-Token") String xAccessToken);
}
