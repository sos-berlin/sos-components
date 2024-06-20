package com.sos.joc.event.bean.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.event.bean.JOCEvent;

public class AddOrderEvent extends JOCEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public AddOrderEvent() {
    }

    public AddOrderEvent(String controllerId, String orderId, String orderJson) {
        super("AddOrderEvent", controllerId, null);
        putVariable("orderId", orderId);
        putVariable("orderJson", orderJson);
    }
    
    public AddOrderEvent(String controllerId, String orderId) {
        super("AddOrderEvent", controllerId, null);
        putVariable("orderId", orderId);
    }
    
    @JsonIgnore
    public String getOrderId() {
        return (String) getVariables().get("orderId");
    }
    
    @JsonIgnore
    public String getOrderJson() {
        return (String) getVariables().get("orderJson");
    }
}
