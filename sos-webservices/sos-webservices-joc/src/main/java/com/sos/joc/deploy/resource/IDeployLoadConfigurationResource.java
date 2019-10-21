package com.sos.joc.deploy.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.model.deploy.DeployFilter;

public interface IDeployLoadConfigurationResource {

    @Path("save")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
	public JOCDefaultResponse postDeployLoadConfiguration(String xAccessToken, DeployFilter filter, String comment) throws Exception;
	
}
