package com.sos.joc.publish.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.model.publish.SetVersionFilter;

public interface ISetVersion {

    @POST
    @Path("set_version")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postSetVersion(@HeaderParam("X-Access-Token") String xAccessToken, byte[] setVersionFilter) throws Exception;
}
