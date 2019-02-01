package com.sos.jobscheduler.model.command;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME, 
		include = JsonTypeInfo.As.PROPERTY, 
		property = "TYPE")
@JsonSubTypes({ 
    @JsonSubTypes.Type(value = JSBatchCommands.class, name = "JSBatchCommands"),
    @JsonSubTypes.Type(value = CancelOrder.class, name = "CancelOrder"),
    @JsonSubTypes.Type(value = Abort.class, name = "EmercencyStop"),
	@JsonSubTypes.Type(value = Terminate.class, name = "Terminate") })
public interface ICommand {

	public CommandType getTYPE();

	public void setTYPE(CommandType tYPE);
}
