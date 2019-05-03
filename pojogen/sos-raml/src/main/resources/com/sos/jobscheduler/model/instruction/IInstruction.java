package com.sos.jobscheduler.model.instruction;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME, 
		include = JsonTypeInfo.As.PROPERTY, 
		property = "TYPE")
@JsonSubTypes({ 
		@JsonSubTypes.Type(value = IfElse.class, name = "If"),
		@JsonSubTypes.Type(value = NamedJob.class, name = "Execute.Named"),
		@JsonSubTypes.Type(value = AnonymousJob.class, name = "Execute.Anonymous"),
		@JsonSubTypes.Type(value = ForkJoin.class, name = "Fork"),
		@JsonSubTypes.Type(value = TryCatch.class, name = "Try"),
		@JsonSubTypes.Type(value = Retry.class, name = "Try"),
		@JsonSubTypes.Type(value = RetryInCatch.class, name = "Retry"),
		@JsonSubTypes.Type(value = Finish.class, name = "Finish"),
		@JsonSubTypes.Type(value = Fail.class, name = "Fail"),
		@JsonSubTypes.Type(value = Abort.class, name = "Fail")})
public interface IInstruction {
	
	public InstructionType getTYPE();
	
	public void setTYPE(InstructionType tYPE);
}
