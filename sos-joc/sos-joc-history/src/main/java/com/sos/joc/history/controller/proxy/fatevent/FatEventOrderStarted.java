package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventType;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.OrderStartedInfo;

public final class FatEventOrderStarted extends AFatEventOrder {

    private Date scheduledFor;
    private boolean maybePreviousStatesLogged;
    private Integer priority;

    public FatEventOrderStarted(Long eventId, Date eventDatetime, OrderStartedInfo osi) {
        super(eventId, eventDatetime);
        this.scheduledFor = osi.getScheduledFor();
        this.maybePreviousStatesLogged = osi.maybePreviousStatesLogged();
    }
    
    @Override
    public void set(Object... objects) {
        super.set(objects);
        this.priority = (Integer) objects[objects.length - 1];
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderStarted;
    }

    public Date getScheduledFor() {
        return scheduledFor;
    }

    public boolean maybePreviousStatesLogged() {
        return maybePreviousStatesLogged;
    }
    
    public Integer getPriority() {
        return priority;
    }
}
