package com.sos.jobscheduler.model.command;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME, 
		include = JsonTypeInfo.As.PROPERTY, 
		property = "TYPE")
@JsonSubTypes({ 
		@JsonSubTypes.Type(value = com.sos.jobscheduler.model.command.Abort.class, name = "EmercencyStop"),
		@JsonSubTypes.Type(value = com.sos.jobscheduler.model.command.Terminate.class, name = "Terminate") })
public interface ICommand {

	public CommandType getTYPE();

	public void setTYPE(CommandType tYPE);
}
