package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;
import com.sos.joc.history.controller.proxy.HistoryEventType;

// without outcome
public final class FatEventOrderSleeping extends AFatEventOrderBase {

    private final Date until;

    public FatEventOrderSleeping(Long eventId, Date eventDatetime, String orderId, Position position, Date until) {
        super(eventId, eventDatetime, orderId, position);
        this.until = until;
    }

    public Date getUntil() {
        return until;
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderSleeping;
    }

}
