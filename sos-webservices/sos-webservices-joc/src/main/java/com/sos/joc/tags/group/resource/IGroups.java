
package com.sos.joc.tags.group.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;

public interface IGroups {

    @POST
    @Produces({ "application/json" })
    public JOCDefaultResponse postGroups(@HeaderParam("X-Access-Token") String accessToken);
}
