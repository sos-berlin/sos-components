package com.sos.webservices.deployment.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.webservices.deployment.interfaces.IDeploy;

public class SimpleDeployImpl implements IDeploy {
	

	@Override
	public void sendConfiguration(String configJson) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendConfiguration(Workflow workflow) throws JsonProcessingException {
		ObjectMapper om = new ObjectMapper();
		String workflowJson = om.writeValueAsString(workflow);
		// TODO send workflowJson to JobScheduler web service

	}

	@Override
	public void rollbackTo(String version) {
		// TODO: Auto-generated method stub
		
	}

}
