package com.sos.jobscheduler.event.master;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;

public final class JobSchedulerEvent {

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

    private EventSeq eventSeq = null;
    private Long lastEventId = null;
    private JsonArray stampeds = null;
    private JsonObject lastStampedsEntry = null;

    public JobSchedulerEvent(Long eventId, final JsonObject events) {
        String seq = getEventSeq(events);
        if (seq.equalsIgnoreCase(EventSeq.NonEmpty.name())) {
            eventSeq = EventSeq.NonEmpty;
            stampeds = getEventStampeds(events);
            lastStampedsEntry = getLastJsonObject(stampeds);
            lastEventId = getEventId(lastStampedsEntry);
        } else if (seq.equalsIgnoreCase(EventSeq.Empty.name())) {
            eventSeq = EventSeq.Empty;
            lastEventId = getLastEventId(events);
        } else if (seq.equalsIgnoreCase(EventSeq.Torn.name())) {
            eventSeq = EventSeq.Torn;
        }
    }

    public static JsonObject getLastJsonObject(JsonArray arr) {
        if (arr == null) {
            return null;
        }
        int size = arr.size();
        if (size == 0) {
            return null;
        }
        return arr.getJsonObject(size - 1);
    }

    public static Long getEventId(JsonObject json) {
        Long eventId = null;
        if (json != null) {
            JsonNumber r = json.getJsonNumber(EventKey.eventId.name());
            if (r != null) {
                eventId = r.longValue();
            }
        }
        return eventId;
    }

    public static Long getLastEventId(JsonObject json) {
        Long eventId = null;
        if (json != null) {
            JsonNumber r = json.getJsonNumber(EventKey.lastEventId.name());
            if (r != null) {
                eventId = r.longValue();
            }
        }
        return eventId;
    }

    public static String getEventSeq(JsonObject json) {
        return json == null ? null : json.getString(EventKey.TYPE.name());
    }

    public static JsonArray getEventStampeds(JsonObject json) {
        return json == null ? null : json.getJsonArray(EventKey.stampeds.name());
    }

    public EventSeq getEventSeq() {
        return eventSeq;
    }

    public Long getLastEventId() {
        return lastEventId;
    }

    public JsonArray getStampeds() {
        return stampeds;
    }

    public JsonObject getLastStampedsEntry() {
        return lastStampedsEntry;
    }

}
