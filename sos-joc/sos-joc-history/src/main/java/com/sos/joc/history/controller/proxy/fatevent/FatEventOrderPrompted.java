package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;
import com.sos.joc.history.controller.proxy.HistoryEventType;

// without outcome
public final class FatEventOrderPrompted extends AFatEventOrderBase {

    private final String question;

    public FatEventOrderPrompted(Long eventId, Date eventDatetime, String orderId, Position position, String question) {
        super(eventId, eventDatetime, orderId, position);
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderPrompted;
    }

}
