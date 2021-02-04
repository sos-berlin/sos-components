package com.sos.js7.history.helper;

import java.util.Date;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.util.SOSParameterSubstitutor;
import com.sos.js7.event.controller.EventMeta;

public class HistoryUtil {

    public static final String NEW_LINE = "\r\n";

    public static String getFolderFromPath(String path) {
        if (!path.startsWith("/")) {
            return "/";
        }
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

    public static String map2Json(Map<String, js7.data.value.Value> map) throws JsonProcessingException {
        if (map == null || map.isEmpty()) {
            return null;
        }

        JsonObjectBuilder b = Json.createObjectBuilder();
        for (Map.Entry<String, js7.data.value.Value> entry : map.entrySet()) {
            convert2JsonType(b, entry.getKey(), entry.getValue());
        }
        return b.build().toString();
    }

    private static void convert2JsonType(JsonObjectBuilder b, String name, js7.data.value.Value value) {
        if (value instanceof js7.data.value.StringValue) {
            b.add(name, value.convertToString());
        } else if (value instanceof js7.data.value.NumberValue) {
            try {
                b.add(name, ((scala.math.BigDecimal) value.toJava()).bigDecimal());
            } catch (Throwable e) {

            }
        } else if (value instanceof js7.data.value.BooleanValue) {
            b.add(name, (Boolean.parseBoolean(value.convertToString())));
        }
    }
}
