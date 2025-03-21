
package com.sos.joc.tags.group.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

public interface IGroup {

    @POST
    @Path("read")
    @Produces({ "application/json" })
    public JOCDefaultResponse readGroups(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);
}
