package com.sos.joc.agents.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IAgentsExport {

    @POST
    @Path("export")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postExport(@HeaderParam("X-Access-Token") String xAccessToken, byte[] agentsExportFilter);
}
