package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventType;

import js7.data.workflow.Instruction;

// without outcome
public final class FatEventOrderSuspended extends AFatEventOrderProcessed {

    private String stoppedJobName;
    private boolean isStarted;

    public FatEventOrderSuspended(Long eventId, Date eventDatetime, Instruction instruction, boolean isStarted) {
        super(eventId, eventDatetime);
        this.stoppedJobName = getJobName(instruction);
        this.isStarted = isStarted;
    }

    public String getStoppedJobName() {
        return stoppedJobName;
    }

    public boolean isStarted() {
        return isStarted;
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderSuspended;
    }
}
