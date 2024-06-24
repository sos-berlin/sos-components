package com.sos.joc.event.bean.order;

public class TerminateOrderEvent extends OrderEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public TerminateOrderEvent() {
    }

    public TerminateOrderEvent(String controllerId, String orderId) {
        super("TerminateOrderEvent", controllerId, orderId);
    }
    
}
