
package com.sos.joc.tags.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;

public interface ITags {

    @POST
    @Produces({ "application/json" })
    public JOCDefaultResponse postTags(@HeaderParam("X-Access-Token") String xAccessToken);
}
