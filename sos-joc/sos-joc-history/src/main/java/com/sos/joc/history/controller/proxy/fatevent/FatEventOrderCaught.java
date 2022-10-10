package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;
import com.sos.joc.history.controller.proxy.HistoryEventType;

import js7.data.workflow.Instruction;
import js7.data.workflow.instructions.Retry;
import js7.data.workflow.instructions.TryInstruction;

// without outcome
public final class FatEventOrderCaught extends AFatEventOrderBase {

    public enum FatEventOrderCaughtCause {
        Unknown, TryInstruction, Retry
    }

    private FatEventOrderCaughtCause cause = FatEventOrderCaughtCause.Unknown;

    public FatEventOrderCaught(Long eventId, Date eventDatetime, String orderId, Position position, Instruction instruction) {
        super(eventId, eventDatetime, orderId, position);
        if (instruction != null) {
            if (instruction instanceof TryInstruction) {
                cause = FatEventOrderCaughtCause.TryInstruction;
            } else if (instruction instanceof Retry) {
                cause = FatEventOrderCaughtCause.Retry;
            }
        }
    }

    public FatEventOrderCaughtCause getCause() {
        return cause;
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderCaught;
    }

}
