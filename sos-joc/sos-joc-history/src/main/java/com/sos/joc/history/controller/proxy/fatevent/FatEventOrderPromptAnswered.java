package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;
import com.sos.joc.history.controller.proxy.HistoryEventType;

// without outcome
public final class FatEventOrderPromptAnswered extends AFatEventOrderBase {

    public FatEventOrderPromptAnswered(Long eventId, Date eventDatetime, String orderId, Position position) {
        super(eventId, eventDatetime, orderId, position);
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderPromptAnswered;
    }

}
