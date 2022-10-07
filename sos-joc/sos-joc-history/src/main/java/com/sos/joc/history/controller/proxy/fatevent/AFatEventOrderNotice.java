package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;

// without outcome
public abstract class AFatEventOrderNotice extends AFatEventOrderBase {

    public AFatEventOrderNotice(Long eventId, Date eventDatetime, String orderId, Position position) {
        super(eventId, eventDatetime, orderId, position);
    }

}
