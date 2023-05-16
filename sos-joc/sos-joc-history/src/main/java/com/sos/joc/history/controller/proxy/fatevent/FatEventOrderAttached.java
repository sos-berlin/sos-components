package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;
import java.util.List;

import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;
import com.sos.joc.history.controller.proxy.HistoryEventType;

// without outcome
public final class FatEventOrderAttached extends AFatEventOrderBase {

    private List<Date> waitingForAdmission;
    private boolean started;

    public FatEventOrderAttached(Long eventId, Date eventDatetime, String orderId, Position position, List<Date> waitingForAdmission, boolean started)
            throws Exception {
        super(eventId, eventDatetime, orderId, position);
        this.waitingForAdmission = waitingForAdmission;
        this.started = started;
    }

    public List<Date> getWaitingForAdmission() {
        return waitingForAdmission;
    }

    public boolean isStarted() {
        return started;
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderAttached;
    }

}
