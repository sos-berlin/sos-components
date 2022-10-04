package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;
import com.sos.joc.history.controller.proxy.HistoryEventType;

// without outcome
public final class FatEventOrderNoticesRead extends AFatEventOrderNotice {

    private final FatExpectNotices notices;

    public FatEventOrderNoticesRead(Long eventId, Date eventDatetime, String orderId, Position position, FatExpectNotices notices) {
        super(eventId, eventDatetime, orderId, position);
        this.notices = notices;
    }

    public FatExpectNotices getNotices() {
        return notices;
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderNoticesRead;
    }

}
