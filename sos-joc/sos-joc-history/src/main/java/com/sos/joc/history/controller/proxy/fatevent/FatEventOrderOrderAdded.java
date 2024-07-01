package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.OrderAddedInfo;
import com.sos.joc.history.controller.proxy.HistoryEventType;

public final class FatEventOrderOrderAdded extends AFatEventOrderBase {

    private final OrderAddedInfo orderAddedInfo;

    public FatEventOrderOrderAdded(Long eventId, Date eventDatetime, String orderId, Position position, OrderAddedInfo orderAddedInfo) {
        super(eventId, eventDatetime, orderId, position);
        this.orderAddedInfo = orderAddedInfo;
    }

    public OrderAddedInfo getOrderAddedInfo() {
        return orderAddedInfo;
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderOrderAdded;
    }

}
