package com.sos.joc.event.bean.order;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AddOrderEvent extends OrderEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public AddOrderEvent() {
    }

    public AddOrderEvent(String controllerId, String orderId, String workflowName) {
        super("AddOrderEvent", controllerId, orderId);
        putVariable("workflowName", workflowName);
    }
    
    @JsonIgnore
    public String getWorkflowName() {
        return (String) getVariables().get("workflowName");
    }
}
