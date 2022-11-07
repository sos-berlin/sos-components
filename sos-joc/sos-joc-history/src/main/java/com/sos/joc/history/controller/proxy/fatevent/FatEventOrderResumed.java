package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventType;

import js7.data.workflow.Instruction;

// without outcome
public final class FatEventOrderResumed extends AFatEventOrderProcessed {

    private String jobName;
    private boolean isStarted;

    public FatEventOrderResumed(Long eventId, Date eventDatetime, Instruction instruction, boolean isStarted) {
        super(eventId, eventDatetime);
        this.jobName = getJobName(instruction);
        this.isStarted = isStarted;
    }

    public String getJobName() {
        return jobName;
    }

    public boolean isStarted() {
        return isStarted;
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderResumed;
    }
}
