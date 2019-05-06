package com.sos.jobscheduler.model.deploy;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME, 
		include = JsonTypeInfo.As.PROPERTY, 
		property = "TYPE")
@JsonSubTypes({ 
		@JsonSubTypes.Type(value = com.sos.jobscheduler.model.workflow.Workflow.class, name = "Workflow"),
		@JsonSubTypes.Type(value = com.sos.jobscheduler.model.agent.AgentRef.class, name = "AgentRef")})
public interface IDeployable {
	
	public DeployType getTYPE();
	
	public void setTYPE(DeployType tYPE);
}
