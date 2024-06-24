package com.sos.joc.event.bean.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.event.bean.JOCEvent;

public class OrderEvent extends JOCEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderEvent() {
    }

    public OrderEvent(String key, String controllerId, String orderId) {
        super(key, controllerId, null);
        putVariable("orderId", orderId);
    }
    
    @JsonIgnore
    public String getOrderId() {
        return (String) getVariables().get("orderId");
    }
    
}
