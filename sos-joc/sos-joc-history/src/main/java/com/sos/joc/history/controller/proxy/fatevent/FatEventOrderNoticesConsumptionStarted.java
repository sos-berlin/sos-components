package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;
import java.util.List;

import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;
import com.sos.joc.history.controller.proxy.HistoryEventType;

// without outcome
public final class FatEventOrderNoticesConsumptionStarted extends AFatEventOrderNotice {

    private final List<FatExpectNotice> notices;

    public FatEventOrderNoticesConsumptionStarted(Long eventId, Date eventDatetime, String orderId, Position position,
            List<FatExpectNotice> notices) {
        super(eventId, eventDatetime, orderId, position);
        this.notices = notices;
    }

    public List<FatExpectNotice> getNotices() {
        return notices;
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderNoticesConsumptionStarted;
    }

}
