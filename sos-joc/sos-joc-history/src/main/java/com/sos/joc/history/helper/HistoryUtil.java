package com.sos.joc.history.helper;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.common.Variables;
import com.sos.joc.Globals;

import js7.data.value.ListValue;
import js7.data.value.ObjectValue;
import js7.data.value.Value;
import scala.collection.JavaConverters;

public class HistoryUtil {

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
        Variables v = new Variables();
        for (Map.Entry<String, js7.data.value.Value> entry : map.entrySet()) {
            if (entry.getValue() instanceof js7.data.value.ListValue) {
                List<Value> lv = ((ListValue) entry.getValue()).toJava();
                for (Value l : lv) {
                    if (l instanceof ObjectValue) {
                        try {
                            v.setAdditionalProperty(entry.getKey(), toVariables(JavaConverters.asJava(((ObjectValue) l).nameToValue())));
                        } catch (Throwable e) {
                        }
                    }
                }
            } else {
                v.setAdditionalProperty(entry.getKey(), entry.getValue().toJava());
            }
        }
        return v;
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
            map = new HashMap<>();
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
