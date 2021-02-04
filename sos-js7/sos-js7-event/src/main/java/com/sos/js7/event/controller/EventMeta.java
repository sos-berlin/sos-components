package com.sos.js7.event.controller;

import java.time.Instant;

public final class EventMeta {

    public static final String CONTROLLER_API_PATH = "/controller/api/";

    public static enum EventSeq {
        NonEmpty, Empty, Torn
    };

    public static enum ClusterEventSeq {
        NodesAppointed, PreparedToBeCoupled, Coupled, ActiveShutDown, PassiveLost, SwitchedOver, FailedOver
    };

    public static enum EventPath {
        event, fatEvent, cluster
    };

    public static enum Path {
        command, session
    };

    public static Instant eventId2Instant(Long eventId) {
        return Instant.ofEpochMilli(eventId / 1000);
    }

    public static Instant timestamp2Instant(Long timestamp) {
        return Instant.ofEpochMilli(timestamp);
    }

}
