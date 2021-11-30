package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.OrderLock;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;
import com.sos.joc.history.controller.proxy.HistoryEventType;

public final class FatEventOrderLockReleased extends AFatEventOrderLock {

    public FatEventOrderLockReleased(Long eventId, Date eventDatetime, String orderId, OrderLock orderLock, Position position) {
        super(eventId, eventDatetime, orderId, orderLock, position);
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderLockReleased;
    }

}
