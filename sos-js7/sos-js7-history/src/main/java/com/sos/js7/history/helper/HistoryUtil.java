package com.sos.js7.history.helper;

import java.util.Date;

import com.sos.commons.util.SOSParameterSubstitutor;
import com.sos.js7.event.controller.EventMeta;

public class HistoryUtil {

    public static final String NEW_LINE = "\r\n";

    public static String getFolderFromPath(String path) {
        int li = path.lastIndexOf("/");
        if (li == 0) {
            return path.substring(0, 1);
        }
        return li > -1 ? path.substring(0, li) : path;
    }

    public static String getBasenameFromPath(String path) {
        int li = path.lastIndexOf("/");
        return li > -1 ? path.substring(li + 1) : path;
    }

    /** An variable is referenced as "${VAR}" */
    public static String resolveVars(String cmd) {
        SOSParameterSubstitutor ps = new SOSParameterSubstitutor();
        String val = ps.replaceEnvVars(cmd);
        return ps.replaceSystemProperties(val);
    }

    public static String nl2sp(String value) {
        return value.replaceAll("\\r\\n|\\r|\\n", " ");
    }

    public static Date getEventIdAsDate(Long eventId) {
        return eventId == null ? null : Date.from(EventMeta.eventId2Instant(eventId));
    }

    public static Long getDateAsEventId(Date date) {
        return date == null ? null : date.getTime() * 1_000;
    }
}
