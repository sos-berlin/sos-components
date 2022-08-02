package com.sos.joc.agents.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IAgentsExport {

    @Path("export")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public JOCDefaultResponse getExport(@HeaderParam("X-Access-Token") String xAccessToken, String  agentsExportFilter);

    @POST
    @Path("export")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postExport(@HeaderParam("X-Access-Token") String xAccessToken, byte[] agentsExportFilter);
}
