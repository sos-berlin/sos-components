package com.sos.jobscheduler.event.master;

public final class EventMeta {

    public static final String MASTER_API_PATH = "/master/api/";

    public static enum EventSeq {
        NonEmpty, Empty, Torn
    };

    public static enum EventPath {
        event, order, workflow, fatEvent
    };

    public static enum EventKey {
        TYPE, lastEventId, eventId, stampeds
    };
}
