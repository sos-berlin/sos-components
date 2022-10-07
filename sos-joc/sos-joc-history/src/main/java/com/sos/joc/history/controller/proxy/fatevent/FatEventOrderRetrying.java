package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;
import com.sos.joc.history.controller.proxy.HistoryEventType;

// without outcome
public final class FatEventOrderRetrying extends AFatEventOrderBase {

    private final Date delayedUntil;

    public FatEventOrderRetrying(Long eventId, Date eventDatetime, String orderId, Position position, Date delayedUntil) {
        super(eventId, eventDatetime, orderId, position);
        this.delayedUntil = delayedUntil;
    }

    public Date getDelayedUntil() {
        return delayedUntil;
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderRetrying;
    }

}
