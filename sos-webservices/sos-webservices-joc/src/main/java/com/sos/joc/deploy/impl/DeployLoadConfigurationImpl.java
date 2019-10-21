package com.sos.joc.deploy.impl;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.deploy.resource.IDeployLoadConfigurationResource;
import com.sos.joc.model.deploy.DeployFilter;

@Path("deploy")
public class DeployLoadConfigurationImpl implements IDeployLoadConfigurationResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployLoadConfigurationImpl.class);
    private static final String API_CALL = "./deploy/load";

	@Override
	public JOCDefaultResponse postDeployLoadConfiguration(String xAccessToken, DeployFilter filter, String comment)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
