package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Date;
import java.util.List;

import com.sos.joc.classes.history.HistoryPosition;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryOrder.OrderLock;

public abstract class AFatEventOrderLock extends AFatEvent {

    private final String orderId;
    private final OrderLock orderLock;
    private final String position;

    public AFatEventOrderLock(Long eventId, Date eventDatetime, String orderId, OrderLock orderLock, List<?> position) {
        super(eventId, eventDatetime);
        this.orderId = orderId;
        this.orderLock = orderLock;
        this.position = HistoryPosition.asString(position);
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
