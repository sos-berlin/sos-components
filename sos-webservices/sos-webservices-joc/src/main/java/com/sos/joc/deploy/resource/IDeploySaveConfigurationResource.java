package com.sos.joc.deploy.resource;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IDeploySaveConfigurationResource {

    @Path("save")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
	public JOCDefaultResponse postDeploySaveConfiguration(@HeaderParam("X-Access-Token") String xAccessToken, final InputStream jsObj)
			throws Exception;
	
}
