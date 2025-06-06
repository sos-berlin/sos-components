
package com.sos.joc.tags.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

public interface ITagsUsedBy {

    @POST
    @Path("used")
    @Produces({ "application/json" })
    public JOCDefaultResponse postUsedBy(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
}
