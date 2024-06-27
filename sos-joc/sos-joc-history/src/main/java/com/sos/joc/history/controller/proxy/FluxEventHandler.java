package com.sos.joc.history.controller.proxy;

import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.order.AddOrderEvent;
import com.sos.joc.event.bean.order.TerminateOrderEvent;

import js7.data.event.Event;
import js7.data.order.OrderEvent.OrderAdded;
import js7.data.order.OrderEvent.OrderTerminated;
import js7.data.order.OrderId;
import js7.data_for_java.order.JOrder;
import js7.proxy.javaapi.data.controller.JEventAndControllerState;

public class FluxEventHandler {

    public static void processEvent(JEventAndControllerState<Event> eventAndState, String controllerId) throws Exception {
        Event event = eventAndState.stampedEvent().value().event();
        if (event instanceof OrderAdded) {
            OrderId oid = (OrderId) eventAndState.stampedEvent().value().key();
            JOrder jOrder = eventAndState.state().idToOrder().get(oid);
            if (jOrder != null) {
                EventBus.getInstance().post(new AddOrderEvent(controllerId, oid.string(), jOrder.workflowId().path().string()));
            }
        } else if (event instanceof OrderTerminated) { // cancelled or finished
            OrderId oid = (OrderId) eventAndState.stampedEvent().value().key();
            String orderIdModifier = oid.string().substring(12, 13);
            boolean isChildOrder = oid.string().contains("|");
            if ("D".equals(orderIdModifier) && !isChildOrder) { //order generated by addOrder instruction
                EventBus.getInstance().post(new TerminateOrderEvent(controllerId, oid.string()));
            }
        }
    }

}