package com.sos.joc.deploy.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.model.publish.DeployFilter;

public interface IDeployDownloadUnsignedConfigurationResource {

    @Path("download")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public JOCDefaultResponse postDownloadUnsignedConfiguration(@HeaderParam("X-Access-Token") String xAccessToken, DeployFilter filter)
            throws Exception;
}
