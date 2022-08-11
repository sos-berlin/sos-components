package com.sos.joc.cluster.common;

import java.time.Instant;
import java.util.Date;

public class JocClusterUtil {

    public static Date getEventIdAsDate(Long eventId) {
        return eventId == null ? null : Date.from(eventId2Instant(eventId));
    }

    public static Instant eventId2Instant(Long eventId) {
        return Instant.ofEpochMilli(eventId / 1000);
    }

    public static Instant timestamp2Instant(Long timestamp) {
        return Instant.ofEpochMilli(timestamp);
    }

    public static Long getDateAsEventId(Date date) {
        return date == null ? null : date.getTime() * 1_000;
    }

    public static String getBasenameFromPath(String path) {
        int li = path.lastIndexOf("/");
        return li > -1 ? path.substring(li + 1) : path;
    }

}
