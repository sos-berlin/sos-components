package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;
import com.sos.joc.history.controller.proxy.HistoryEventType;

// without outcome
public final class FatEventOrderPriorityChanged extends AFatEventOrderBase {

    private final Integer priority;

    public FatEventOrderPriorityChanged(Long eventId, Date eventDatetime, String orderId, Position position, Integer priority) {
        super(eventId, eventDatetime, orderId, position);
        this.priority = priority;
    }

    public Integer getPriority() {
        return priority;
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderPriorityChanged;
    }

}
