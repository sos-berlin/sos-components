package com.sos.joc.publish.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.model.publish.SetVersionsFilter;

public interface ISetVersions {

    @POST
    @Path("set_versions")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postSetVersion(@HeaderParam("X-Access-Token") String xAccessToken, SetVersionsFilter setVersionsFilter) throws Exception;
}
