package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Date;
import java.util.List;

import com.sos.js7.history.controller.proxy.HistoryEventType;

public final class FatEventOrderJoined extends AFatEventOrder {

    private List<FatForkedChild> childs;
    private FatOutcome outcome;

    public FatEventOrderJoined(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void set(Object... objects) {
        super.set(objects);
        this.childs = (List<FatForkedChild>) objects[objects.length - 2];
        this.outcome = (FatOutcome) objects[objects.length - 1];
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderJoined;
    }

    public List<FatForkedChild> getChilds() {
        return childs;
    }

    public FatOutcome getOutcome() {
        return outcome;
    }

}
