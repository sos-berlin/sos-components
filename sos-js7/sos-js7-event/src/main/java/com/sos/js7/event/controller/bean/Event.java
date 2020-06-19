package com.sos.js7.event.controller.bean;

import java.util.List;

import com.sos.js7.event.controller.EventMeta.EventSeq;

public class Event {

    private EventSeq type;
    private Long lastEventId;
    private List<IEntry> stamped;
    private Long after;// Torn

    public EventSeq getType() {
        return type;
    }

    public void setType(EventSeq val) {
        type = val;
    }

    public Long getLastEventId() {
        return lastEventId;
    }

    public void setLastEventId(Long val) {
        lastEventId = val;
    }

    public List<IEntry> getStamped() {
        return stamped;
    }

    public void setStamped(List<IEntry> val) {
        stamped = val;
    }

    public Long getAfter() {
        return after;
    }

    public void setAfter(Long val) {
        after = val;
    }

}
