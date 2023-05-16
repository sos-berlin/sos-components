package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;
import java.util.List;

import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;
import com.sos.joc.history.controller.proxy.HistoryEventType;

import js7.data.order.OrderEvent.OrderMoved;
import js7.data.workflow.Instruction;
import js7.data_for_java.workflow.position.JPosition;

// without outcome
public final class FatEventOrderMoved extends AFatEventOrderBase {

    private String to;
    private String reason;
    private FatInstruction instruction;
    private List<Date> waitingForAdmission;
    private boolean started;

    public FatEventOrderMoved(Long eventId, Date eventDatetime, String orderId, Position position, OrderMoved om, Instruction instruction,
            List<Date> waitingForAdmission, boolean started) {
        super(eventId, eventDatetime, orderId, position);
        this.to = JPosition.apply(om.to()).toString();
        this.reason = getReason(om);
        if (instruction != null) {
            this.instruction = new FatInstruction(instruction);
        }
        this.waitingForAdmission = waitingForAdmission;
        this.started = started;
    }

    public String getTo() {
        return to;
    }

    public String getReason() {
        return reason;
    }

    public FatInstruction getInstruction() {
        return instruction;
    }

    public List<Date> getWaitingForAdmission() {
        return waitingForAdmission;
    }

    public boolean isStarted() {
        return started;
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderMoved;
    }

    // js7.data.order.OrderEvent$OrderMoved$SkippedDueToWorkflowPathControl$
    private String getReason(final OrderMoved om) {
        if (!om.reason().isDefined()) {
            return null;
        }

        final String className = om.reason().get().getClass().getName();
        String cn = className;
        if (className.endsWith("$")) {
            cn = className.substring(0, className.length() - 1);
        }
        int indx = cn.lastIndexOf("$");
        if (indx == -1) {
            indx = cn.lastIndexOf(".");
        }
        return indx == -1 ? cn : cn.substring(indx + 1);
    }
}
