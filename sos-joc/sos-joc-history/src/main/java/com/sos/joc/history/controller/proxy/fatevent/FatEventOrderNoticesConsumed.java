package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;
import com.sos.joc.history.controller.proxy.HistoryEventType;

// without outcome
public final class FatEventOrderNoticesConsumed extends AFatEventOrderNotice {

    private final boolean failed;

    public FatEventOrderNoticesConsumed(Long eventId, Date eventDatetime, String orderId, Position position, boolean failed) {
        super(eventId, eventDatetime, orderId, position);
        this.failed = failed;
    }

    public boolean isFailed() {
        return failed;
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderNoticesConsumed;
    }

}
