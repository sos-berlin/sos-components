package com.sos.js7.history.helper;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSParameterSubstitutor;
import com.sos.js7.event.controller.EventMeta;

public class HistoryUtil {

    public static final String NEW_LINE = "\r\n";
    private static final String POSITION_DELIMITER = "/";

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

    public static String getPositionAsString(List<?> positions) {
        if (positions != null) {
            return positions.stream().map(o -> o.toString()).collect(Collectors.joining(POSITION_DELIMITER));
        }
        return null;
    }

    public static String getPositionParentAsString(List<?> positions) {// 0->0, 1/fork_1/0 -> 1/fork_1
        if (positions == null || positions.size() < 1) {
            return null;
        }
        // if (pos.size() == 1) {
        // return pos.get(0).toString();
        // }
        return positions.stream().limit(positions.size() - 1).map(o -> o.toString()).collect(Collectors.joining(POSITION_DELIMITER));
    }

    public static Integer getPositionRetry(List<?> positions) {
        if (positions != null) {
            Optional<?> r = positions.stream().filter(f -> f.toString().startsWith("try+")).findFirst();
            if (r.isPresent()) {
                return Integer.parseInt(r.get().toString().substring(3));// TODO check
            }
        }
        return 0;
    }

    public static Integer getPositionLast(List<?> positions) {
        if (positions != null && positions.size() > 0) {
            return (Integer) positions.get(positions.size() - 1);
        }
        return null;
    }

    public static Date getEventIdAsDate(Long eventId) {
        return eventId == null ? null : Date.from(EventMeta.eventId2Instant(eventId));
    }

}
