
package com.sos.joc.tag.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface ITagModify {

    @POST
    @Path("rename")
    @Produces({ "application/json" })
    public JOCDefaultResponse postRename(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
}
