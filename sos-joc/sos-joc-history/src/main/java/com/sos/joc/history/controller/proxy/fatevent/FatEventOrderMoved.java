package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;
import com.sos.joc.history.controller.proxy.HistoryEventType;

import js7.data.order.OrderEvent.OrderMoved;
import js7.data.workflow.Instruction;
import js7.data.workflow.instructions.Execute.Named;
import js7.data_for_java.workflow.position.JPosition;

// without outcome
public final class FatEventOrderMoved extends AFatEventOrderBase {

    private String to;
    private String reason;
    private String jobName;
    private boolean isOrderStarted;

    public FatEventOrderMoved() {
        super(null, null, null, null);
    }

    public FatEventOrderMoved(Long eventId, Date eventDatetime, String orderId, Position position, OrderMoved om, Instruction instruction,
            boolean isOrderStarted) {
        super(eventId, eventDatetime, orderId, position);
        this.to = JPosition.apply(om.to()).toString();
        this.reason = getReason(om.reason().get().getClass().getSimpleName());
        this.jobName = getJobName(instruction);
        this.isOrderStarted = isOrderStarted;
    }

    public String getTo() {
        return to;
    }

    public String getReason() {
        return reason;
    }

    public String getJobName() {
        return jobName;
    }

    public boolean isOrderStarted() {
        return isOrderStarted;
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderMoved;
    }

    private String getReason(String className) {
        return className.endsWith("$") ? className.substring(0, className.length() - 1) : className;
    }

    private String getJobName(Instruction instruction) {
        if (instruction instanceof Named) {
            return ((Named) instruction).name().string();
        }
        return null;
    }
}
