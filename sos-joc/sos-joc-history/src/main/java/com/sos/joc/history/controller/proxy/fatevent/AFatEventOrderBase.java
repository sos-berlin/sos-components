package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;

// without outcome
public abstract class AFatEventOrderBase extends AFatEvent {

    private final String orderId;
    private final FatPosition position;

    public AFatEventOrderBase(Long eventId, Date eventDatetime, String orderId, Position position) {
        super(eventId, eventDatetime);
        this.orderId = orderId;
        if (position == null) {
            this.position = null;
        } else {
            this.position = new FatPosition(position);
        }
    }

    @Override
    public void set(Object... objects) {
        // TODO Auto-generated method stub

    }

    public String getOrderId() {
        return orderId;
    }

    public FatPosition getPosition() {
        return position;
    }

}
