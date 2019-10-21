package com.sos.joc.deploy.impl;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.deploy.resource.IDeploySaveConfigurationResource;
import com.sos.joc.model.deploy.DeployFilter;

@Path("deploy")
public class DeploySaveConfigurationImpl implements IDeploySaveConfigurationResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeploySaveConfigurationImpl.class);
    private static final String API_CALL = "./deploy/save";

	@Override
	public JOCDefaultResponse postDeploySaveConfiguration(String xAccessToken, DeployFilter filter, String comment)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
