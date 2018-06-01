package com.sos.webservices.deployment.interfaces;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.jobscheduler.model.workflow.Workflow;

public interface IDeploy {
	
	public void sendConfiguration(String configJson);

	public void sendConfiguration(Workflow workflow) throws JsonProcessingException;
	
	public void rollbackTo(String version);
	 
}
