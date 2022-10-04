package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;
import com.sos.joc.history.controller.proxy.HistoryEventType;

// without outcome
public final class FatEventOrderNoticePosted extends AFatEventOrderNotice {

    private final FatPostNotice notice;

    public FatEventOrderNoticePosted(Long eventId, Date eventDatetime, String orderId, Position position, FatPostNotice notice) {
        super(eventId, eventDatetime, orderId, position);
        this.notice = notice;
    }

    public FatPostNotice getNotice() {
        return notice;
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderNoticePosted;
    }

}
