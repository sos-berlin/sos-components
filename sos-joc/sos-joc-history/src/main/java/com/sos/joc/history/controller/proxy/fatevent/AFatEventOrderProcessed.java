package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;
import com.sos.joc.history.helper.IOriginalOrderIdProvider;

public abstract class AFatEventOrderProcessed extends AFatEvent implements IOriginalOrderIdProvider {

    private String orderId;
    private FatOutcome outcome;
    private FatPosition position;

    public AFatEventOrderProcessed(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @Override
    public void set(Object... objects) {
        if (objects.length > 0) {
            this.orderId = (String) objects[0];
            if (objects.length > 1) {// FatEventOrderStepProcessed, FatEventOrderFailed
                if (objects[1] != null) {
                    this.outcome = toFatOutcome(objects[1]);
                }
                if (objects.length > 2) {
                    this.position = new FatPosition((Position) objects[2]);
                }
            }
        }
    }

    @Override
    public String getOrderId() {
        return orderId;
    }

    public FatOutcome getOutcome() {
        return outcome;
    }

    public FatPosition getPosition() {
        return position;
    }

}
