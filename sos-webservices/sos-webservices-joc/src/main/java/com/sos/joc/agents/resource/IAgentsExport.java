package com.sos.joc.agents.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IAgentsExport {

    @POST
    @Path("export")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postExport(@HeaderParam("X-Access-Token") String xAccessToken, byte[] agentsExportFilter);
}
