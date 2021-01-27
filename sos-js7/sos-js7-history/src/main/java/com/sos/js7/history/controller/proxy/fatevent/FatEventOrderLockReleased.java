package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.js7.history.controller.proxy.HistoryEventType;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryOrder.OrderLock;

public final class FatEventOrderLockReleased extends AFatEventOrderLock {

    public FatEventOrderLockReleased(Long eventId, Date eventDatetime, String orderId, OrderLock orderLock) {
        super(eventId, eventDatetime, orderId, orderLock);
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderLockReleased;
    }

}
