package com.sos.jobscheduler.model.command;

public interface ICommand {
	
	public CommandType getTYPE();
	
	public void setTYPE(CommandType tYPE);
}
