package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Date;
import java.util.List;

import com.sos.js7.history.controller.proxy.HistoryEventType;

public final class FatEventOrderForked extends AFatEventOrder {

    private List<FatForkedChild> childs;

    public FatEventOrderForked(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void set(Object... objects) {
        super.set(objects);
        this.childs = (List<FatForkedChild>) objects[objects.length - 1];
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderForked;
    }

    public List<FatForkedChild> getChilds() {
        return childs;
    }
}
