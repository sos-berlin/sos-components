package com.sos.joc.history.helper;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.common.Variables;
import com.sos.joc.Globals;
import com.sos.joc.classes.order.OrdersHelper;

public class HistoryUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryUtil.class);

    private static final String DEFAULT_TIME_ZONE = "Etc/UTC";
    private static final String TIME_ZONE_GMT = "Etc/GMT";

    public static String getTimeZone(String caller, String timeZone) {
        if (SOSString.isEmpty(timeZone)) {
            LOGGER.info(String.format("[%s]TimeZone is empty. Set to default=%s", caller, DEFAULT_TIME_ZONE));
            return DEFAULT_TIME_ZONE;
        }
        // java TimeZone.getTimeZone - ... the specified TimeZone, or the GMT zone if the given ID cannot be understood
        if (timeZone.toUpperCase().equals("GMT")) {
            LOGGER.info(String.format("[%s]TimeZone=%s. Set to %s", caller, timeZone, TIME_ZONE_GMT));
            return TIME_ZONE_GMT;
        }
        return timeZone;
    }

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

    public static Path getOrderLog(Path dir, Long orderId) {
        return dir.resolve(orderId + ".log");
    }

    public static Path getOrderLog(Path dir, String orderId) {
        try {
            return dir.resolve(orderId + ".log");
        } catch (Throwable e) {
            try {
                String newName = SOSCheckJavaVariableName.makeStringRuleConform(orderId);
                if (SOSString.isEmpty(newName)) {
                    return null;
                }
                return dir.resolve(newName + ".log");
            } catch (Throwable ee) {
                return null;
            }
        }
    }

    // @TODO
    public static String normalizeFileName(String name) {
        return name.replaceAll(":", "_");
    }

    public static Path getOrderStepLog(Path dir, LogEntry le) {
        return dir.resolve(le.getHistoryOrderId() + "_" + le.getHistoryOrderStepId() + ".log");
    }

    public static Path getOrderLogDirectory(Path logDir, Long historyOrderMainParentId) {
        return logDir.resolve(String.valueOf(historyOrderMainParentId));
    }

    public static String getForkChildNameFromOrderId(String forkChildOrderId) {
        int li = forkChildOrderId.lastIndexOf("|");
        return li > -1 ? forkChildOrderId.substring(li + 1) : forkChildOrderId;
    }

    public static String getMainOrderId(String orderId) {
        int li = orderId.indexOf("|");
        return li > -1 ? orderId.substring(0, li) : orderId;
    }

    public static String nl2sp(String value) {
        return value.replaceAll("\\r\\n|\\r|\\n", " ");
    }

    public static String toJsonString(Object o) throws JsonProcessingException {
        if (o == null) {
            return null;
        }
        return Globals.objectMapper.writeValueAsString(o);
    }

    public static <T> T fromJsonString(String content, Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
        if (content == null) {
            return null;
        }
        return (T) Globals.objectMapper.readValue(content, clazz);
    }

    public static String toString(js7.data.value.Value val) {
        if (val == null) {
            return null;
        }
        return val.toJava().toString();
    }

    public static Variables toVariables(Map<String, js7.data.value.Value> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        return OrdersHelper.scalaValuedArgumentsToVariables(map);
    }

    public static Variables toVariables(Map<String, js7.data.value.Value> map, List<CachedWorkflowParameter> params) throws JsonProcessingException {
        Variables v = toVariables(map);
        if (v == null && params == null) {
            return null;
        }
        if (params == null || params.size() == 0) {
            return v;
        }
        if (v == null) {
            map = new LinkedHashMap<>();
            v = new Variables();
        }
        for (CachedWorkflowParameter param : params) {
            if (!map.containsKey(param.getName())) {
                if (param.getValue() != null) {
                    if (param.getType() == null) {// final
                        v.setAdditionalProperty(param.getName(), param.getValue());
                    } else {
                        switch (param.getType()) {
                        case Boolean:
                            v.setAdditionalProperty(param.getName(), Boolean.parseBoolean(param.getValue()));
                            break;
                        case Number:
                            v.setAdditionalProperty(param.getName(), new BigDecimal(param.getValue()));
                            break;
                        case List:
                            break;
                        case String:
                        default:
                            v.setAdditionalProperty(param.getName(), param.getValue());
                            break;
                        }
                    }
                }
            }
        }
        return v;
    }
}
