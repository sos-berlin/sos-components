package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;
import java.util.List;

import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.OrderLock;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;

public abstract class AFatEventOrderLocks extends AFatEventOrderBase {

    private final List<OrderLock> orderLocks;

    public AFatEventOrderLocks(Long eventId, Date eventDatetime, String orderId, Position position, List<OrderLock> orderLocks) {
        super(eventId, eventDatetime, orderId, position);
        this.orderLocks = orderLocks;
    }

    public List<OrderLock> getOrderLocks() {
        return orderLocks;
    }

}
