package com.sos.joc.xmleditor.resource;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.xmleditor.commons.JocXmlEditor;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface IStandardYADEDeployResource {

    public static final String PATH = "deploy";
    public static final String IMPL_PATH = JocXmlEditor.getResourceImplPath(PATH);

    @POST
    @Path(PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse deploy(@HeaderParam("X-Access-Token") final String accessToken, byte[] in);

}
