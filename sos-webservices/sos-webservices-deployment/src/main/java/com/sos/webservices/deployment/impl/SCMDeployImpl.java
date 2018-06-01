package com.sos.webservices.deployment.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.webservices.deployment.interfaces.IDeploy;

public class SCMDeployImpl implements IDeploy {

	public void connectToSCM(String url) {
		// TODO Auto-generated method stub

	}

	public void sendCommitCommand(String commitCommand) {
		// TODO Auto-generated method stub
		
	}

	public void sendTagCommand(String tagCommand, String version) {
		// TODO Auto-generated method stub
		
	}

	public void sendCheckoutCommand(String checkoutCommand) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendConfiguration(String configJson) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendConfiguration(Workflow workflow) throws JsonProcessingException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void rollbackTo(String version) {
		// TODO Auto-generated method stub
		
	}

}
