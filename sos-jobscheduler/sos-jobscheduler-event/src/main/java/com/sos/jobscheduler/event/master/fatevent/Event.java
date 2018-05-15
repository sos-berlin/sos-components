package com.sos.jobscheduler.event.master.fatevent;

import java.util.List;

import com.sos.jobscheduler.event.master.JobSchedulerEvent.EventSeq;

public class Event {

    private EventSeq type;
    private Long lastEventId;
    private List<Entry> stampeds;

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

    public List<Entry> getStampeds() {
        return stampeds;
    }

    public void setStampeds(List<Entry> val) {
        stampeds = val;
    }

}
