package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;
import java.util.List;

import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.OrderLock;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;
import com.sos.joc.history.controller.proxy.HistoryEventType;

public final class FatEventOrderLocksAcquired extends AFatEventOrderLocks {

    public FatEventOrderLocksAcquired(Long eventId, Date eventDatetime, String orderId, Position position, List<OrderLock> orderLocks) {
        super(eventId, eventDatetime, orderId, position, orderLocks);
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderLocksAcquired;
    }

}
