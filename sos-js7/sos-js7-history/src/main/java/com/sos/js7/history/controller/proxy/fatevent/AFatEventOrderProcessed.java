package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Date;
import java.util.List;

import com.sos.joc.classes.history.HistoryPosition;

public abstract class AFatEventOrderProcessed extends AFatEvent {

    private String orderId;
    private FatOutcome outcome;
    private String position;

    public AFatEventOrderProcessed(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @Override
    public void set(Object... objects) {
        if (objects.length > 0) {
            this.orderId = (String) objects[0];
            if (objects.length > 1) {// FatEventOrderStepProcessed, FatEventOrderFailed
                if (objects[1] != null) {
                    this.outcome = (FatOutcome) objects[1];
                }
                if (objects.length > 2) {
                    this.position = HistoryPosition.asString((List<?>) objects[2]);
                }
            }
        }
    }

    public String getOrderId() {
        return orderId;
    }

    public FatOutcome getOutcome() {
        return outcome;
    }

    public String getPosition() {
        return position;
    }
}
