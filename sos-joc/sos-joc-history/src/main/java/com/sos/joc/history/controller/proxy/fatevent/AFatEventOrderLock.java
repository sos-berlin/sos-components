package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.OrderLock;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;

public abstract class AFatEventOrderLock extends AFatEvent {

    private final String orderId;
    private final OrderLock orderLock;
    private final String position;

    public AFatEventOrderLock(Long eventId, Date eventDatetime, String orderId, OrderLock orderLock, Position position) {
        super(eventId, eventDatetime);
        this.orderId = orderId;
        this.orderLock = orderLock;
        this.position = position == null ? null : position.asString();
    }

    @Override
    public void set(Object... objects) {

    }

    public String getOrderId() {
        return orderId;
    }

    public OrderLock getOrderLock() {
        return orderLock;
    }

    public String getPosition() {
        return position;
    }
}
