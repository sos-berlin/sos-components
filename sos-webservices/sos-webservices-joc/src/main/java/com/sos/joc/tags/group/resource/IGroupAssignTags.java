
package com.sos.joc.tags.group.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

public interface IGroupAssignTags {

    @POST
    @Path("store")
    @Produces({ "application/json" })
    public JOCDefaultResponse assignTags(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);
}
