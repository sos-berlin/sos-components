package com.sos.joc.history.controller.proxy;

import js7.data.event.Event;
import js7.data.order.OrderId;
import js7.proxy.javaapi.data.controller.JEventAndControllerState;

public class FluxEventHandler {

    public static void processEvent(JEventAndControllerState<Event> event, String controllerId) throws Exception {
        String className = event.stampedEvent().value().event().getClass().getSimpleName();
        if ("OrderAdded".equals(className)) {
            OrderId oid = (OrderId) event.stampedEvent().value().key();
            // System.out.println("" + oid.string());
        }
    }

}
