package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventType;

import js7.data.workflow.Instruction;

// without outcome
public final class FatEventOrderSuspended extends AFatEventOrderProcessed {

    private FatInstruction stoppedInstruction;
    private boolean isStarted;

    public FatEventOrderSuspended(Long eventId, Date eventDatetime, Instruction instruction, boolean isStarted) {
        super(eventId, eventDatetime);
        this.stoppedInstruction = new FatInstruction(instruction);
        this.isStarted = isStarted;
    }

    public FatInstruction getStoppedInstruction() {
        return stoppedInstruction;
    }

    public boolean isStarted() {
        return isStarted;
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderSuspended;
    }
}
