package com.sos.jobscheduler.model.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME, 
		include = JsonTypeInfo.As.PROPERTY, 
		property = "TYPE")
@JsonSubTypes({ 
		@JsonSubTypes.Type(value = OrderAdded.class, name = "OrderAdded"),
		@JsonSubTypes.Type(value = OrderAttached.class, name = "OrderAttached"),
		@JsonSubTypes.Type(value = OrderTransferredToAgent.class, name = "OrderTransferredToAgent"),
		@JsonSubTypes.Type(value = OrderProcessingStarted.class, name = "OrderProcessingStarted"),
		@JsonSubTypes.Type(value = OrderStdoutWritten.class, name = "OrderStdoutWritten"),
		@JsonSubTypes.Type(value = OrderStderrWritten.class, name = "OrderStderrWritten"),
		@JsonSubTypes.Type(value = OrderProcessed.class, name = "OrderProcessed"),
		@JsonSubTypes.Type(value = OrderForked.class, name = "OrderForked"),
		@JsonSubTypes.Type(value = OrderJoined.class, name = "OrderJoined"),
		@JsonSubTypes.Type(value = OrderOffered.class, name = "OrderOffered"),
		@JsonSubTypes.Type(value = OrderAwaiting.class, name = "OrderAwaiting"),
		@JsonSubTypes.Type(value = OrderMoved.class, name = "OrderMoved"),
		@JsonSubTypes.Type(value = OrderDetachable.class, name = "OrderDetachable"),
		@JsonSubTypes.Type(value = OrderDetached.class, name = "OrderDetached"),
		@JsonSubTypes.Type(value = OrderFinished.class, name = "OrderFinished") })
public interface IEvent {

	public EventType getTYPE();

	public void setTYPE(EventType tYPE);
}
