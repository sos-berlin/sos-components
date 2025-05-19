package com.sos.joc.xmleditor.resource;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.xmleditor.commons.JocXmlEditor;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

public interface ISchemaDownloadResource {

    public static final String PATH = "schema/download";
    public static final String IMPL_PATH = JocXmlEditor.getResourceImplPath(PATH);

    @GET
    @Path(PATH)
    public JOCDefaultResponse process(@HeaderParam("X-Access-Token") final String xAccessToken, @QueryParam("accessToken") String accessToken,
            @QueryParam("objectType") String objectType, @QueryParam("show") String show, @QueryParam("schemaIdentifier") String schemaIdentifier);

}
