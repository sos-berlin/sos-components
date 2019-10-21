package com.sos.joc.deploy.impl;

import javax.ws.rs.Path;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.deploy.resource.IDeployUploadSignedConfigurationResource;

@Path("deploy")
public class DeployUploadSignedConfigurationImpl implements IDeployUploadSignedConfigurationResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployUploadSignedConfigurationImpl.class);
    private static final String API_CALL = "./deploy/upload";

    @Override
	public JOCDefaultResponse postUploadSignedConfiguration(String xAccessToken, FormDataBodyPart body, String comment) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
