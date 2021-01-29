package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Date;
import java.util.List;

import com.sos.js7.history.controller.proxy.HistoryEventType;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryOrder.OrderLock;

public final class FatEventOrderLockAcquired extends AFatEventOrderLock {

    public FatEventOrderLockAcquired(Long eventId, Date eventDatetime, String orderId, OrderLock orderLock, List<?> position) {
        super(eventId, eventDatetime, orderId, orderLock, position);
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderLockAcquired;
    }

}
