package com.sos.joc.deploy.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.model.deploy.DeployLoadFilter;

public interface IDeployLoadConfigurationResource {

    @Path("load")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
	public JOCDefaultResponse postDeployLoadConfiguration(@HeaderParam("X-Access-Token") String xAccessToken, DeployLoadFilter filter)
			throws Exception;
	
}
