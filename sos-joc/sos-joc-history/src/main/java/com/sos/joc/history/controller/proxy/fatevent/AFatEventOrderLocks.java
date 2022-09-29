package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;
import java.util.List;

import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.OrderLock;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;

public abstract class AFatEventOrderLocks extends AFatEvent {

    private final String orderId;
    private final List<OrderLock> orderLocks;
    private final String position;

    public AFatEventOrderLocks(Long eventId, Date eventDatetime, String orderId, List<OrderLock> orderLocks, Position position) {
        super(eventId, eventDatetime);
        this.orderId = orderId;
        this.orderLocks = orderLocks;
        this.position = position == null ? null : position.asString();
    }

    @Override
    public void set(Object... objects) {

    }

    public String getOrderId() {
        return orderId;
    }

    public List<OrderLock> getOrderLocks() {
        return orderLocks;
    }

    public String getPosition() {
        return position;
    }
}
