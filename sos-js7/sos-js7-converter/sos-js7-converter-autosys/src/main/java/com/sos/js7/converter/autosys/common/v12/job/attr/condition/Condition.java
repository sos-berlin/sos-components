package com.sos.js7.converter.autosys.common.v12.job.attr.condition;

import java.util.HashMap;
import java.util.Map;

import com.sos.js7.converter.autosys.common.v12.job.attr.AJobAttributes;
import com.sos.js7.converter.commons.report.CommonReport.ErrorType;
import com.sos.js7.converter.commons.report.ConverterReport;

public class Condition {

    public enum ConditionType {
        DONE, FAILURE, NOTRUNNING, SUCCESS, TERMINATED, EXITCODE, GLOBAL_VARIABLE, SOS_UNKNOWN
    }

    private enum MapKeys {
        NAME, LOOKBACK, EXTERNAL
    }

    private final ConditionType type;
    private final String name;
    private String value;
    private final String lookBack;
    private final String externalInstance;

    /** <br/>
     * v(app.varA) = "X"<br/>
     * s(app.jobA)<br/>
     * f(jobA^PROD,24)<br/>
     * 
     * @throws Exception */
    public Condition(String val) throws Exception {
        try {
            String v = val.trim();
            int i = val.indexOf("(");

            // app.varA
            String innerVal = v.substring(i + 1, v.indexOf(")"));

            Map<MapKeys, String> resultMap = new HashMap<>();
            resultMap = getLookBack(resultMap, innerVal);
            resultMap = getExternalInstance(resultMap);

            this.type = getType(v.substring(0, i).toLowerCase(), val);
            this.name = resultMap.get(MapKeys.NAME);
            this.value = getValue(v);
            this.lookBack = resultMap.get(MapKeys.LOOKBACK);
            this.externalInstance = resultMap.get(MapKeys.EXTERNAL);
        } catch (Throwable e) {
            throw new Exception(String.format("[%s]%s", val, e.toString()), e);
        }
    }

    private ConditionType getType(String t, String original) {
        ConditionType type = ConditionType.SOS_UNKNOWN;
        switch (t) {
        case "d":
        case "done":
            type = ConditionType.DONE;
            break;
        case "f":
        case "failure":
            type = ConditionType.FAILURE;
            break;
        case "s":
        case "success":
            type = ConditionType.SUCCESS;
            break;
        case "v":
        case "value":
            type = ConditionType.GLOBAL_VARIABLE;
            break;
        case "n":
        case "notrunning":
            type = ConditionType.NOTRUNNING;
            break;
        case "t":
        case "terminated":
            type = ConditionType.TERMINATED;
            break;
        default:
            ConverterReport.INSTANCE.addErrorRecord(ErrorType.WARNING, String.format("[%s]unknown condition type=%s", original, t));
            break;
        }
        return type;
    }

    private Map<MapKeys, String> getLookBack(Map<MapKeys, String> map, String val) {
        String[] arr = val.split(",");
        if (arr.length == 2) {
            map.put(MapKeys.NAME, arr[0].trim());
            map.put(MapKeys.LOOKBACK, arr[1].trim());
            ConverterReport.INSTANCE.addErrorRecord(ErrorType.WARNING, String.format("[condition with lookBack]%s", val));
        } else {
            map.put(MapKeys.NAME, val);
        }
        return map;
    }

    private Map<MapKeys, String> getExternalInstance(Map<MapKeys, String> map) {
        String[] arr = map.get(MapKeys.NAME).split("\\^");
        if (arr.length == 2) {
            map.put(MapKeys.NAME, arr[0].trim());
            map.put(MapKeys.EXTERNAL, arr[1].trim());
            ConverterReport.INSTANCE.addErrorRecord(ErrorType.WARNING, String.format("[condition with external]%s", map.get(MapKeys.NAME)));
        }
        return map;
    }

    private String getValue(String val) {
        String[] arr = val.split("=");
        return arr.length == 2 ? AJobAttributes.stringValue(arr[1]) : null;
    }

    public ConditionType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getLookBack() {
        return lookBack;
    }

    public String getExternalInstance() {
        return externalInstance;
    }

    public String getKey() {
        StringBuilder sb = new StringBuilder(name);
        if (externalInstance != null) {
            sb.append("-").append(externalInstance);
        }
        if (lookBack != null) {
            sb.append("-").append(lookBack.replaceAll(":", ""));
        }
        return sb.toString();
    }

}
