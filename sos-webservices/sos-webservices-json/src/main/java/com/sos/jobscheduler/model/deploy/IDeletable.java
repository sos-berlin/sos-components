package com.sos.jobscheduler.model.deploy;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME, 
		include = JsonTypeInfo.As.PROPERTY, 
		property = "TYPE")
@JsonSubTypes({ 
		@JsonSubTypes.Type(value = com.sos.jobscheduler.model.workflow.DeleteWorkflow.class, name = "WorkflowPath"),
		@JsonSubTypes.Type(value = com.sos.jobscheduler.model.agent.DeleteAgentRef.class, name = "AgentRefPath")})
public interface IDeletable {
	
	public DeleteType getTYPE();
	
	public void setTYPE(DeleteType tYPE);
}
