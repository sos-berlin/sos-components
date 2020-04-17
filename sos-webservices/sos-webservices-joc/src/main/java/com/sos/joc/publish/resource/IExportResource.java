package com.sos.joc.publish.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.model.publish.ExportFilter;

public interface IExportResource {

    @Path("export")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public JOCDefaultResponse postExportConfiguration(@HeaderParam("X-Access-Token") String xAccessToken, ExportFilter filter)
            throws Exception;
}
