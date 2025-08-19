package com.sos.joc.publish.repository.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface ILinkFolder {

    @POST
    @Path("link")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postLink(@HeaderParam("X-Access-Token") String xAccessToken, byte[] linkFolderFilter) throws Exception;
}
