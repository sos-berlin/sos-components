package com.sos.joc.event.bean.history;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "TYPE", visible = true)
@JsonSubTypes({ @JsonSubTypes.Type(HistoryOrderTerminated.class), @JsonSubTypes.Type(HistoryOrderStarted.class),
        @JsonSubTypes.Type(HistoryOrderUpdated.class) })

public abstract class HistoryOrderEvent extends HistoryEvent {

    public HistoryOrderEvent(String key, String controllerId, String orderId, String workflowName, String workflowVersion, Object payload) {
        super(key, controllerId, null, payload);
        putVariable("orderId", orderId);
        putVariable("workflowName", workflowName);
        putVariable("WorkflowVersionId", workflowVersion);
    }

    @JsonIgnore
    public String getOrderId() {
        return getVariables().get("orderId");
    }

    @JsonIgnore
    public String getWorkflowName() {
        return getVariables().get("workflowName");
    }

    @JsonIgnore
    public String getWorkflowVersionId() {
        return getVariables().get("WorkflowVersionId");
    }
}
