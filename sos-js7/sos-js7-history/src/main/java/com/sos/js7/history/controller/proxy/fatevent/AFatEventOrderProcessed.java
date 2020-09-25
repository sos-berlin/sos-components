package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Date;

public abstract class AFatEventOrderProcessed extends AFatEvent {

    private String orderId;
    private FatOutcome outcome;

    public AFatEventOrderProcessed(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @Override
    public void set(Object... objects) {
        if (objects.length > 0) {
            this.orderId = (String) objects[0];
            if (objects.length > 1) {// FatEventOrderStepProcessed, FatEventOrderFailed
                this.outcome = (FatOutcome) objects[1];
            }
        }
    }

    public String getOrderId() {
        return orderId;
    }

    public FatOutcome getOutcome() {
        return outcome;
    }
}
