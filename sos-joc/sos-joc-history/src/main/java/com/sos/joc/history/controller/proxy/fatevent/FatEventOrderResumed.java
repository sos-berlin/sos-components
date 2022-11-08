package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventType;

import js7.data.workflow.Instruction;

// without outcome
public final class FatEventOrderResumed extends AFatEventOrderProcessed {

    private FatInstruction instruction;
    private boolean isStarted;

    public FatEventOrderResumed(Long eventId, Date eventDatetime, Instruction instruction, boolean isStarted) {
        super(eventId, eventDatetime);
        if (instruction != null) {
            this.instruction = new FatInstruction(instruction);
        }
        this.isStarted = isStarted;
    }

    public FatInstruction getInstruction() {
        return instruction;
    }

    public boolean isStarted() {
        return isStarted;
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderResumed;
    }
}
