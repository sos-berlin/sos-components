package com.sos.jobscheduler.model.instruction;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME, 
		include = JsonTypeInfo.As.PROPERTY, 
		property = "TYPE")
@JsonSubTypes({ 
		@JsonSubTypes.Type(value = IfElse.class, name = "If"),
		@JsonSubTypes.Type(value = Job.class, name = "Job"),
		@JsonSubTypes.Type(value = Workflow.class, name = "Workflow"),
		@JsonSubTypes.Type(value = ForkJoin.class, name = "ForkJoin") })
public interface IInstruction {
	
	public InstructionType getTYPE();
	
	public void setTYPE(InstructionType tYPE);
}
