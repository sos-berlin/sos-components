package com.sos.jobscheduler.model.job;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME, 
		include = JsonTypeInfo.As.PROPERTY, 
		property = "TYPE")
@JsonSubTypes({ 
		@JsonSubTypes.Type(value = ExecutableScript.class, name = "ExecutableScript"),
		@JsonSubTypes.Type(value = ExecutablePath.class, name = "ExecutablePath")})
public interface IExecutable {
	
	public ExecutableType getTYPE();
	
	public void setTYPE(ExecutableType tYPE);
}
