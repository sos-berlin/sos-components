package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryOrder.OrderLock;

public abstract class AFatEventOrderLock extends AFatEvent {

    private final String orderId;
    private final OrderLock orderLock;

    public AFatEventOrderLock(Long eventId, Date eventDatetime, String orderId, OrderLock orderLock) {
        super(eventId, eventDatetime);
        this.orderId = orderId;
        this.orderLock = orderLock;
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
}
